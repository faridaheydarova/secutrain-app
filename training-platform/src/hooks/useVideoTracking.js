import { useEffect, useRef, useCallback } from 'react';
import { progressAPI } from '../services/api';

const HEARTBEAT_INTERVAL_MS = 15000; // how often we ping the backend while actively watching
const IDLE_THRESHOLD_MS = 60000;     // no mouse/keyboard/video activity for this long => "away"

/**
 * Tracks a user's interaction with a video: accurate watched-time (via merged
 * intervals, so seeking/skipping doesn't inflate progress), play/pause/seek
 * events, and periodic heartbeats to progressAPI.update().
 *
 * Payload shape matches backend UpdateProgressRequest
 * (ProgressTrackingController.updateProgress): { watchedDuration,
 * lastWatchedPosition, playbackSpeed }. assignmentId/videoId are path
 * variables, not body fields.
 */
export default function useVideoTracking({ assignmentId, videoId, videoRef, enabled }) {
  const lastActivityRef = useRef(Date.now());
  const watchedIntervalsRef = useRef([]);   // closed [start, end] segments (video currentTime, seconds)
  const segmentStartRef = useRef(null);     // currently open segment start, or null
  const isPlayingRef = useRef(false);
  const heartbeatTimerRef = useRef(null);

  const markActivity = useCallback(() => {
    lastActivityRef.current = Date.now();
  }, []);

  const closeSegment = useCallback(() => {
    const el = videoRef.current;
    if (segmentStartRef.current != null && el) {
      const end = el.currentTime;
      if (end > segmentStartRef.current) {
        watchedIntervalsRef.current.push([segmentStartRef.current, end]);
      }
      segmentStartRef.current = null;
    }
  }, [videoRef]);

  const openSegment = useCallback(() => {
    const el = videoRef.current;
    if (el) segmentStartRef.current = el.currentTime;
  }, [videoRef]);

  // Merge overlapping/adjacent intervals so replaying a section isn't double-counted
  const computeWatchedSeconds = useCallback(() => {
    const el = videoRef.current;
    const intervals = [...watchedIntervalsRef.current];
    if (segmentStartRef.current != null && el) {
      intervals.push([segmentStartRef.current, el.currentTime]);
    }
    intervals.sort((a, b) => a[0] - b[0]);
    const merged = [];
    for (const [s, e] of intervals) {
      const last = merged[merged.length - 1];
      if (last && s <= last[1]) last[1] = Math.max(last[1], e);
      else merged.push([s, e]);
    }
    return merged.reduce((sum, [s, e]) => sum + (e - s), 0);
  }, [videoRef]);

  // Matches backend UpdateProgressRequest exactly: assignmentId/videoId are
  // path variables (not part of the body), so the body only needs these three.
  const buildPayload = useCallback(() => {
    const el = videoRef.current;
    return {
      watchedDuration: Math.floor(computeWatchedSeconds()),
      lastWatchedPosition: Math.floor(el?.currentTime || 0),
      playbackSpeed: el?.playbackRate || 1.0,
    };
  }, [videoRef, computeWatchedSeconds]);

  const sendUpdate = useCallback(
    async ({ beacon = false } = {}) => {
      if (!assignmentId || !videoId || !videoRef.current) return;
      const payload = buildPayload();

      if (beacon) {
        // navigator.sendBeacon can't carry an Authorization header, so we use
        // fetch with keepalive:true instead — it survives page unload and
        // still lets us attach the bearer token.
        const token = localStorage.getItem('accessToken');
        const base = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
        try {
          fetch(`${base}/user/progress/${assignmentId}/${videoId}/update`, {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json',
              ...(token ? { Authorization: `Bearer ${token}` } : {}),
            },
            body: JSON.stringify(payload),
            keepalive: true,
          });
        } catch {
          /* best-effort on unload */
        }
        return;
      }

      try {
        await progressAPI.update(assignmentId, videoId, payload);
      } catch (err) {
        console.error('Heartbeat update failed:', err);
      }
    },
    [assignmentId, videoId, videoRef, buildPayload]
  );

  // Periodic heartbeat — only while tab is visible and user isn't idle
  useEffect(() => {
    if (!enabled || !assignmentId) return undefined;
    heartbeatTimerRef.current = setInterval(() => {
      const idleFor = Date.now() - lastActivityRef.current;
      const tabVisible = document.visibilityState === 'visible';
      if (tabVisible && idleFor < IDLE_THRESHOLD_MS) {
        sendUpdate();
      }
    }, HEARTBEAT_INTERVAL_MS);
    return () => clearInterval(heartbeatTimerRef.current);
  }, [enabled, assignmentId, sendUpdate]);

  // Page-wide activity listeners for idle detection (not just video events)
  useEffect(() => {
    if (!enabled) return undefined;
    const opts = { passive: true };
    window.addEventListener('mousemove', markActivity, opts);
    window.addEventListener('keydown', markActivity, opts);
    window.addEventListener('scroll', markActivity, opts);
    window.addEventListener('touchstart', markActivity, opts);
    return () => {
      window.removeEventListener('mousemove', markActivity, opts);
      window.removeEventListener('keydown', markActivity, opts);
      window.removeEventListener('scroll', markActivity, opts);
      window.removeEventListener('touchstart', markActivity, opts);
    };
  }, [enabled, markActivity]);

  // Tab hidden => stop counting watched time; tab visible again => resume if still playing
  useEffect(() => {
    if (!enabled) return undefined;
    const onVisibility = () => {
      if (document.visibilityState === 'hidden') {
        closeSegment();
      } else if (isPlayingRef.current) {
        openSegment();
        markActivity();
      }
    };
    document.addEventListener('visibilitychange', onVisibility);
    return () => document.removeEventListener('visibilitychange', onVisibility);
  }, [enabled, closeSegment, openSegment, markActivity]);

  // Final flush when the user navigates away or closes the tab
  useEffect(() => {
    if (!enabled) return undefined;
    const flush = () => {
      closeSegment();
      sendUpdate({ beacon: true });
    };
    window.addEventListener('beforeunload', flush);
    window.addEventListener('pagehide', flush);
    return () => {
      window.removeEventListener('beforeunload', flush);
      window.removeEventListener('pagehide', flush);
      flush(); // component unmount (e.g. client-side route change)
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [enabled]);

  const handlers = {
    onPlay: () => {
      markActivity();
      isPlayingRef.current = true;
      openSegment();
    },
    onPause: () => {
      markActivity();
      isPlayingRef.current = false;
      closeSegment();
      sendUpdate();
    },
    onSeeking: () => {
      closeSegment();
    },
    onSeeked: () => {
      markActivity();
      if (isPlayingRef.current) openSegment();
    },
    onEnded: () => {
      isPlayingRef.current = false;
      closeSegment();
      sendUpdate();
    },
    onTimeUpdate: () => {
      markActivity();
    },
  };

  return { handlers, getWatchedSeconds: computeWatchedSeconds };
}

import { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { videoAPI, progressAPI } from '../services/api';
import Navbar from '../components/Layout/Navbar';
import Card from '../components/Common/Card';
import Button from '../components/Common/Button';
import ProgressBar from '../components/Common/ProgressBar';
import Loading from '../components/Common/Loading';
import useVideoTracking from '../hooks/useVideoTracking';

const fmt = s => {
  if (!s) return '00:00';
  const m = Math.floor(s / 60);
  const sec = Math.floor(s % 60);
  return `${m}:${sec < 10 ? '0' : ''}${sec}`;
};

export default function VideoPlayer() {
  const { videoId, assignmentId } = useParams();
  const navigate = useNavigate();
  const videoRef = useRef(null);
  const [video, setVideo] = useState(null);
  const [videoSrc, setVideoSrc] = useState(null);
  const [videoError, setVideoError] = useState(null);
  const [progress, setProgress] = useState(null);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [loading, setLoading] = useState(true);
  const [completing, setCompleting] = useState(false);


  const trackingEnabled = !!assignmentId && !progress?.isCompleted;
  const { handlers: trackingHandlers, getWatchedSeconds } = useVideoTracking({
    assignmentId,
    videoId,
    videoRef,
    enabled: trackingEnabled,
  });

  useEffect(() => {
    (async () => {
      try {
        const vRes = await videoAPI.getById(videoId);
        setVideo(vRes.data);
        setDuration(vRes.data.durationSeconds || 0);
        if (assignmentId) {
          await progressAPI.start(assignmentId, videoId);
          const pRes = await progressAPI.get(assignmentId, videoId);
          setProgress(pRes.data);
          setCurrentTime(pRes.data.lastWatchedPosition || 0);
        }
      } catch (err) {
        console.error('Progress load error:', err);
      }
      setLoading(false);
    })();
  }, [videoId, assignmentId]);

  useEffect(() => {
    let revokeUrl = null;
    (async () => {
      try {
        const res = await videoAPI.stream(videoId);
        const url = URL.createObjectURL(res.data);
        revokeUrl = url;
        setVideoSrc(url);
      } catch (err) {
        console.error('Video stream error:', err);
        setVideoError('Video yüklənə bilmədi.');
      }
    })();
    return () => { if (revokeUrl) URL.revokeObjectURL(revokeUrl); };
  }, [videoId]);

  const handleLoadedMetadata = () => {
    const el = videoRef.current;
    if (!el) return;
    if (!duration && el.duration) setDuration(el.duration);
    if (currentTime > 0 && currentTime < el.duration) {
      el.currentTime = currentTime;
    }
  };

  const handleTimeUpdate = () => {
    const el = videoRef.current;
    if (!el) return;
    setCurrentTime(el.currentTime);
    trackingHandlers.onTimeUpdate();
  };

  const handleComplete = async () => {
    if (!assignmentId) return;
    setCompleting(true);
    try {
      await progressAPI.complete(assignmentId, videoId, {
        assignmentId: Number(assignmentId),
        videoId: Number(videoId),
        totalWatchedDuration: Math.floor(getWatchedSeconds() || currentTime)
      });
      const pRes = await progressAPI.get(assignmentId, videoId);
      setProgress(pRes.data);
    } catch (err) {
      console.error('Complete error:', err);
    }
    setCompleting(false);
  };

  if (loading) return <><Navbar title="Video Player" /><Loading /></>;
  if (!video) return <><Navbar title="Video Player" /><p className="p-6 text-slate-500">Video not found.</p></>;

  return (
      <div>
        <Navbar title={video.title} />
        <div className="p-6 space-y-4 animate-fadeIn max-w-4xl mx-auto">
          <Button variant="secondary" onClick={() => navigate(-1)}>← Back</Button>
          <div className="aspect-video bg-black rounded-2xl flex items-center justify-center relative overflow-hidden shadow-2xl">
            {videoSrc ? (
                <video
                    ref={videoRef}
                    src={videoSrc}
                    controls
                    className="w-full h-full"
                    onLoadedMetadata={handleLoadedMetadata}
                    onTimeUpdate={handleTimeUpdate}
                    onPlay={trackingHandlers.onPlay}
                    onPause={trackingHandlers.onPause}
                    onSeeking={trackingHandlers.onSeeking}
                    onSeeked={trackingHandlers.onSeeked}
                    onEnded={trackingHandlers.onEnded}
                />
            ) : videoError ? (
                <div className="text-center text-white/40">
                  <div className="text-6xl mb-4">⚠️</div>
                  <p className="text-sm">{videoError}</p>
                </div>
            ) : (
                <div className="text-center text-white/40">
                  <div className="text-6xl mb-4 animate-pulse">▶</div>
                  <p className="text-sm">Video yüklənir...</p>
                </div>
            )}
          </div>
          <Card>
            <h2 className="font-display text-2xl font-bold mb-2">{video.title}</h2>
            <p className="text-slate-500 mb-4">{video.description}</p>
            <div className="grid grid-cols-3 gap-3 mb-4">
              <div className="bg-slate-50 rounded-xl p-3 text-center">
                <p className="text-xs text-slate-400">Duration</p>
                <p className="font-bold">{video.durationFormatted || fmt(duration)}</p>
              </div>
              <div className="bg-slate-50 rounded-xl p-3 text-center">
                <p className="text-xs text-slate-400">Views</p>
                <p className="font-bold">{video.viewCount || 0}</p>
              </div>
              <div className="bg-slate-50 rounded-xl p-3 text-center">
                <p className="text-xs text-slate-400">Your Progress</p>
                <p className="font-bold">{progress ? `${progress.completionPercentage || 0}%` : '—'}</p>
              </div>
            </div>
            {progress && (
                <div className="p-3 bg-blue-50 border border-blue-100 rounded-xl mb-4">
                  <div className="flex justify-between text-sm mb-2">
                    <span className="text-blue-700 font-medium">Your progress</span>
                    <span className="text-blue-700">{progress.completionPercentage || 0}%</span>
                  </div>
                  <ProgressBar value={progress.completionPercentage || 0} showLabel={false} />
                </div>
            )}
            {assignmentId && (
                <Button
                    variant={progress?.isCompleted ? 'success' : 'primary'}
                    className="w-full"
                    loading={completing}
                    disabled={progress?.isCompleted}
                    onClick={handleComplete}
                >
                  {progress?.isCompleted ? '✅ Completed' : 'Mark as Complete'}
                </Button>
            )}
          </Card>
        </div>
      </div>
  );
}

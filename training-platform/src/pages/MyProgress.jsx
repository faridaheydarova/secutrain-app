import { useEffect, useState } from 'react';
import { progressAPI } from '../services/api';
import Navbar from '../components/Layout/Navbar';
import Card from '../components/Common/Card';
import StatCard from '../components/Common/StatCard';
import Badge from '../components/Common/Badge';
import ProgressBar from '../components/Common/ProgressBar';
import Loading from '../components/Common/Loading';

export default function MyProgress() {
  const [items, setItems] = useState([]);
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const [pRes, rRes] = await Promise.all([progressAPI.getMyAll(), progressAPI.getMyReport()]);
        setItems(pRes.data || []);
        setReport(rRes.data);
      } catch {}
      setLoading(false);
    })();
  }, []);

  if (loading) return <><Navbar title="My Progress" /><Loading /></>;

  return (
    <div>
      <Navbar title="My Progress" />
      <div className="p-6 space-y-6 animate-fadeIn">

        {report && (
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            <StatCard label="Overall Progress" value={`${(report.overallCompletionRate||0).toFixed(0)}%`} icon="📊" color="blue" />
            <StatCard label="Completed"        value={report.completedAssignments||0} icon="✅" color="green" />
            <StatCard label="In Progress"      value={report.inProgressAssignments||0} icon="▶️" color="amber" />
            <StatCard label="Time Watched"     value={`${Math.floor((report.totalWatchedDuration||0)/3600)}h`} icon="⏱" color="purple" />
          </div>
        )}

        <Card title={`Video Progress (${items.length})`}>
          {items.length === 0 ? (
            <p className="text-slate-400 text-center py-8">No progress tracked yet.</p>
          ) : (
            <div className="space-y-3">
              {items.map(p => (
                <div key={p.id} className="p-4 border border-slate-100 rounded-xl">
                  <div className="flex items-center justify-between mb-3">
                    <h4 className="font-semibold text-slate-800">{p.videoTitle}</h4>
                    <Badge label={p.status} />
                  </div>
                  <ProgressBar value={p.completionPercentage||0} />
                  <div className="flex justify-between mt-2 text-xs text-slate-400">
                    <span>Watched: {p.watchedDurationFormatted||'00:00:00'}</span>
                    <span>Attempts: {p.attempts||0}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>

      </div>
    </div>
  );
}

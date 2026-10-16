import { useEffect, useState } from 'react';
import { userAPI, campaignAPI, videoAPI, progressAPI } from '../services/api';
import Navbar from '../components/Layout/Navbar';
import Card from '../components/Common/Card';
import StatCard from '../components/Common/StatCard';
import ProgressBar from '../components/Common/ProgressBar';
import Loading from '../components/Common/Loading';

export default function AdminAnalytics() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([userAPI.getStats(), campaignAPI.getStats(), videoAPI.getStats()])
      .then(([u,c,v]) => setData({ u:u.data, c:c.data, v:v.data }))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <><Navbar title="Analytics" /><Loading /></>;

  return (
    <div>
      <Navbar title="Analytics Dashboard" />
      <div className="p-6 space-y-6 animate-fadeIn">

        {/* Top Stats */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard label="Total Users"     value={data?.u?.totalUsers||0}                 icon="👥" color="blue" sub={`${data?.u?.activeUsers||0} active`} />
          <StatCard label="Campaigns"       value={data?.c?.totalCampaigns||0}             icon="🎯" color="green" sub={`${data?.c?.activeCampaigns||0} active`} />
          <StatCard label="Videos"          value={data?.v?.totalVideos||0}                icon="🎬" color="amber" sub={data?.v?.totalStorageFormatted||'0 MB'} />
          <StatCard label="Total Views"     value={data?.v?.totalViews||data?.v?.videoViewCount||0}  icon="👁" color="purple" />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Campaign Stats */}
          <Card title="Campaign Overview">
            <div className="space-y-4">
              {[
                ['Total Campaigns',  data?.c?.totalCampaigns||0],
                ['Active Campaigns', data?.c?.activeCampaigns||0],
                ['Total Assigned',   data?.c?.totalAssignments||0],
                ['Completed',        data?.c?.completedAssignments||0],
              ].map(([l,v]) => (
                <div key={l} className="flex items-center justify-between py-2 border-b border-slate-50 last:border-0">
                  <span className="text-slate-600 text-sm">{l}</span>
                  <span className="font-bold text-slate-800">{v}</span>
                </div>
              ))}
              <div className="pt-2">
                <div className="flex justify-between text-sm mb-2">
                  <span className="text-slate-500">Overall Completion Rate</span>
                  <span className="font-bold">{(data?.c?.completionRate||0).toFixed(0)}%</span>
                </div>
                <ProgressBar value={data?.c?.completionRate||0} showLabel={false} />
              </div>
            </div>
          </Card>

          {/* Video Stats */}
          <Card title="Video Overview">
            <div className="space-y-4">
              {[
                ['Total Videos',   data?.v?.totalVideos||0],
                ['Active Videos',  data?.v?.activeVideos||0],
                ['Total Views',    data?.v?.totalViews||data?.v?.videoViewCount||0],
                ['Avg Duration',   `${Math.floor((data?.v?.averageDuration||0)/60)} min`],
                ['Storage Used',   data?.v?.totalStorageFormatted||'0 MB'],
              ].map(([l,v]) => (
                <div key={l} className="flex items-center justify-between py-2 border-b border-slate-50 last:border-0">
                  <span className="text-slate-600 text-sm">{l}</span>
                  <span className="font-bold text-slate-800">{v}</span>
                </div>
              ))}
            </div>
          </Card>
        </div>

        {/* User Stats */}
        {data?.u && (
          <Card title="User Overview">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              {[
                ['Total Users',    data.u.totalUsers||0,   'bg-blue-50 text-blue-600'],
                ['Active Users',   data.u.activeUsers||0,  'bg-emerald-50 text-emerald-600'],
                ['Admin Users',    data.u.adminUsers||0,   'bg-purple-50 text-purple-600'],
                ['Regular Users',  data.u.regularUsers||0, 'bg-amber-50 text-amber-600'],
              ].map(([l,v,cl]) => (
                <div key={l} className={`rounded-2xl p-4 ${cl}`}>
                  <p className="text-3xl font-bold">{v}</p>
                  <p className="text-sm mt-1 opacity-80">{l}</p>
                </div>
              ))}
            </div>
          </Card>
        )}

      </div>
    </div>
  );
}

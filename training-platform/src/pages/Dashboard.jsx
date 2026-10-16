import { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { campaignAPI, videoAPI, userAPI, progressAPI } from '../services/api';
import Navbar from '../components/Layout/Navbar';
import StatCard from '../components/Common/StatCard';
import Card from '../components/Common/Card';
import ProgressBar from '../components/Common/ProgressBar';
import Loading from '../components/Common/Loading';

export default function Dashboard() {
  const { user } = useSelector(s => s.auth);
  const navigate = useNavigate();
  const isAdmin = user?.role === 'ADMIN';

  const [stats, setStats] = useState(null);
  const [campaigns, setCampaigns] = useState([]);
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        if (isAdmin) {
          const campRes = await campaignAPI.getAll(0, 5);
          setCampaigns(campRes.data.content || []);

          const [uStats, cStats, vStats] = await Promise.all([
            userAPI.getStats(), campaignAPI.getStats(), videoAPI.getStats()
          ]);
          setStats({ users: uStats.data, campaigns: cStats.data, videos: vStats.data });
        } else {
          const r = await progressAPI.getMyReport();
          setReport(r.data);
          setCampaigns(r.data?.campaignProgress || []);
        }
      } catch (err) {
        console.error('Dashboard load error:', err);
      }
      setLoading(false);
    })();
  }, [isAdmin]);

  if (loading) return <><Navbar title="Dashboard" /><Loading /></>;

  return (
      <div>
        <Navbar title="Dashboard" />
        <div className="p-6 space-y-6 animate-fadeIn">

          <div className="rounded-2xl bg-gradient-to-r from-blue-600 to-blue-800 p-6 text-white flex items-center justify-between">
            <div>
              <h2 className="font-display text-2xl font-bold">Welcome back, {user?.username}! 👋</h2>
              <p className="text-blue-200 mt-1 text-sm">
                {isAdmin ? 'Here is your platform overview.' : 'Continue your learning journey.'}
              </p>
            </div>
            <div className="text-6xl opacity-20 font-display font-bold select-none">T</div>
          </div>

          {isAdmin && stats ? (
              <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                <StatCard label="Total Users"     value={stats.users?.totalUsers||0}            icon="👥" color="blue" />
                <StatCard label="Active Campaigns" value={stats.campaigns?.activeCampaigns||0}  icon="🎯" color="green" />
                <StatCard label="Total Videos"     value={stats.videos?.totalVideos||0}          icon="🎬" color="amber" />
                <StatCard label="Completion Rate"  value={`${(stats.campaigns?.completionRate||0).toFixed(0)}%`} icon="📈" color="purple" />
              </div>
          ) : report ? (
              <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                <StatCard label="My Progress"      value={`${(report.overallCompletionRate||0).toFixed(0)}%`} icon="📊" color="blue" />
                <StatCard label="Completed"        value={report.completedAssignments||0}   icon="✅" color="green" />
                <StatCard label="In Progress"      value={report.inProgressAssignments||0}  icon="▶️" color="amber" />
                <StatCard label="Time Watched"     value={`${Math.floor((report.totalWatchedDuration||0)/3600)}h`} icon="⏱" color="purple" />
              </div>
          ) : null}

          <Card title={isAdmin ? 'Recent Campaigns' : 'My Campaigns'}>
            {campaigns.length === 0 ? (
                <p className="text-slate-400 text-center py-8">
                  {isAdmin ? 'No campaigns yet.' : 'No campaigns assigned to you yet.'}
                </p>
            ) : (
                <div className="space-y-3">
                  {campaigns.map(c => (
                      <div key={c.campaignId || c.id} onClick={() => navigate(`/campaigns/${c.campaignId || c.id}`)}
                           className="p-4 border border-slate-100 rounded-xl hover:border-blue-200 hover:bg-blue-50/50 cursor-pointer transition-all group">
                        <div className="flex items-center justify-between mb-2">
                          <h4 className="font-semibold text-slate-800 group-hover:text-blue-700">{c.campaignName || c.name}</h4>
                          <span className="text-xs text-slate-400">{c.totalVideos||0} videos</span>
                        </div>
                        <ProgressBar value={c.completionPercentage ?? c.completionRate ?? 0} />
                      </div>
                  ))}
                </div>
            )}
          </Card>

        </div>
      </div>
  );
}
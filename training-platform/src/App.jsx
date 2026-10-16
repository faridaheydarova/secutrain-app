import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import MyProgress from './pages/MyProgress';
import Campaigns from './pages/Campaigns';
import CampaignCreate from './pages/CampaignCreate';
import CampaignDetail from './pages/CampaignDetail';
import CampaignEdit from './pages/CampaignEdit';
import Videos from './pages/Videos';
import VideoUpload from './pages/VideoUpload';
import VideoEdit from './pages/VideoEdit';
import VideoPlayer from './pages/VideoPlayer';
import AdminUsers from './pages/AdminUsers';
import AdminAnalytics from './pages/AdminAnalytics';
import NotFound from './pages/NotFound';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<Layout />}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="my-progress" element={<MyProgress />} />
        <Route path="campaigns" element={<Campaigns />} />
        <Route path="campaigns/create" element={<CampaignCreate />} />
        <Route path="campaigns/:id" element={<CampaignDetail />} />
        <Route path="campaigns/:id/edit" element={<CampaignEdit />} />
        <Route path="videos" element={<Videos />} />
        <Route path="videos/upload" element={<VideoUpload />} />
        <Route path="videos/:id/edit" element={<VideoEdit />} />
        <Route path="videos/:videoId/play" element={<VideoPlayer />} />
        <Route path="videos/:videoId/play/:assignmentId" element={<VideoPlayer />} />
        <Route path="admin/users" element={<AdminUsers />} />
        <Route path="admin/analytics" element={<AdminAnalytics />} />
      </Route>
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

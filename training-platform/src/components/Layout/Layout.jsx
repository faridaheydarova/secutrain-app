import { Outlet, Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import Sidebar from './Sidebar';

export default function Layout() {
  const { isAuthenticated } = useSelector(s => s.auth);
  const { sidebarOpen } = useSelector(s => s.ui);

  if (!isAuthenticated) return <Navigate to="/login" replace />;

  return (
    <div className="flex h-screen overflow-hidden bg-surface">
      {sidebarOpen && <Sidebar />}
      <div className="flex-1 overflow-y-auto">
        <Outlet />
      </div>
    </div>
  );
}

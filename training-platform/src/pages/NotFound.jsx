import { useNavigate } from 'react-router-dom';
import Button from '../components/Common/Button';

export default function NotFound() {
  const navigate = useNavigate();
  return (
    <div className="min-h-screen flex flex-col items-center justify-center text-center p-6">
      <div className="text-8xl font-display font-bold text-slate-200 mb-4">404</div>
      <h1 className="text-3xl font-display font-bold text-slate-800 mb-2">Page not found</h1>
      <p className="text-slate-500 mb-8">The page you are looking for does not exist.</p>
      <Button onClick={() => navigate('/dashboard')}>Go to Dashboard</Button>
    </div>
  );
}

import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { fetchVideos } from '../store/slices/videoSlice';
import { videoAPI } from '../services/api';
import Navbar from '../components/Layout/Navbar';
import Card from '../components/Common/Card';
import Button from '../components/Common/Button';
import Badge from '../components/Common/Badge';
import Loading from '../components/Common/Loading';
import Modal from '../components/Common/Modal';

export default function Videos() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { items, loading } = useSelector(s => s.videos);
  const [search, setSearch] = useState('');
  const [deleteId, setDeleteId] = useState(null);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => { dispatch(fetchVideos()); }, [dispatch]);

  const handleDelete = async () => {
    setDeleting(true);
    try { await videoAPI.delete(deleteId); dispatch(fetchVideos()); }
    catch {}
    setDeleting(false); setDeleteId(null);
  };

  const filtered = items.filter(v => v.title?.toLowerCase().includes(search.toLowerCase()));

  if (loading) return <><Navbar title="Videos" /><Loading /></>;

  return (
    <div>
      <Navbar title="Videos" />
      <div className="p-6 space-y-4 animate-fadeIn">
        <div className="flex gap-3">
          <input className="input flex-1" placeholder="Search videos..." value={search} onChange={e => setSearch(e.target.value)} />
          <Button onClick={() => navigate('/videos/upload')}>+ Upload</Button>
        </div>

        {filtered.length === 0 ? (
          <Card><p className="text-slate-400 text-center py-12">No videos found.</p></Card>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
            {filtered.map(v => (
              <Card key={v.id} className="flex flex-col">
                <div className="aspect-video bg-gradient-to-br from-slate-100 to-slate-200 rounded-xl mb-4 flex items-center justify-center text-5xl">
                  🎬
                </div>
                <div className="flex-1">
                  <div className="flex items-start justify-between gap-2 mb-2">
                    <h3 className="font-bold text-slate-800 line-clamp-2">{v.title}</h3>
                    <Badge label={v.isActive ? 'ACTIVE' : 'INACTIVE'} />
                  </div>
                  <p className="text-slate-500 text-sm mb-4 line-clamp-2">{v.description || '—'}</p>
                  <div className="grid grid-cols-3 gap-2 mb-4">
                    <div className="bg-slate-50 rounded-lg p-2 text-center">
                      <p className="text-xs text-slate-400">Duration</p>
                      <p className="text-sm font-semibold text-slate-700">{v.durationFormatted||'—'}</p>
                    </div>
                    <div className="bg-slate-50 rounded-lg p-2 text-center">
                      <p className="text-xs text-slate-400">Size</p>
                      <p className="text-sm font-semibold text-slate-700">{v.fileSizeFormatted||'—'}</p>
                    </div>
                    <div className="bg-slate-50 rounded-lg p-2 text-center">
                      <p className="text-xs text-slate-400">Views</p>
                      <p className="text-sm font-semibold text-slate-700">{v.viewCount||0}</p>
                    </div>
                  </div>
                </div>
                <div className="flex gap-2">
                  <Button variant="primary" size="sm" className="flex-1" onClick={() => navigate(`/videos/${v.id}/play`)}>▶ Play</Button>
                  <Button variant="secondary" size="sm" onClick={() => navigate(`/videos/${v.id}/edit`)}>✏</Button>
                  <Button variant="danger" size="sm" onClick={() => setDeleteId(v.id)}>🗑</Button>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>

      <Modal isOpen={!!deleteId} onClose={() => setDeleteId(null)} title="Delete Video?">
        <p className="text-slate-600 mb-6">This will permanently delete the video file and all related data.</p>
        <div className="flex gap-3">
          <Button variant="secondary" className="flex-1" onClick={() => setDeleteId(null)}>Cancel</Button>
          <Button variant="danger" className="flex-1" loading={deleting} onClick={handleDelete}>Delete</Button>
        </div>
      </Modal>
    </div>
  );
}

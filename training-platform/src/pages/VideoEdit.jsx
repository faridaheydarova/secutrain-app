import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { videoAPI } from '../services/api';
import Navbar from '../components/Layout/Navbar';
import Card from '../components/Common/Card';
import Button from '../components/Common/Button';
import Alert from '../components/Common/Alert';
import Loading from '../components/Common/Loading';

export default function VideoEdit() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState({ title:'', description:'', durationSeconds:'' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    videoAPI.getById(id).then(r => {
      const v = r.data;
      setForm({ title:v.title||'', description:v.description||'', durationSeconds:v.durationSeconds||'' });
      setLoading(false);
    }).catch(() => setLoading(false));
  }, [id]);

  const handle = async e => {
    e.preventDefault(); setError(''); setSaving(true);
    try { await videoAPI.update(id, form); navigate('/videos'); }
    catch (err) { setError(err.response?.data?.message || 'Failed to update'); }
    setSaving(false);
  };

  if (loading) return <><Navbar title="Edit Video" /><Loading /></>;

  return (
    <div>
      <Navbar title="Edit Video" />
      <div className="p-6 animate-fadeIn">
        <Card className="max-w-2xl mx-auto">
          <h2 className="font-display text-2xl font-bold mb-6">Edit Video</h2>
          {error && <Alert type="error" message={error} onClose={() => setError('')} />}
          <form onSubmit={handle} className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Title *</label>
              <input className="input" value={form.title} onChange={e => setForm({...form,title:e.target.value})} required />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Description</label>
              <textarea className="input resize-none" rows={4} value={form.description} onChange={e => setForm({...form,description:e.target.value})} />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Duration (seconds)</label>
              <input type="number" className="input" value={form.durationSeconds} onChange={e => setForm({...form,durationSeconds:e.target.value})} />
            </div>
            <div className="flex gap-3 pt-2">
              <Button variant="secondary" type="button" className="flex-1" onClick={() => navigate('/videos')}>Cancel</Button>
              <Button variant="primary" type="submit" className="flex-1" loading={saving}>Save Changes</Button>
            </div>
          </form>
        </Card>
      </div>
    </div>
  );
}

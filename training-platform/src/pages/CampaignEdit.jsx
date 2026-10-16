import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { campaignAPI } from '../services/api';
import Navbar from '../components/Layout/Navbar';
import Card from '../components/Common/Card';
import Button from '../components/Common/Button';
import Alert from '../components/Common/Alert';
import Loading from '../components/Common/Loading';

export default function CampaignEdit() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name:'', description:'', startDate:'', endDate:'' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    campaignAPI.getById(id).then(r => {
      const c = r.data;
      setForm({ name:c.name||'', description:c.description||'', startDate:c.startDate||'', endDate:c.endDate||'' });
      setLoading(false);
    }).catch(() => setLoading(false));
  }, [id]);

  const handle = async e => {
    e.preventDefault(); setError(''); setSaving(true);
    try { await campaignAPI.update(id, form); navigate(`/campaigns/${id}`); }
    catch (err) { setError(err.response?.data?.message || 'Failed to update'); }
    setSaving(false);
  };

  if (loading) return <><Navbar title="Edit Campaign" /><Loading /></>;

  return (
    <div>
      <Navbar title="Edit Campaign" />
      <div className="p-6 animate-fadeIn">
        <Card className="max-w-2xl mx-auto">
          <h2 className="font-display text-2xl font-bold mb-6">Edit Campaign</h2>
          {error && <Alert type="error" message={error} onClose={() => setError('')} />}
          <form onSubmit={handle} className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Campaign Name *</label>
              <input className="input" value={form.name} onChange={e => setForm({...form,name:e.target.value})} required />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Description</label>
              <textarea className="input resize-none" rows={4} value={form.description} onChange={e => setForm({...form,description:e.target.value})} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">Start Date</label>
                <input type="date" className="input" value={form.startDate} onChange={e => setForm({...form,startDate:e.target.value})} />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">End Date</label>
                <input type="date" className="input" value={form.endDate} onChange={e => setForm({...form,endDate:e.target.value})} />
              </div>
            </div>
            <div className="flex gap-3 pt-2">
              <Button variant="secondary" type="button" className="flex-1" onClick={() => navigate(`/campaigns/${id}`)}>Cancel</Button>
              <Button variant="primary" type="submit" className="flex-1" loading={saving}>Save Changes</Button>
            </div>
          </form>
        </Card>
      </div>
    </div>
  );
}

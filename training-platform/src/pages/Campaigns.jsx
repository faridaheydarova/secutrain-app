import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { fetchCampaigns } from '../store/slices/campaignSlice';
import { campaignAPI, progressAPI } from '../services/api';
import Navbar from '../components/Layout/Navbar';
import Card from '../components/Common/Card';
import Button from '../components/Common/Button';
import Badge from '../components/Common/Badge';
import ProgressBar from '../components/Common/ProgressBar';
import Loading from '../components/Common/Loading';
import Modal from '../components/Common/Modal';

export default function Campaigns() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { user } = useSelector(s => s.auth);
  const isAdmin = user?.role === 'ADMIN';

  const { items, loading: adminLoading } = useSelector(s => s.campaigns);
  const [userCampaigns, setUserCampaigns] = useState([]);
  const [userLoading, setUserLoading] = useState(true);
  const [deleteId, setDeleteId] = useState(null);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    if (isAdmin) {
      dispatch(fetchCampaigns());
    } else {
      (async () => {
        try {
          const r = await campaignAPI.getMyAssigned();
          setUserCampaigns(r.data || []);
        } catch (err) {
          console.error('My campaigns load error:', err);
        }
        setUserLoading(false);
      })();
    }
  }, [dispatch, isAdmin]);

  const handleDelete = async () => {
    setDeleting(true);
    try { await campaignAPI.delete(deleteId); dispatch(fetchCampaigns()); }
    catch (err) { console.error(err); }
    setDeleting(false); setDeleteId(null);
  };

  const loading = isAdmin ? adminLoading : userLoading;
  if (loading) return <><Navbar title="Campaigns" /><Loading /></>;

  const campaigns = isAdmin ? (items || []) : (userCampaigns || []);

  return (
      <div>
        <Navbar title="Campaigns" />
        <div className="p-6 space-y-4 animate-fadeIn">
          <div className="flex justify-between items-center">
            <p className="text-slate-500 text-sm">
              {campaigns.length} {isAdmin ? 'campaigns found' : 'campaigns assigned to you'}
            </p>
            {isAdmin && (
                <Button onClick={() => navigate('/campaigns/create')}>+ New Campaign</Button>
            )}
          </div>

          {campaigns.length === 0 ? (
              <Card>
                <p className="text-slate-400 text-center py-12">
                  {isAdmin ? 'No campaigns yet. Create one!' : 'No campaigns assigned to you yet.'}
                </p>
              </Card>
          ) : (
              <div className="grid gap-4">
                {campaigns.map(c => (
                    <Card key={c.id}>
                      <div className="flex gap-4">
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-3 mb-1">
                            <h3 className="font-bold text-slate-800 text-lg">{c.name}</h3>
                            <Badge label={c.isActive ? 'ACTIVE' : 'INACTIVE'} />
                          </div>
                          <p className="text-slate-500 text-sm mb-4 line-clamp-2">{c.description}</p>

                          <div className="grid grid-cols-3 gap-4 mb-4">
                            <div className="bg-slate-50 rounded-xl p-3 text-center">
                              <p className="text-2xl font-bold text-slate-800">{c.totalVideos || 0}</p>
                              <p className="text-xs text-slate-400 mt-0.5">Videos</p>
                            </div>
                            <div className="bg-slate-50 rounded-xl p-3 text-center">
                              <p className="text-2xl font-bold text-slate-800">{isAdmin ? (c.totalAssigned || 0) : (c.totalCompleted || 0)}</p>
                              <p className="text-xs text-slate-400 mt-0.5">{isAdmin ? 'Assigned' : 'Completed'}</p>
                            </div>
                            <div className="bg-slate-50 rounded-xl p-3 text-center">
                              <p className="text-2xl font-bold text-slate-800">{(c.completionRate || 0).toFixed(0)}%</p>
                              <p className="text-xs text-slate-400 mt-0.5">Progress</p>
                            </div>
                          </div>

                          <ProgressBar value={c.completionRate || 0} />
                        </div>

                        <div className="flex flex-col gap-2 shrink-0">
                          <Button variant="secondary" size="sm" onClick={() => navigate(`/campaigns/${c.id}`)}>View</Button>
                          {isAdmin && (
                              <>
                                <Button variant="secondary" size="sm" onClick={() => navigate(`/campaigns/${c.id}/edit`)}>Edit</Button>
                                <Button variant="danger" size="sm" onClick={() => setDeleteId(c.id)}>Delete</Button>
                              </>
                          )}
                        </div>
                      </div>
                    </Card>
                ))}
              </div>
          )}
        </div>

        {isAdmin && (
            <Modal isOpen={!!deleteId} onClose={() => setDeleteId(null)} title="Delete Campaign?">
              <p className="text-slate-600 mb-6">This will permanently delete the campaign and all its assignments.</p>
              <div className="flex gap-3">
                <Button variant="secondary" className="flex-1" onClick={() => setDeleteId(null)}>Cancel</Button>
                <Button variant="danger" className="flex-1" loading={deleting} onClick={handleDelete}>Delete</Button>
              </div>
            </Modal>
        )}
      </div>
  );
}
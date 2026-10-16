import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { campaignAPI, videoAPI, userAPI } from '../services/api';
import Navbar from '../components/Layout/Navbar';
import Card from '../components/Common/Card';
import Button from '../components/Common/Button';
import Badge from '../components/Common/Badge';
import ProgressBar from '../components/Common/ProgressBar';
import Loading from '../components/Common/Loading';
import Modal from '../components/Common/Modal';

export default function CampaignDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useSelector(s => s.auth);
  const isAdmin = user?.role === 'ADMIN';

  const [campaign, setCampaign] = useState(null);
  const [allVideos, setAllVideos] = useState([]);
  const [allUsers, setAllUsers] = useState([]);
  const [selected, setSelected] = useState([]);
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [showAdd, setShowAdd] = useState(false);
  const [showAssign, setShowAssign] = useState(false);
  const [assigning, setAssigning] = useState(false);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    try {
      const cRes = await campaignAPI.getById(id);
      setCampaign(cRes.data);
      if (isAdmin) {
        const vRes = await videoAPI.getAll(0, 100);
        setAllVideos(vRes.data.content || []);
      }
    } catch (err) {
      console.error('Campaign load error:', err);
    }
    setLoading(false);
  };

  useEffect(() => { load(); }, [id]);

  const addVideos = async () => {
    try {
      await campaignAPI.addVideos(id, selected);
      setShowAdd(false);
      setSelected([]);
      load();
    } catch (err) {
      console.error(err);
    }
  };

  const removeVideo = async vid => {
    try {
      await campaignAPI.removeVideo(id, vid);
      load();
    } catch (err) {
      console.error(err);
    }
  };

  const toggle = vid => setSelected(s => s.includes(vid) ? s.filter(x => x !== vid) : [...s, vid]);

  const openAssign = async () => {
    setShowAssign(true);
    try {
      const uRes = await userAPI.getAll(0, 100);
      setAllUsers(uRes.data.content || []);
    } catch (err) {
      console.error(err);
    }
  };

  const toggleUser = uid => setSelectedUsers(s => s.includes(uid) ? s.filter(x => x !== uid) : [...s, uid]);

  const assignUsers = async () => {
    setAssigning(true);
    try {
      await campaignAPI.assign(id, selectedUsers);
      setShowAssign(false);
      setSelectedUsers([]);
      load();
    } catch (err) {
      console.error(err);
    }
    setAssigning(false);
  };

  if (loading) return <><Navbar title="Campaign" /><Loading /></>;
  if (!campaign) return <><Navbar title="Campaign" /><p className="p-6">Not found</p></>;

  return (
      <div>
        <Navbar title={campaign.name} />
        <div className="p-6 space-y-6 animate-fadeIn">
          <div className="flex gap-3">
            <Button variant="secondary" onClick={() => navigate(-1)}>← Back</Button>
            {isAdmin && (
                <Button variant="secondary" onClick={() => navigate(`/campaigns/${id}/edit`)}>Edit</Button>
            )}
          </div>

          <Card>
            <div className="flex items-start justify-between mb-4">
              <div>
                <h2 className="font-display text-2xl font-bold mb-2">{campaign.name}</h2>
                <p className="text-slate-500">{campaign.description}</p>
              </div>
              <Badge label={campaign.isActive ? 'ACTIVE' : 'INACTIVE'} />
            </div>

            <div className="grid grid-cols-4 gap-4 pt-4 border-t border-slate-100">
              {[
                ['Videos', campaign.totalVideos || 0, '🎬'],
                ['Assigned', campaign.totalAssigned || 0, '👥'],
                ['Completed', campaign.totalCompleted || 0, '✅'],
                ['Completion', `${(campaign.completionRate || 0).toFixed(0)}%`, '📈']
              ].map(([l, v, i]) => (
                  <div key={l} className="text-center p-3 bg-slate-50 rounded-xl">
                    <div className="text-2xl mb-1">{i}</div>
                    <div className="text-2xl font-bold text-slate-800">{v}</div>
                    <div className="text-xs text-slate-400">{l}</div>
                  </div>
              ))}
            </div>
            <div className="mt-4"><ProgressBar value={campaign.completionRate || 0} /></div>
          </Card>

          <div className="flex justify-between items-center">
            <h3 className="font-display text-xl font-bold">Videos in Campaign</h3>
            {isAdmin && (
                <div className="flex gap-3">
                  <Button variant="secondary" onClick={openAssign}>👥 Assign Users</Button>
                  <Button onClick={() => setShowAdd(true)}>+ Add Videos</Button>
                </div>
            )}
          </div>

          {(campaign.videos || []).length === 0 ? (
              <Card><p className="text-slate-400 text-center py-8">No videos yet.</p></Card>
          ) : (
              <div className="space-y-3">
                {(campaign.videos || []).map(v => (
                    <Card key={v.videoId}>
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 bg-blue-100 rounded-xl flex items-center justify-center text-xl">🎬</div>
                          <div>
                            <h4 className="font-semibold">{v.title}</h4>
                            <p className="text-xs text-slate-400">
                              {v.durationSeconds ? `${Math.floor(v.durationSeconds / 60)}:${String(v.durationSeconds % 60).padStart(2, '0')}` : '—'}
                            </p>
                          </div>
                        </div>
                        <div className="flex gap-2">
                          {isAdmin ? (
                              <Button variant="danger" size="sm" onClick={() => removeVideo(v.videoId)}>Remove</Button>
                          ) : (
                              <Button variant="primary" size="sm" onClick={() => navigate(`/videos/${v.videoId}/play/${campaign.assignmentId}`)}>▶ Watch</Button>
                          )}
                        </div>
                      </div>
                    </Card>
                ))}
              </div>
          )}

          {isAdmin && (
              <>
                <Modal isOpen={showAdd} onClose={() => setShowAdd(false)} title="Add Videos">
                  <div className="space-y-2 max-h-72 overflow-y-auto mb-4">
                    {allVideos.map(v => (
                        <label key={v.id} className="flex items-center gap-3 p-3 border rounded-xl hover:bg-slate-50 cursor-pointer">
                          <input type="checkbox" checked={selected.includes(v.id)} onChange={() => toggle(v.id)} className="rounded" />
                          <div>
                            <p className="font-medium text-sm">{v.title}</p>
                            <p className="text-xs text-slate-400">{v.durationFormatted}</p>
                          </div>
                        </label>
                    ))}
                  </div>
                  <div className="flex gap-3">
                    <Button variant="secondary" className="flex-1" onClick={() => setShowAdd(false)}>Cancel</Button>
                    <Button variant="primary" className="flex-1" onClick={addVideos} disabled={selected.length === 0}>
                      Add {selected.length > 0 ? `(${selected.length})` : ''}
                    </Button>
                  </div>
                </Modal>

                <Modal isOpen={showAssign} onClose={() => setShowAssign(false)} title="Assign Users to Campaign">
                  <div className="space-y-2 max-h-72 overflow-y-auto mb-4">
                    {allUsers.length === 0 ? (
                        <p className="text-slate-400 text-center py-4">No users found.</p>
                    ) : allUsers.map(u => (
                        <label key={u.id} className="flex items-center gap-3 p-3 border rounded-xl hover:bg-slate-50 cursor-pointer">
                          <input type="checkbox" checked={selectedUsers.includes(u.id)} onChange={() => toggleUser(u.id)} className="rounded" />
                          <div>
                            <p className="font-medium text-sm">{u.username} <span className="text-xs text-slate-400">({u.role})</span></p>
                            <p className="text-xs text-slate-400">{u.email}</p>
                          </div>
                        </label>
                    ))}
                  </div>
                  <div className="flex gap-3">
                    <Button variant="secondary" className="flex-1" onClick={() => setShowAssign(false)}>Cancel</Button>
                    <Button variant="primary" className="flex-1" loading={assigning} onClick={assignUsers} disabled={selectedUsers.length === 0}>
                      Assign {selectedUsers.length > 0 ? `(${selectedUsers.length})` : ''}
                    </Button>
                  </div>
                </Modal>
              </>
          )}
        </div>
      </div>
  );
}
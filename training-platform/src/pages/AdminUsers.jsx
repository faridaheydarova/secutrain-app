import { useEffect, useState } from 'react';
import { userAPI } from '../services/api';
import Navbar from '../components/Layout/Navbar';
import Card from '../components/Common/Card';
import Button from '../components/Common/Button';
import Badge from '../components/Common/Badge';
import Loading from '../components/Common/Loading';
import Modal from '../components/Common/Modal';
import Alert from '../components/Common/Alert';

export default function AdminUsers() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [deleteId, setDeleteId] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState('');
  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState({ username:'', email:'', password:'', firstName:'', lastName:'' });
  const [creating, setCreating] = useState(false);

  const load = async () => {
    try { const r = await userAPI.getAll(0,50); setUsers(r.data.content||[]); }
    catch {}
    setLoading(false);
  };

  useEffect(() => { load(); }, []);

  const handleDelete = async () => {
    setDeleting(true);
    try { await userAPI.delete(deleteId); load(); }
    catch {}
    setDeleting(false); setDeleteId(null);
  };

  const handleRoleChange = async (id, role) => {
    try { await userAPI.changeRole(id, role); load(); }
    catch {}
  };

  const handleCreate = async e => {
    e.preventDefault(); setError(''); setCreating(true);
    try { await userAPI.create(form); setShowCreate(false); setForm({username:'',email:'',password:'',firstName:'',lastName:''}); load(); }
    catch (err) { setError(err.response?.data?.message || 'Failed to create'); }
    setCreating(false);
  };

  if (loading) return <><Navbar title="Users" /><Loading /></>;

  return (
    <div>
      <Navbar title="User Management" />
      <div className="p-6 space-y-4 animate-fadeIn">
        <div className="flex justify-between items-center">
          <p className="text-slate-500 text-sm">{users.length} users</p>
          <Button onClick={() => setShowCreate(true)}>+ Add User</Button>
        </div>

        <Card>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-slate-100">
                  {['User','Email','Role','Status','Actions'].map(h => (
                    <th key={h} className="text-left pb-3 text-xs font-semibold text-slate-400 uppercase tracking-wider pr-4">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50">
                {users.map(u => (
                  <tr key={u.id} className="hover:bg-slate-50/50 transition">
                    <td className="py-4 pr-4">
                      <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center font-bold text-sm">
                          {u.username?.[0]?.toUpperCase()}
                        </div>
                        <div>
                          <p className="font-semibold text-sm">{u.username}</p>
                          <p className="text-xs text-slate-400">{u.firstName} {u.lastName}</p>
                        </div>
                      </div>
                    </td>
                    <td className="py-4 pr-4 text-sm text-slate-600">{u.email}</td>
                    <td className="py-4 pr-4">
                      <select value={u.role} onChange={e => handleRoleChange(u.id, e.target.value)}
                        className="text-xs border border-slate-200 rounded-lg px-2 py-1 bg-white focus:outline-none focus:ring-1 focus:ring-blue-400">
                        <option value="USER">USER</option>
                        <option value="ADMIN">ADMIN</option>
                      </select>
                    </td>
                    <td className="py-4 pr-4"><Badge label={u.isActive ? 'ACTIVE' : 'INACTIVE'} /></td>
                    <td className="py-4">
                      <div className="flex gap-2">
                        <Button variant="secondary" size="sm" onClick={() => userAPI.toggleStatus(u.id, !u.isActive).then(load)}>
                          {u.isActive ? 'Deactivate' : 'Activate'}
                        </Button>
                        <Button variant="danger" size="sm" onClick={() => setDeleteId(u.id)}>Delete</Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      </div>

      <Modal isOpen={showCreate} onClose={() => setShowCreate(false)} title="Create User">
        {error && <Alert type="error" message={error} onClose={() => setError('')} />}
        <form onSubmit={handleCreate} className="space-y-3">
          <div className="grid grid-cols-2 gap-3">
            <div><label className="block text-xs font-medium text-slate-600 mb-1">First Name</label><input className="input" value={form.firstName} onChange={e => setForm({...form,firstName:e.target.value})} /></div>
            <div><label className="block text-xs font-medium text-slate-600 mb-1">Last Name</label><input className="input" value={form.lastName} onChange={e => setForm({...form,lastName:e.target.value})} /></div>
          </div>
          <div><label className="block text-xs font-medium text-slate-600 mb-1">Username *</label><input className="input" value={form.username} onChange={e => setForm({...form,username:e.target.value})} required /></div>
          <div><label className="block text-xs font-medium text-slate-600 mb-1">Email *</label><input type="email" className="input" value={form.email} onChange={e => setForm({...form,email:e.target.value})} required /></div>
          <div><label className="block text-xs font-medium text-slate-600 mb-1">Password *</label><input type="password" className="input" value={form.password} onChange={e => setForm({...form,password:e.target.value})} required /></div>
          <div className="flex gap-3 pt-2">
            <Button variant="secondary" type="button" className="flex-1" onClick={() => setShowCreate(false)}>Cancel</Button>
            <Button variant="primary" type="submit" className="flex-1" loading={creating}>Create</Button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={!!deleteId} onClose={() => setDeleteId(null)} title="Delete User?">
        <p className="text-slate-600 mb-6">This action cannot be undone.</p>
        <div className="flex gap-3">
          <Button variant="secondary" className="flex-1" onClick={() => setDeleteId(null)}>Cancel</Button>
          <Button variant="danger" className="flex-1" loading={deleting} onClick={handleDelete}>Delete</Button>
        </div>
      </Modal>
    </div>
  );
}

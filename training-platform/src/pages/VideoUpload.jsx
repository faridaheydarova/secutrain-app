import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { videoAPI } from '../services/api';
import Navbar from '../components/Layout/Navbar';
import Card from '../components/Common/Card';
import Button from '../components/Common/Button';
import Alert from '../components/Common/Alert';

export default function VideoUpload() {
  const navigate = useNavigate();
  const inputRef = useRef();
  const [file, setFile] = useState(null);
  const [form, setForm] = useState({ title:'', description:'', durationSeconds:'' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [dragging, setDragging] = useState(false);

  const handleFile = f => {
    if (!f) return;
    setFile(f);
    if (!form.title) setForm(p => ({...p, title: f.name.replace(/\.[^.]+$/, '')}));
  };

  const handleDrop = e => {
    e.preventDefault(); setDragging(false);
    handleFile(e.dataTransfer.files[0]);
  };

  const handle = async e => {
    e.preventDefault(); setError(''); setLoading(true); setProgress(0);
    if (!file) { setError('Please select a video file'); setLoading(false); return; }
    try {
      const fd = new FormData();
      fd.append('file', file);
      fd.append('title', form.title);
      fd.append('description', form.description);
      fd.append('durationSeconds', form.durationSeconds || '0');
      await videoAPI.upload(fd);
      navigate('/videos');
    } catch (err) {
      setError(err.response?.data?.message || 'Upload failed');
    }
    setLoading(false);
  };

  return (
    <div>
      <Navbar title="Upload Video" />
      <div className="p-6 animate-fadeIn">
        <Card className="max-w-2xl mx-auto">
          <h2 className="font-display text-2xl font-bold mb-6">Upload Video</h2>
          {error && <Alert type="error" message={error} onClose={() => setError('')} />}

          <form onSubmit={handle} className="space-y-5">
            <div
              onDragOver={e => { e.preventDefault(); setDragging(true); }}
              onDragLeave={() => setDragging(false)}
              onDrop={handleDrop}
              onClick={() => inputRef.current?.click()}
              className={`border-2 border-dashed rounded-2xl p-10 text-center cursor-pointer transition-all ${dragging ? 'border-blue-500 bg-blue-50' : file ? 'border-emerald-400 bg-emerald-50' : 'border-slate-200 hover:border-blue-400 hover:bg-blue-50/50'}`}
            >
              <input ref={inputRef} type="file" accept="video/*" className="hidden" onChange={e => handleFile(e.target.files[0])} />
              <div className="text-4xl mb-3">{file ? '✅' : '🎬'}</div>
              <p className="font-semibold text-slate-700">{file ? file.name : 'Drop video here or click to browse'}</p>
              <p className="text-slate-400 text-sm mt-1">{file ? `${(file.size/1024/1024).toFixed(1)} MB` : 'MP4, AVI, MOV, MKV · Max 500MB'}</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Title *</label>
              <input className="input" value={form.title} onChange={e => setForm({...form,title:e.target.value})} placeholder="Video title" required />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Description</label>
              <textarea className="input resize-none" rows={3} value={form.description} onChange={e => setForm({...form,description:e.target.value})} placeholder="Video description..." />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Duration (seconds)</label>
              <input type="number" className="input" value={form.durationSeconds} onChange={e => setForm({...form,durationSeconds:e.target.value})} placeholder="e.g. 3600 (1 hour)" />
            </div>

            {loading && (
              <div className="space-y-2">
                <div className="h-2 bg-slate-100 rounded-full overflow-hidden">
                  <div className="h-full bg-blue-500 rounded-full transition-all duration-500 animate-pulse" style={{width:'60%'}} />
                </div>
                <p className="text-sm text-slate-500 text-center">Uploading… please wait</p>
              </div>
            )}

            <div className="flex gap-3 pt-2">
              <Button variant="secondary" type="button" className="flex-1" onClick={() => navigate('/videos')}>Cancel</Button>
              <Button variant="primary" type="submit" className="flex-1" loading={loading}>Upload Video</Button>
            </div>
          </form>
        </Card>
      </div>
    </div>
  );
}

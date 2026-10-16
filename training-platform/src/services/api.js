import axios from 'axios';

const BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({ baseURL: BASE, headers: { 'Content-Type': 'application/json' } });

api.interceptors.request.use(cfg => {
  const token = localStorage.getItem('accessToken');
  if (token) cfg.headers.Authorization = `Bearer ${token}`;
  return cfg;
});

api.interceptors.response.use(r => r, err => {
  if (err.response?.status === 401) {
    localStorage.clear();
    window.location.href = '/login';
  }
  return Promise.reject(err);
});

export const authAPI = {
  login: d => api.post('/auth/login', d),
  register: d => api.post('/auth/register', d),
};

export const userAPI = {
  getAll: (p=0,s=10) => api.get(`/admin/users?page=${p}&size=${s}`),
  getById: id => api.get(`/admin/users/${id}`),
  create: d => api.post('/admin/users', d),
  update: (id,d) => api.put(`/admin/users/${id}`, d),
  delete: id => api.delete(`/admin/users/${id}`),
  changeRole: (id,role) => api.put(`/admin/users/${id}/role?newRole=${role}`),
  toggleStatus: (id,active) => api.put(`/admin/users/${id}/status?isActive=${active}`),
  getStats: () => api.get('/admin/users/stats/overview'),
};

export const campaignAPI = {
  getAll: (p=0,s=10) => api.get(`/admin/campaigns?page=${p}&size=${s}`),
  getById: id => api.get(`/admin/campaigns/${id}`),
  create: d => api.post('/admin/campaigns', d),
  update: (id,d) => api.put(`/admin/campaigns/${id}`, d),
  delete: id => api.delete(`/admin/campaigns/${id}`),
  addVideos: (id,videoIds) => api.post(`/admin/campaigns/${id}/videos`, { videoIds }),
  removeVideo: (id,vId) => api.delete(`/admin/campaigns/${id}/videos/${vId}`),
  assign: (id,userIds) => api.post(`/admin/campaigns/${id}/assign`, { userIds }),
  getActive: () => api.get('/admin/campaigns/active/list'),
  getStats: () => api.get('/admin/campaigns/stats/overview'),
  getMyAssigned: () => api.get('/admin/campaigns/my/assigned'),
};

export const videoAPI = {
  upload: fd => api.post('/admin/videos', fd, { headers: { 'Content-Type': 'multipart/form-data' } }),
  getAll: (p=0,s=10) => api.get(`/admin/videos?page=${p}&size=${s}`),
  getById: id => api.get(`/admin/videos/${id}`),
  update: (id,d) => api.put(`/admin/videos/${id}`, d),
  delete: id => api.delete(`/admin/videos/${id}`),
  getActive: () => api.get('/admin/videos/active/list'),
  searchTitle: q => api.get(`/admin/videos/search/title?title=${encodeURIComponent(q)}`),
  getStats: () => api.get('/admin/videos/stats/overview'),
  activate: id => api.put(`/admin/videos/${id}/activate`),
  deactivate: id => api.put(`/admin/videos/${id}/deactivate`),
  stream: id => api.get(`/admin/videos/${id}/stream`, { responseType: 'blob' }),
};

export const progressAPI = {
  start: (aId,vId) => api.post(`/user/progress/${aId}/${vId}/start`),
  update: (aId,vId,d) => api.put(`/user/progress/${aId}/${vId}/update`, d),
  complete: (aId,vId,d) => api.post(`/user/progress/${aId}/${vId}/complete`, d),
  get: (aId,vId) => api.get(`/user/progress/${aId}/${vId}`),
  getMyAll: () => api.get('/user/progress/my-progress/all'),
  getMyReport: () => api.get('/user/progress/report/my-report'),
  getCampaignDetails: cId => api.get(`/user/progress/campaign/${cId}/details`),
  getResume: (aId,vId) => api.post(`/user/progress/${aId}/${vId}/resume`),
  getCampaignReport: cId => api.get(`/user/progress/admin/campaign/${cId}/report`),
  getAtRisk: (days=7) => api.get(`/user/progress/admin/at-risk?daysInactive=${days}`),
};

export default api;

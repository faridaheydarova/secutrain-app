import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { videoAPI } from '../../services/api';

export const fetchVideos = createAsyncThunk('videos/fetchAll', async ({ page=0, size=10 }={}) => {
  const r = await videoAPI.getAll(page, size);
  return r.data;
});

const slice = createSlice({
  name: 'videos',
  initialState: { items: [], total: 0, loading: false, error: null },
  reducers: {},
  extraReducers: b => {
    b.addCase(fetchVideos.pending, s => { s.loading = true; })
     .addCase(fetchVideos.fulfilled, (s, a) => {
       s.loading = false;
       s.items = a.payload.content || [];
       s.total = a.payload.totalElements || 0;
     })
     .addCase(fetchVideos.rejected, (s, a) => { s.loading = false; s.error = a.error.message; });
  },
});
export default slice.reducer;

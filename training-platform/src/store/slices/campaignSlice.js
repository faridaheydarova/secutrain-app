import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { campaignAPI } from '../../services/api';

export const fetchCampaigns = createAsyncThunk('campaigns/fetchAll', async ({ page=0, size=10 }={}) => {
  const r = await campaignAPI.getAll(page, size);
  return r.data;
});

const slice = createSlice({
  name: 'campaigns',
  initialState: { items: [], total: 0, loading: false, error: null },
  reducers: {},
  extraReducers: b => {
    b.addCase(fetchCampaigns.pending, s => { s.loading = true; })
     .addCase(fetchCampaigns.fulfilled, (s, a) => {
       s.loading = false;
       s.items = a.payload.content || [];
       s.total = a.payload.totalElements || 0;
     })
     .addCase(fetchCampaigns.rejected, (s, a) => { s.loading = false; s.error = a.error.message; });
  },
});
export default slice.reducer;

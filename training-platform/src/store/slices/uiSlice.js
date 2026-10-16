import { createSlice } from '@reduxjs/toolkit';

const slice = createSlice({
  name: 'ui',
  initialState: { sidebarOpen: true },
  reducers: {
    toggleSidebar(s) { s.sidebarOpen = !s.sidebarOpen; },
  },
});
export const { toggleSidebar } = slice.actions;
export default slice.reducer;

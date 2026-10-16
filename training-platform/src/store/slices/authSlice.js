import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { authAPI } from '../../services/api';

export const loginUser = createAsyncThunk('auth/login', async (creds, { rejectWithValue }) => {
  try {
    const res = await authAPI.login(creds);
    const { accessToken, refreshToken, username, role } = res.data;
    localStorage.setItem('accessToken', accessToken);
    if (refreshToken) localStorage.setItem('refreshToken', refreshToken);
    return { username, role, accessToken };
  } catch (e) {
    return rejectWithValue(e.response?.data?.message || 'Login failed');
  }
});

const saved = (() => { try { return JSON.parse(localStorage.getItem('authUser')); } catch { return null; } })();

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    user: saved,
    isAuthenticated: !!localStorage.getItem('accessToken'),
    loading: false,
    error: null,
  },
  reducers: {
    logout(state) {
      state.user = null; state.isAuthenticated = false;
      localStorage.clear();
    },
  },
  extraReducers: b => {
    b.addCase(loginUser.pending, s => { s.loading = true; s.error = null; })
     .addCase(loginUser.fulfilled, (s, a) => {
       s.loading = false; s.isAuthenticated = true;
       s.user = { username: a.payload.username, role: a.payload.role };
       localStorage.setItem('authUser', JSON.stringify(s.user));
     })
     .addCase(loginUser.rejected, (s, a) => { s.loading = false; s.error = a.payload; });
  },
});

export const { logout } = authSlice.actions;
export default authSlice.reducer;

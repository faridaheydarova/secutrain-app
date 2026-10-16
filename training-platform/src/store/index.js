import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';
import campaignReducer from './slices/campaignSlice';
import videoReducer from './slices/videoSlice';
import uiReducer from './slices/uiSlice';

export default configureStore({
  reducer: { auth: authReducer, campaigns: campaignReducer, videos: videoReducer, ui: uiReducer },
});

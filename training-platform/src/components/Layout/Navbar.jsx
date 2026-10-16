import { useDispatch } from 'react-redux';
import { toggleSidebar } from '../../store/slices/uiSlice';

export default function Navbar({ title }) {
  const dispatch = useDispatch();
  return (
    <header className="bg-white border-b border-slate-100 px-6 py-4 flex items-center gap-4 sticky top-0 z-10">
      <button onClick={() => dispatch(toggleSidebar())}
        className="w-9 h-9 flex items-center justify-center rounded-xl hover:bg-slate-100 transition">
        <svg className="w-5 h-5 text-slate-600" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>
      {title && <h1 className="font-display font-bold text-slate-800 text-xl">{title}</h1>}
    </header>
  );
}

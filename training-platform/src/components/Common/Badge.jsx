const map = {
  ACTIVE:'bg-emerald-100 text-emerald-700', INACTIVE:'bg-slate-100 text-slate-500',
  COMPLETED:'bg-emerald-100 text-emerald-700', IN_PROGRESS:'bg-blue-100 text-blue-700',
  NOT_STARTED:'bg-slate-100 text-slate-500', ADMIN:'bg-purple-100 text-purple-700',
  USER:'bg-sky-100 text-sky-700',
};
export default function Badge({ label }) {
  return <span className={`badge ${map[label]||'bg-slate-100 text-slate-600'}`}>{label?.replace('_',' ')}</span>;
}

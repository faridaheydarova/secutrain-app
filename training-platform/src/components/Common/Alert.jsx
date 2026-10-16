const cfg = {
  error:   { bg:'bg-red-50 border-red-200 text-red-800',   icon:'✗' },
  success: { bg:'bg-emerald-50 border-emerald-200 text-emerald-800', icon:'✓' },
  info:    { bg:'bg-blue-50 border-blue-200 text-blue-800', icon:'ℹ' },
  warning: { bg:'bg-amber-50 border-amber-200 text-amber-800', icon:'⚠' },
};
export default function Alert({ type='info', message, onClose }) {
  const c = cfg[type];
  return (
    <div className={`border rounded-xl px-4 py-3 flex items-start gap-3 mb-4 ${c.bg}`}>
      <span className="font-bold mt-0.5">{c.icon}</span>
      <span className="flex-1 text-sm">{message}</span>
      {onClose && <button onClick={onClose} className="text-lg leading-none opacity-60 hover:opacity-100">×</button>}
    </div>
  );
}

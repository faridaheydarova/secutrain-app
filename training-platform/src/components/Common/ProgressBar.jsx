export default function ProgressBar({ value=0, showLabel=true }) {
  const pct = Math.min(100, Math.max(0, value));
  const color = pct===100 ? 'bg-emerald-500' : pct>50 ? 'bg-blue-500' : pct>20 ? 'bg-amber-500' : 'bg-red-400';
  return (
    <div className="space-y-1">
      <div className="h-2 bg-slate-100 rounded-full overflow-hidden">
        <div className={`h-full rounded-full transition-all duration-500 ${color}`} style={{width:`${pct}%`}} />
      </div>
      {showLabel && <p className="text-xs text-slate-500 text-right">{pct.toFixed(0)}%</p>}
    </div>
  );
}

export default function Card({ title, children, className='' }) {
  return (
    <div className={`card ${className}`}>
      {title && <h3 className="font-display text-lg font-bold mb-4 text-slate-800">{title}</h3>}
      {children}
    </div>
  );
}

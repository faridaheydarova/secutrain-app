export default function Button({ children, variant='primary', size='md', loading, disabled, className='', ...props }) {
  const v = { primary:'btn-primary', secondary:'btn-secondary', danger:'btn-danger',
    success:'bg-emerald-500 text-white px-5 py-2.5 rounded-xl font-semibold hover:bg-emerald-600 transition-all duration-200' };
  const s = { sm:'!px-3 !py-1.5 text-sm', md:'', lg:'!px-7 !py-3.5 text-base' };
  return (
    <button
      className={`${v[variant]} ${s[size]} ${className} ${disabled||loading?'opacity-50 cursor-not-allowed':''}`}
      disabled={disabled||loading}
      {...props}
    >
      {loading ? (
        <span className="flex items-center gap-2">
          <span className="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin" />
          Loading…
        </span>
      ) : children}
    </button>
  );
}

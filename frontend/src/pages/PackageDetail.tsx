import { useParams, useNavigate } from 'react-router-dom'
import { useGetPackageById } from '../api'
import { useCurrency } from '../contexts/CurrencyContext'

export default function PackageDetail() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { currency } = useCurrency()

  const { data: pkg, isLoading, isError, error } = useGetPackageById(id ?? '', { currency })

  if (isLoading && !pkg) {
    return (
      <div className="flex items-center gap-2 text-slate-500 dark:text-slate-400">
        <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24" fill="none"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" /><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" /></svg>
        Loading…
      </div>
    )
  }
  if (isError) {
    const err = error as { response?: { data?: { message?: string } }; message?: string }
    return (
      <div className="rounded-xl border border-red-200 dark:border-red-800 bg-red-50 dark:bg-red-900/20 px-4 py-3 text-red-700 dark:text-red-300">
        Error: {err?.response?.data?.message ?? err?.message ?? 'Failed to load'}
      </div>
    )
  }
  if (!pkg) return null

  return (
    <div>
      <button
        type="button"
        onClick={() => navigate('/')}
        className="mb-4 flex items-center gap-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 px-3 py-2 text-sm font-medium text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors"
      >
        <span aria-hidden>←</span> Back to list
      </button>
      <div className="rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 overflow-hidden shadow-sm">
        <div className="p-6 border-b border-slate-200 dark:border-slate-700">
          <h1 className="text-2xl font-semibold text-slate-900 dark:text-slate-100">{pkg.name}</h1>
          {pkg.description && (
            <p className="mt-2 text-slate-600 dark:text-slate-400">{pkg.description}</p>
          )}
          <p className="mt-3 text-amber-600 dark:text-amber-400 font-semibold">
            Total: {pkg.totalPrice} {pkg.currency}
          </p>
          <p className="mt-1 text-sm text-slate-500 dark:text-slate-500">
            Created {pkg.createdAt ? new Date(pkg.createdAt).toLocaleString() : ''}
          </p>
        </div>
        <div className="p-6">
          <h2 className="text-lg font-medium text-slate-800 dark:text-slate-200 mb-3">Products</h2>
          <ul className="space-y-2">
            {pkg.products?.map((prod, i) => (
              <li
                key={i}
                className="flex flex-wrap items-center justify-between gap-2 rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-700/50 px-4 py-3 text-sm"
              >
                <span className="font-medium text-slate-900 dark:text-slate-100">
                  {prod.productName}
                </span>
                <span className="text-slate-600 dark:text-slate-400">
                  {(prod.price ?? prod.productPriceUsd) != null
                    ? `${Number(prod.price ?? prod.productPriceUsd)} ${prod.currency ?? 'USD'}`
                    : '—'}{' '}
                  <span className="text-slate-400 dark:text-slate-500">(id: {prod.externalProductId})</span>
                </span>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  )
}

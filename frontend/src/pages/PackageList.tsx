import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { fetchPackages } from '../api/packageApi'
import { useCurrency } from '../contexts/CurrencyContext'
import type { PackageSummary } from '../types/api'

export default function PackageList() {
  const { currency } = useCurrency()
  const [packages, setPackages] = useState<PackageSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const size = 20

  useEffect(() => {
    setLoading(true)
    setError(null)
    fetchPackages(page, size, currency)
      .then((data) => {
        setPackages(data.content)
        setTotalPages(data.totalPages)
      })
      .catch((err: { response?: { data?: { message?: string } }; message?: string }) =>
        setError(err.response?.data?.message ?? err.message ?? 'Failed to load')
      )
      .finally(() => setLoading(false))
  }, [page, currency])

  if (loading && packages.length === 0) {
    return (
      <div className="flex items-center gap-2 text-slate-500 dark:text-slate-400">
        <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24" fill="none"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" /><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" /></svg>
        Loading packages…
      </div>
    )
  }
  if (error) {
    return (
      <div className="rounded-xl border border-red-200 dark:border-red-800 bg-red-50 dark:bg-red-900/20 px-4 py-3 text-red-700 dark:text-red-300">
        Error: {error}
      </div>
    )
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold text-slate-800 dark:text-slate-100 mb-6">Packages</h1>
      {packages.length === 0 ? (
        <div className="rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 p-8 text-center text-slate-600 dark:text-slate-400">
          <p className="mb-3">No packages yet.</p>
          <Link
            to="/create"
            className="inline-flex items-center gap-2 rounded-lg bg-amber-500 hover:bg-amber-600 text-white font-medium px-4 py-2 transition-colors"
          >
            Create one
          </Link>
        </div>
      ) : (
        <>
          <ul className="space-y-3">
            {packages.map((pkg) => (
              <li
                key={pkg.id}
                className="rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 overflow-hidden shadow-sm hover:shadow-md transition-shadow"
              >
                <Link
                  to={`/packages/${pkg.id}`}
                  className="block p-4 hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors"
                >
                  <div className="flex flex-wrap items-baseline justify-between gap-2">
                    <span className="font-semibold text-slate-900 dark:text-slate-100">
                      {pkg.name}
                    </span>
                    <span className="text-amber-600 dark:text-amber-400 font-medium">
                      {pkg.totalPrice} {pkg.currency}
                    </span>
                  </div>
                  {pkg.description && (
                    <p className="mt-1 text-sm text-slate-600 dark:text-slate-400 line-clamp-1">
                      {pkg.description}
                    </p>
                  )}
                  <p className="mt-2 text-xs text-slate-500 dark:text-slate-500">
                    Created {new Date(pkg.createdAt).toLocaleString()}
                  </p>
                </Link>
              </li>
            ))}
          </ul>
          {totalPages > 1 && (
            <div className="mt-6 flex items-center justify-center gap-3">
              <button
                type="button"
                disabled={page === 0}
                onClick={() => setPage((p) => p - 1)}
                className="rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 px-4 py-2 text-sm font-medium text-slate-700 dark:text-slate-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors"
              >
                Previous
              </button>
              <span className="text-sm text-slate-600 dark:text-slate-400">
                Page {page + 1} of {totalPages}
              </span>
              <button
                type="button"
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
                className="rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 px-4 py-2 text-sm font-medium text-slate-700 dark:text-slate-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}

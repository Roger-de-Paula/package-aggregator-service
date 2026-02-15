import { useState, useEffect, useCallback } from 'react'
import { getCurrencies } from '../api'
import { useCurrency } from '../contexts/CurrencyContext'

const DEBOUNCE_MS = 300

export default function CurrencyModal() {
  const { currencyModalOpen, setCurrencyModalOpen, setCurrency } = useCurrency()
  const [search, setSearch] = useState('')
  const [debouncedSearch, setDebouncedSearch] = useState('')
  const [options, setOptions] = useState<{ code?: string; name?: string }[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!currencyModalOpen) return
    const t = setTimeout(() => setDebouncedSearch(search.trim()), DEBOUNCE_MS)
    return () => clearTimeout(t)
  }, [currencyModalOpen, search])

  useEffect(() => {
    if (!currencyModalOpen) return
    setLoading(true)
    setError(null)
    getCurrencies(debouncedSearch ? { search: debouncedSearch } : undefined)
      .then(setOptions)
      .catch((e) => setError(e.response?.data?.message ?? e.message ?? 'Failed to load currencies'))
      .finally(() => setLoading(false))
  }, [currencyModalOpen, debouncedSearch])

  const handleClose = useCallback(() => {
    setCurrencyModalOpen(false)
    setSearch('')
    setError(null)
  }, [setCurrencyModalOpen])

  const handleSelect = useCallback(
    (code: string) => {
      setCurrency(code)
      handleClose()
    },
    [setCurrency, handleClose]
  )

  if (!currencyModalOpen) return null

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
      onClick={handleClose}
      role="dialog"
      aria-modal="true"
      aria-labelledby="currency-modal-title"
    >
      <div
        className="w-full max-w-md rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 shadow-xl max-h-[80vh] flex flex-col"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="p-4 border-b border-slate-200 dark:border-slate-700">
          <h2 id="currency-modal-title" className="text-lg font-semibold text-slate-900 dark:text-slate-100">
            Choose currency
          </h2>
          <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
            Search by code or name. Results come from the server.
          </p>
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="e.g. JPY or Japanese"
            className="mt-3 w-full rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-slate-900 dark:text-slate-100 px-3 py-2 focus:ring-2 focus:ring-amber-500 outline-none"
            autoFocus
          />
        </div>
        <div className="flex-1 overflow-y-auto min-h-0 p-2">
          {loading && (
            <div className="py-6 text-center text-slate-500 dark:text-slate-400 text-sm">
              Loading…
            </div>
          )}
          {error && (
            <div className="py-4 text-center text-red-600 dark:text-red-400 text-sm">
              {error}
            </div>
          )}
          {!loading && !error && options.length === 0 && (
            <div className="py-6 text-center text-slate-500 dark:text-slate-400 text-sm">
              {debouncedSearch ? 'No currencies match your search.' : 'Type to search currencies.'}
            </div>
          )}
          {!loading && !error && options.length > 0 && (
            <ul className="space-y-0.5">
              {options.map((opt) => (
                <li key={opt.code ?? ''}>
                  <button
                    type="button"
                    onClick={() => handleSelect(opt.code ?? '')}
                    className="w-full text-left rounded-lg px-3 py-2.5 text-sm font-medium text-slate-800 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors"
                  >
                    <span className="font-mono text-amber-600 dark:text-amber-400">{opt.code}</span>
                    <span className="ml-2 text-slate-600 dark:text-slate-400">{opt.name}</span>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
        <div className="p-3 border-t border-slate-200 dark:border-slate-700">
          <button
            type="button"
            onClick={handleClose}
            className="w-full rounded-lg border border-slate-300 dark:border-slate-600 bg-slate-100 dark:bg-slate-700 text-slate-700 dark:text-slate-300 py-2 text-sm font-medium hover:bg-slate-200 dark:hover:bg-slate-600 transition-colors"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  )
}

import { useState, FormEvent, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { createPackage, fetchProducts } from '../api/packageApi'
import { useCurrency } from '../contexts/CurrencyContext'
import type { Product } from '../types/api'

export default function CreatePackage() {
  const navigate = useNavigate()
  const { currency } = useCurrency()
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [products, setProducts] = useState<Product[]>([])
  const [productsLoading, setProductsLoading] = useState(true)
  const [productsError, setProductsError] = useState<string | null>(null)
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set())
  const [submitLoading, setSubmitLoading] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    setProductsLoading(true)
    setProductsError(null)
    fetchProducts(currency)
      .then((data) => {
        if (!cancelled) setProducts(data)
      })
      .catch((err: { response?: { data?: { message?: string } }; message?: string }) => {
        if (!cancelled) setProductsError(err.response?.data?.message ?? err.message ?? 'Failed to load products')
      })
      .finally(() => {
        if (!cancelled) setProductsLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [currency])

  const toggleProduct = (id: string) => {
    setSelectedIds((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    const productIds = Array.from(selectedIds)
    if (productIds.length === 0) {
      setSubmitError('Select at least one product.')
      return
    }
    setSubmitLoading(true)
    setSubmitError(null)
    createPackage({ name, description, productIds })
      .then(() => navigate('/'))
      .catch((err: { response?: { data?: { message?: string } }; message?: string }) =>
        setSubmitError(err.response?.data?.message ?? err.message ?? 'Failed to create package')
      )
      .finally(() => setSubmitLoading(false))
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold text-slate-800 dark:text-slate-100 mb-6">Create Package</h1>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 p-6 shadow-sm space-y-4">
          <div>
            <label htmlFor="name" className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">
              Package name *
            </label>
            <input
              id="name"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              className="w-full rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-slate-900 dark:text-slate-100 px-3 py-2 focus:ring-2 focus:ring-amber-500 focus:border-amber-500 dark:focus:ring-amber-400 outline-none transition-shadow"
            />
          </div>
          <div>
            <label htmlFor="description" className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">
              Description
            </label>
            <input
              id="description"
              type="text"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-slate-900 dark:text-slate-100 px-3 py-2 focus:ring-2 focus:ring-amber-500 focus:border-amber-500 dark:focus:ring-amber-400 outline-none transition-shadow"
            />
          </div>
        </div>

        <div className="rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 p-6 shadow-sm">
          <h2 className="text-lg font-medium text-slate-800 dark:text-slate-200 mb-2">Choose products</h2>
          <p className="text-sm text-slate-500 dark:text-slate-400 mb-4">
            Prices in <strong>{currency}</strong>. Select one or more products to include. You’ve selected <strong>{selectedIds.size}</strong> product{selectedIds.size !== 1 ? 's' : ''}.
          </p>

          {productsLoading && (
            <div className="flex items-center gap-2 text-slate-500 dark:text-slate-400 py-8">
              <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24" fill="none"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" /><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" /></svg>
              Loading products…
            </div>
          )}

          {productsError && (
            <div className="rounded-lg border border-red-200 dark:border-red-800 bg-red-50 dark:bg-red-900/20 px-4 py-3 text-red-700 dark:text-red-300">
              {productsError}
            </div>
          )}

          {!productsLoading && !productsError && products.length === 0 && (
            <p className="text-slate-500 dark:text-slate-400 py-4">No products available.</p>
          )}

          {!productsLoading && !productsError && products.length > 0 && (
            <ul className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
              {products.map((product) => {
                const selected = selectedIds.has(product.id)
                return (
                  <li key={product.id}>
                    <button
                      type="button"
                      onClick={() => toggleProduct(product.id)}
                      className={`w-full text-left rounded-xl border-2 p-4 transition-all ${
                        selected
                          ? 'border-amber-500 bg-amber-50 dark:bg-amber-900/20 dark:border-amber-500 shadow-sm'
                          : 'border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-700/50 hover:border-slate-300 dark:hover:border-slate-600 hover:bg-slate-100 dark:hover:bg-slate-700'
                      }`}
                    >
                      <span className="block font-medium text-slate-900 dark:text-slate-100">{product.name}</span>
                      <span className="block mt-1 text-sm text-amber-600 dark:text-amber-400 font-medium">
                        {(product.price ?? product.usdPrice) != null
                          ? `${Number(product.price ?? product.usdPrice)} ${product.currency ?? 'USD'}`
                          : '—'}
                      </span>
                      {selected && (
                        <span className="mt-2 inline-flex items-center gap-1 text-xs text-amber-700 dark:text-amber-300">
                          <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20"><path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" /></svg>
                          Selected
                        </span>
                      )}
                    </button>
                  </li>
                )
              })}
            </ul>
          )}
        </div>

        {submitError && (
          <p className="text-red-600 dark:text-red-400 text-sm">{submitError}</p>
        )}

        <button
          type="submit"
          disabled={submitLoading || selectedIds.size === 0}
          className="w-full max-w-md rounded-lg bg-amber-500 hover:bg-amber-600 disabled:bg-amber-400 disabled:cursor-not-allowed text-white font-medium py-2.5 transition-colors"
        >
          {submitLoading ? 'Creating…' : 'Create Package'}
        </button>
      </form>
    </div>
  )
}

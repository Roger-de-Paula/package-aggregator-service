import { useState, FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { createPackage } from '../api/packageApi'

export default function CreatePackage() {
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [productIdsInput, setProductIdsInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    const productIds = productIdsInput
      .split(',')
      .map((s) => parseInt(s.trim(), 10))
      .filter((n) => !Number.isNaN(n))
    if (productIds.length === 0) {
      setError('Enter at least one product ID (e.g. 1,2,3)')
      return
    }
    setLoading(true)
    setError(null)
    createPackage({ name, description, productIds })
      .then(() => navigate('/'))
      .catch((err: { response?: { data?: { message?: string } }; message?: string }) =>
        setError(err.response?.data?.message ?? err.message ?? 'Failed to create')
      )
      .finally(() => setLoading(false))
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold text-slate-800 dark:text-slate-100 mb-6">Create Package</h1>
      <form
        onSubmit={handleSubmit}
        className="max-w-md rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 p-6 shadow-sm space-y-4"
      >
        <div>
          <label htmlFor="name" className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">
            Name *
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
        <div>
          <label htmlFor="productIds" className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">
            Product IDs (comma-separated, e.g. 1,2,3) *
          </label>
          <input
            id="productIds"
            type="text"
            value={productIdsInput}
            onChange={(e) => setProductIdsInput(e.target.value)}
            placeholder="1,2,3"
            className="w-full rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-slate-900 dark:text-slate-100 px-3 py-2 placeholder-slate-400 focus:ring-2 focus:ring-amber-500 focus:border-amber-500 dark:focus:ring-amber-400 outline-none transition-shadow"
          />
        </div>
        {error && (
          <p className="text-red-600 dark:text-red-400 text-sm">{error}</p>
        )}
        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-lg bg-amber-500 hover:bg-amber-600 disabled:bg-amber-400 text-white font-medium py-2.5 transition-colors disabled:cursor-not-allowed"
        >
          {loading ? 'Creating…' : 'Create Package'}
        </button>
      </form>
    </div>
  )
}

import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { fetchPackageById } from '../api/packageApi'
import type { PackageDetail as PackageDetailType } from '../types/api'

const CURRENCIES = ['USD', 'EUR', 'GBP']

export default function PackageDetail() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [currency, setCurrency] = useState('USD')
  const [pkg, setPkg] = useState<PackageDetailType | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    setLoading(true)
    setError(null)
    fetchPackageById(id, currency)
      .then(setPkg)
      .catch((err: { response?: { data?: { message?: string } }; message?: string }) =>
        setError(err.response?.data?.message ?? err.message ?? 'Failed to load')
      )
      .finally(() => setLoading(false))
  }, [id, currency])

  if (loading && !pkg) return <p>Loading...</p>
  if (error) return <p style={{ color: 'red' }}>Error: {error}</p>
  if (!pkg) return null

  return (
    <div>
      <button type="button" onClick={() => navigate('/')} style={{ marginBottom: '1rem' }}>← Back to list</button>
      <h1>{pkg.name}</h1>
      {pkg.description && <p>{pkg.description}</p>}
      <div style={{ marginBottom: '1rem' }}>
        <label>Currency: </label>
        <select value={currency} onChange={(e) => setCurrency(e.target.value)}>
          {CURRENCIES.map((c) => (
            <option key={c} value={c}>{c}</option>
          ))}
        </select>
      </div>
      <p><strong>Total: {pkg.totalPrice} {pkg.currency}</strong></p>
      <p style={{ color: '#666' }}>Created: {new Date(pkg.createdAt).toLocaleString()}</p>
      <h2>Products</h2>
      <ul>
        {pkg.products?.map((prod, i) => (
          <li key={i}>
            {prod.productName} — {prod.productPriceUsd} USD (external id: {prod.externalProductId})
          </li>
        ))}
      </ul>
    </div>
  )
}

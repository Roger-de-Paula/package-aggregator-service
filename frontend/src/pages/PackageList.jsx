import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { fetchPackages } from '../api/packageApi'

const CURRENCIES = ['USD', 'EUR', 'GBP']

export default function PackageList() {
  const [packages, setPackages] = useState([])
  const [currency, setCurrency] = useState('USD')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
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
      .catch((err) => setError(err.response?.data?.message || err.message))
      .finally(() => setLoading(false))
  }, [page, currency])

  if (loading && packages.length === 0) return <p>Loading packages...</p>
  if (error) return <p style={{ color: 'red' }}>Error: {error}</p>

  return (
    <div>
      <h1>Packages</h1>
      <div style={{ marginBottom: '1rem' }}>
        <label>Currency: </label>
        <select value={currency} onChange={(e) => setCurrency(e.target.value)}>
          {CURRENCIES.map((c) => (
            <option key={c} value={c}>{c}</option>
          ))}
        </select>
      </div>
      {packages.length === 0 ? (
        <p>No packages yet. <Link to="/create">Create one</Link>.</p>
      ) : (
        <ul style={{ listStyle: 'none', padding: 0 }}>
          {packages.map((pkg) => (
            <li key={pkg.id} style={{ border: '1px solid #ccc', padding: '0.75rem', marginBottom: '0.5rem' }}>
              <strong><Link to={`/packages/${pkg.id}`}>{pkg.name}</Link></strong>
              {pkg.description && <span> — {pkg.description}</span>}
              <div>
                Total: {pkg.totalPrice} {pkg.currency}
              </div>
              <div style={{ fontSize: '0.9rem', color: '#666' }}>
                Created: {new Date(pkg.createdAt).toLocaleString()}
              </div>
            </li>
          ))}
        </ul>
      )}
      {totalPages > 1 && (
        <div style={{ marginTop: '1rem' }}>
          <button disabled={page === 0} onClick={() => setPage((p) => p - 1)}>Previous</button>
          <span style={{ margin: '0 1rem' }}>Page {page + 1} of {totalPages}</span>
          <button disabled={page >= totalPages - 1} onClick={() => setPage((p) => p + 1)}>Next</button>
        </div>
      )}
    </div>
  )
}

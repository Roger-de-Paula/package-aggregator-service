import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createPackage } from '../api/packageApi'

export default function CreatePackage() {
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [productIdsInput, setProductIdsInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const handleSubmit = (e) => {
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
      .catch((err) => setError(err.response?.data?.message || err.message))
      .finally(() => setLoading(false))
  }

  return (
    <div>
      <h1>Create Package</h1>
      <form onSubmit={handleSubmit} style={{ maxWidth: '400px' }}>
        <div style={{ marginBottom: '0.75rem' }}>
          <label>Name *</label>
          <br />
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            style={{ width: '100%' }}
          />
        </div>
        <div style={{ marginBottom: '0.75rem' }}>
          <label>Description</label>
          <br />
          <input
            type="text"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            style={{ width: '100%' }}
          />
        </div>
        <div style={{ marginBottom: '0.75rem' }}>
          <label>Product IDs (comma-separated, e.g. 1,2,3) *</label>
          <br />
          <input
            type="text"
            value={productIdsInput}
            onChange={(e) => setProductIdsInput(e.target.value)}
            placeholder="1,2,3"
            style={{ width: '100%' }}
          />
        </div>
        {error && <p style={{ color: 'red', marginBottom: '0.5rem' }}>{error}</p>}
        <button type="submit" disabled={loading}>
          {loading ? 'Creating...' : 'Create Package'}
        </button>
      </form>
    </div>
  )
}

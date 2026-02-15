import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
})

export const fetchPackages = (page = 0, size = 20, currency = 'USD') =>
  api.get('/packages', { params: { page, size, currency } }).then((r) => r.data)

export const fetchPackageById = (id, currency = 'USD') =>
  api.get(`/packages/${id}`, { params: { currency } }).then((r) => r.data)

export const createPackage = (body) =>
  api.post('/packages', body).then((r) => r.data)

export const updatePackage = (id, body) =>
  api.put(`/packages/${id}`, body).then((r) => r.data)

export const deletePackage = (id) =>
  api.delete(`/packages/${id}`)

import axios from 'axios'
import type {
  PackageSummary,
  PackageDetail,
  PageResponse,
  CreatePackageRequest,
  UpdatePackageRequest
} from '../types/api'

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
})

export const fetchPackages = (
  page = 0,
  size = 20,
  currency = 'USD'
): Promise<PageResponse<PackageSummary>> =>
  api.get('/packages', { params: { page, size, currency } }).then((r) => r.data)

export const fetchPackageById = (
  id: string,
  currency = 'USD'
): Promise<PackageDetail> =>
  api.get(`/packages/${id}`, { params: { currency } }).then((r) => r.data)

export const createPackage = (body: CreatePackageRequest): Promise<PackageDetail> =>
  api.post('/packages', body).then((r) => r.data)

export const updatePackage = (
  id: string,
  body: UpdatePackageRequest
): Promise<PackageDetail> =>
  api.put(`/packages/${id}`, body).then((r) => r.data)

export const deletePackage = (id: string): Promise<void> =>
  api.delete(`/packages/${id}`)
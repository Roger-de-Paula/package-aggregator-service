import axios from 'axios'
import type {
  PackageSummary,
  PackageDetail,
  PageResponse,
  CreatePackageRequest,
  UpdatePackageRequest,
  Product
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
  api
    .get<PackageDetail>(`/packages/${id}`, {
      params: { currency },
      headers: { 'Cache-Control': 'no-cache', Pragma: 'no-cache' },
    })
    .then((r) => r.data)

export const createPackage = (body: CreatePackageRequest): Promise<PackageDetail> =>
  api.post('/packages', body).then((r) => r.data)

export const updatePackage = (
  id: string,
  body: UpdatePackageRequest
): Promise<PackageDetail> =>
  api.put(`/packages/${id}`, body).then((r) => r.data)

export const deletePackage = (id: string): Promise<void> =>
  api.delete(`/packages/${id}`)

/** Fetch product catalog for package builder (internal endpoint). Prices returned in the given currency. */
export const fetchProducts = (currency = 'USD'): Promise<Product[]> =>
  api
    .get<Product[]>('/products', {
      params: { currency },
      headers: { 'Cache-Control': 'no-cache', Pragma: 'no-cache' },
    })
    .then((r) => r.data)

/** Fetch available currencies (from Frankfurter). Optional search filters by code or name. */
export const fetchCurrencies = (search = ''): Promise<CurrencyOption[]> =>
  api.get<CurrencyOption[]>('/currencies', { params: search ? { search } : {} }).then((r) => r.data)

export interface CurrencyOption {
  code: string
  name: string
}
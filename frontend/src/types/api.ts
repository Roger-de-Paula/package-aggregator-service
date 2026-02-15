/** Matches backend PackageSummaryDto / PackageResponseDto (list and detail) */
export interface PackageSummary {
  id: string
  name: string
  description: string | null
  totalPrice: number
  currency: string
  createdAt: string
}

export interface PackageProduct {
  externalProductId: string
  productName: string
  productPriceUsd: number
  /** Price in the response currency (when package was fetched with ?currency=). */
  price?: number
  /** Display currency (e.g. USD, EUR). */
  currency?: string
}

/** Full package detail with products (PackageResponseDto) */
export interface PackageDetail extends PackageSummary {
  products: PackageProduct[]
}

/** Page response from GET /packages */
export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface CreatePackageRequest {
  name: string
  description?: string
  productIds: string[]
}

export interface UpdatePackageRequest {
  name: string
  description?: string
}

/** Product from GET /products (catalog for building a package). price/currency match the requested currency. */
export interface Product {
  id: string
  name: string
  /** Price in the requested currency (from backend). */
  price?: number
  /** Display currency code (from backend). */
  currency?: string
  /** Legacy: backend may send usdPrice instead of price/currency. */
  usdPrice?: number
}

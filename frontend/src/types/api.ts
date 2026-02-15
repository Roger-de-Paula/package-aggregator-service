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
  externalProductId: number
  productName: string
  productPriceUsd: number
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
  productIds: number[]
}

export interface UpdatePackageRequest {
  name: string
  description?: string
}

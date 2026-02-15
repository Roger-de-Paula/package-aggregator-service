/**
 * Re-exports the Orval-generated API client and types.
 * Use: import { getPackages, getPackageById, ... } from '../api'
 *      import type { PackageSummaryDto, PageDtoPackageSummaryDto, ... } from '../api'
 */
import {
  getPackageAggregationServiceAPI,
  type CreatePackageRequest,
  type CurrencyOptionDto,
  type PackageProductDto,
  type PackageResponseDto,
  type PackageSummaryDto,
  type PageDtoPackageSummaryDto,
  type ProductDto,
  type UpdatePackageRequest,
} from './generated/api'

const api = getPackageAggregationServiceAPI()

export const getPackages = api.getPackages
export const createPackage = api.createPackage
export const getPackageById = api.getPackageById
export const updatePackage = api.updatePackage
export const deletePackage = api.deletePackage
export const getProducts = api.getProducts
export const getCurrencies = api.getCurrencies

export type {
  CreatePackageRequest,
  CurrencyOptionDto,
  PackageProductDto,
  PackageResponseDto,
  PackageSummaryDto,
  PageDtoPackageSummaryDto,
  ProductDto,
  UpdatePackageRequest,
}

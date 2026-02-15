/**
 * Re-exports Orval-generated React Query hooks and types.
 * Use: import { useGetPackages, useCreatePackage, ... } from '../api'
 *      import type { PackageSummaryDto, PackageResponseDto, ... } from '../api'
 */
export {
  useGetPackages,
  useGetPackageById,
  useGetProducts,
  useGetCurrencies,
  useCreatePackage,
  useUpdatePackage,
  useDeletePackage,
  getGetPackagesQueryKey,
} from './generated/api'

export type {
  CreatePackageRequest,
  CurrencyOptionDto,
  PackageProductDto,
  PackageResponseDto,
  PackageSummaryDto,
  PageDtoPackageSummaryDto,
  ProductDto,
  UpdatePackageRequest,
} from './generated/api'

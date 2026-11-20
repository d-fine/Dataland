import type { DataDimensionSearchRequest } from '@clients/backend';

export const nonSourceabilityKeys = {
  all: ['nonSourceability'] as const,
  search: (request: DataDimensionSearchRequest | undefined) =>
    [...nonSourceabilityKeys.all, 'search', request] as const,
  searchGroupedByCompanyAndFramework: (request: DataDimensionSearchRequest | undefined) =>
    [...nonSourceabilityKeys.all, 'searchGroupedByCompanyAndFramework', request] as const,
};

import type { DataDimensionSearchRequest } from '@clients/backend';

export const nonSourceabilityKeys = {
  all: ['nonSourceability'] as const,
  search: (request: DataDimensionSearchRequest) => ['nonSourceability', 'search', request] as const,
};

import type { RequestSearchFilterString } from '@clients/datasourcingservice';

export const enhancedRequestKeys = {
  all: ['enhancedRequest'] as const,
  forRequestingUser: () => [...enhancedRequestKeys.all, 'forRequestingUser'] as const,
  search: (filters: RequestSearchFilterString, chunkSize: number | undefined, chunkIndex: number | undefined) =>
    [...enhancedRequestKeys.all, 'search', filters, chunkSize, chunkIndex] as const,
  searchCount: (filters: RequestSearchFilterString) => [...enhancedRequestKeys.all, 'searchCount', filters] as const,
};

import type { GetStoredDataRequestsQueryParams } from './useGetRequestsQuery';

export const requestKeys = {
  all: ['communityRequests'] as const,

  filtered: (params: GetStoredDataRequestsQueryParams) => ['communityRequests', 'filtered', params] as const,
};

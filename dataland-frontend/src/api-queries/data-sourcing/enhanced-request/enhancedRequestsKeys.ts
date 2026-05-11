import type { RequestSearchFilterString } from '@clients/datasourcingservice';

export const enhancedRequestsKeys = {
  all: ['enhancedRequests'] as const,

  searchCount: (filters: RequestSearchFilterString) => ['enhancedRequests', 'searchCount', filters] as const,
};

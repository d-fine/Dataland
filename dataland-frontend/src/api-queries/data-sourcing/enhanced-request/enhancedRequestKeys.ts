import type { RequestSearchFilterString } from '@clients/datasourcingservice';

export const enhancedRequestKeys = {
  all: ['enhancedRequest'] as const,
  forRequestingUser: () => [...enhancedRequestKeys.all, 'forRequestingUser'] as const,
  searchCount: (filters: RequestSearchFilterString) => [...enhancedRequestKeys.all, 'searchCount', filters] as const,
};

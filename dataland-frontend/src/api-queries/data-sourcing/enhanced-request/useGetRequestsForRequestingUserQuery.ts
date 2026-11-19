import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { enhancedRequestKeys } from '@/api-queries/data-sourcing/enhanced-request/enhancedRequestKeys';
import { type DataSourcingEnhancedRequest } from '@clients/datasourcingservice';

/**
 * Vue Query hook that fetches all data requests of the current user (GET /data-sourcing/enhanced-requests).
 *
 * @param options - Additional TanStack query options.
 * @returns Vue Query result containing the user's {@link DataSourcingEnhancedRequest}s.
 */
export function useGetRequestsForRequestingUserQuery(
  options: Omit<UseQueryOptions<DataSourcingEnhancedRequest[], Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<DataSourcingEnhancedRequest[], Error> {
  const apiClientProvider = useApiClient();

  return useQuery<DataSourcingEnhancedRequest[], Error>({
    queryKey: enhancedRequestKeys.forRequestingUser(),
    queryFn: async () => {
      const response = await apiClientProvider.apiClients.enhancedRequestController.getRequestsForRequestingUser();
      return response.data;
    },
    ...options,
  });
}

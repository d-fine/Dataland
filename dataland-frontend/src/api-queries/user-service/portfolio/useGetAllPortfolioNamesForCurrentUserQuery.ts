import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { portfolioKeys } from '@/api-queries/user-service/portfolio/portfolioKeys';
import { type BasePortfolioName } from '@clients/userservice';

/**
 * Vue Query hook that fetches the ids and names of all portfolios owned by the current user
 * (GET /users/portfolios/names).
 *
 * @param options - Additional TanStack query options.
 * @returns Vue Query result containing the {@link BasePortfolioName}s.
 */
export function useGetAllPortfolioNamesForCurrentUserQuery(
  options: Omit<UseQueryOptions<BasePortfolioName[], Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<BasePortfolioName[], Error> {
  const apiClientProvider = useApiClient();

  return useQuery<BasePortfolioName[], Error>({
    queryKey: portfolioKeys.names(),
    queryFn: async () => {
      const response = await apiClientProvider.apiClients.portfolioController.getAllPortfolioNamesForCurrentUser();
      return response.data;
    },
    ...options,
  });
}

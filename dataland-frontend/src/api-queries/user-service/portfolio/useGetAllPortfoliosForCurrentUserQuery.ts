import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { portfolioKeys } from '@/api-queries/user-service/portfolio/portfolioKeys';
import { type BasePortfolio } from '@clients/userservice';

/**
 * Vue Query hook that fetches all portfolios owned by the current user (GET /users/portfolios/).
 *
 * @param options - Additional TanStack query options.
 * @returns Vue Query result containing the {@link BasePortfolio}s.
 */
export function useGetAllPortfoliosForCurrentUserQuery(
  options: Omit<UseQueryOptions<BasePortfolio[], Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<BasePortfolio[], Error> {
  const apiClientProvider = useApiClient();

  return useQuery<BasePortfolio[], Error>({
    queryKey: portfolioKeys.list(),
    queryFn: async () => {
      const response = await apiClientProvider.apiClients.portfolioController.getAllPortfoliosForCurrentUser();
      return response.data;
    },
    ...options,
  });
}

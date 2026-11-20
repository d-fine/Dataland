import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { portfolioKeys } from '@/api-queries/user-service/portfolio/portfolioKeys';
import { type PortfolioUserAccessRight } from '@clients/userservice';

/**
 * Vue Query hook that fetches the users with access to a portfolio and their roles
 * (GET /users/portfolios/{portfolioId}/access-rights).
 * The query is disabled while `portfolioId` is empty.
 *
 * @param params - Endpoint parameters.
 * @param params.portfolioId - Id of the portfolio.
 * @param options - Additional TanStack query options. A passed `enabled` replaces the default.
 * @returns Vue Query result containing the {@link PortfolioUserAccessRight}s.
 */
export function useGetPortfolioAccessRightsQuery(
  params: { portfolioId: MaybeRefOrGetter<string | undefined> },
  options: Omit<UseQueryOptions<PortfolioUserAccessRight[], Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<PortfolioUserAccessRight[], Error> {
  const apiClientProvider = useApiClient();

  return useQuery<PortfolioUserAccessRight[], Error>({
    queryKey: computed(() => portfolioKeys.accessRights(toValue(params.portfolioId))),
    queryFn: async () => {
      const portfolioId = toValue(params.portfolioId);
      if (!portfolioId) throw new Error('portfolioId is undefined');
      const response = await apiClientProvider.apiClients.portfolioController.getPortfolioAccessRights(portfolioId);
      return response.data;
    },
    enabled: computed(() => !!toValue(params.portfolioId)),
    ...options,
  });
}

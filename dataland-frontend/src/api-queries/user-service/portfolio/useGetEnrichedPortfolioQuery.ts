import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { portfolioKeys } from '@/api-queries/user-service/portfolio/portfolioKeys';
import { type EnrichedPortfolio } from '@clients/userservice';

/**
 * Vue Query hook that fetches a portfolio enriched with company and data availability information
 * (GET /users/portfolios/{portfolioId}/enriched-portfolio).
 * The query is disabled while `portfolioId` is empty.
 *
 * @param params - Endpoint parameters.
 * @param params.portfolioId - Id of the portfolio.
 * @param options - Additional TanStack query options. A passed `enabled` replaces the default.
 * @returns Vue Query result containing the {@link EnrichedPortfolio}.
 */
export function useGetEnrichedPortfolioQuery(
  params: { portfolioId: MaybeRefOrGetter<string | undefined> },
  options: Omit<UseQueryOptions<EnrichedPortfolio, Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<EnrichedPortfolio, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<EnrichedPortfolio, Error>({
    queryKey: computed(() => portfolioKeys.enriched(toValue(params.portfolioId))),
    queryFn: async () => {
      const portfolioId = toValue(params.portfolioId);
      if (!portfolioId) throw new Error('portfolioId is undefined');
      const response = await apiClientProvider.apiClients.portfolioController.getEnrichedPortfolio(portfolioId);
      return response.data;
    },
    enabled: computed(() => !!toValue(params.portfolioId)),
    ...options,
  });
}

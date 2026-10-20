import { type Ref, computed } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient.ts';
import { portfolioKeys } from '@/api-queries/user-service/portfolio/portfolioKeys.ts';
import type { EnrichedPortfolio } from '@clients/userservice';

const FIVE_MINUTES_IN_MS = 5 * 60 * 1000;

/**
 * Vue Query hook that fetches an enriched portfolio for a given portfolio id.
 *
 * @param {Ref<string|undefined>} portfolioId - Reactive reference containing the portfolio id; query runs when truthy.
 * @returns {UseQueryReturnType<EnrichedPortfolio, Error>} Query result containing the enriched portfolio.
 */
export function useGetEnrichedPortfolioQuery(
  portfolioId: Ref<string | undefined>
): UseQueryReturnType<EnrichedPortfolio, Error> {
  const apiClientProvider = useApiClient();
  const queryKey = computed(() => portfolioKeys.enriched(portfolioId.value));
  const enabled = computed(() => !!portfolioId.value);

  return useQuery<EnrichedPortfolio, Error>({
    queryKey,
    enabled,
    staleTime: FIVE_MINUTES_IN_MS,
    queryFn: async () => {
      if (!portfolioId.value) {
        throw new Error('portfolioId is undefined');
      }
      const response = await apiClientProvider.apiClients.portfolioController.getEnrichedPortfolio(portfolioId.value);
      return response.data;
    },
  });
}

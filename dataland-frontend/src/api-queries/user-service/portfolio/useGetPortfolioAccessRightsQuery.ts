import { computed, type Ref } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import { type PortfolioUserAccessRight } from '@clients/userservice';
import { useApiClient } from '@/utils/useApiClient.ts';
import { portfolioKeys } from '@/api-queries/user-service/portfolio/portfolioKeys.ts';

/**
 * Vue Query hook that fetches portfolio access rights for a given portfolio id.
 *
 * @param {Ref<string|undefined>} portfolioId - Reactive reference containing the portfolio id; query runs when truthy.
 * @returns {UseQueryReturnType<PortfolioUserAccessRight[], Error>} Query result containing the portfolio access rights.
 */
export function useGetPortfolioAccessRightsQuery(
  portfolioId: Ref<string | undefined>
): UseQueryReturnType<PortfolioUserAccessRight[], Error> {
  const apiClientProvider = useApiClient();
  const queryKey = computed(() => portfolioKeys.portfolioAccessRights(portfolioId.value));
  const enabled = computed(() => !!portfolioId.value);

  return useQuery<PortfolioUserAccessRight[], Error>({
    queryKey,
    enabled,
    queryFn: async () => {
      if (!portfolioId.value) {
        throw new Error('portfolioId is undefined');
      }
      const response = await apiClientProvider.apiClients.portfolioController.getPortfolioAccessRights(
        portfolioId.value
      );
      return response.data;
    },
  });
}

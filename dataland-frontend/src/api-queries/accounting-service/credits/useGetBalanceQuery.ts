import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { creditsKeys } from '@/api-queries/accounting-service/credits/creditsKeys';

/**
 * Vue Query hook that fetches the credit balance of a company (GET /accounting/credits/{companyId}/balance).
 * The query is disabled while `companyId` is empty.
 *
 * @param params - Endpoint parameters.
 * @param params.companyId - Id of the company.
 * @param options - Additional TanStack query options. A passed `enabled` replaces the default.
 * @returns Vue Query result containing the credit balance.
 */
export function useGetBalanceQuery(
  params: { companyId: MaybeRefOrGetter<string | undefined> },
  options: Omit<UseQueryOptions<number, Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<number, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<number, Error>({
    queryKey: computed(() => creditsKeys.balance(toValue(params.companyId))),
    queryFn: async () => {
      const companyId = toValue(params.companyId);
      if (!companyId) throw new Error('companyId is undefined');
      const response = await apiClientProvider.apiClients.creditsController.getBalance(companyId);
      return response.data;
    },
    enabled: computed(() => !!toValue(params.companyId)),
    ...options,
  });
}

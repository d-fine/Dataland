import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { companyDataKeys } from '@/api-queries/backend/company-data/companyDataKeys';
import { type AggregatedFrameworkDataSummary } from '@clients/backend';

type AggregatedFrameworkDataSummaryByFramework = { [key: string]: AggregatedFrameworkDataSummary };

/**
 * Vue Query hook that fetches the number of reporting periods with data per framework for a company
 * (GET /companies/{companyId}/aggregated-framework-data-summary).
 * The query is disabled while `companyId` is empty.
 *
 * @param params - Endpoint parameters.
 * @param params.companyId - Id of the company.
 * @param options - Additional TanStack query options. A passed `enabled` replaces the default.
 * @returns Vue Query result containing the {@link AggregatedFrameworkDataSummary} per framework.
 */
export function useGetAggregatedFrameworkDataSummaryQuery(
  params: { companyId: MaybeRefOrGetter<string | undefined> },
  options: Omit<UseQueryOptions<AggregatedFrameworkDataSummaryByFramework, Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<AggregatedFrameworkDataSummaryByFramework, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<AggregatedFrameworkDataSummaryByFramework, Error>({
    queryKey: computed(() => companyDataKeys.aggregatedFrameworkDataSummary(toValue(params.companyId))),
    queryFn: async () => {
      const companyId = toValue(params.companyId);
      if (!companyId) throw new Error('companyId is undefined');
      const response =
        await apiClientProvider.backendClients.companyDataController.getAggregatedFrameworkDataSummary(companyId);
      return response.data;
    },
    enabled: computed(() => !!toValue(params.companyId)),
    ...options,
  });
}

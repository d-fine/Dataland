import { computed, toValue, type MaybeRefOrGetter, type Ref } from 'vue';
import { useQueries, type QueryObserverResult } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { companyDataKeys } from '@/api-queries/backend/company-data/companyDataKeys';
import { type CompanyInformation } from '@clients/backend';

/**
 * Vue Query hook that fetches the information of several companies in parallel, one GET /companies/{companyId}/info
 * request per company. Shares the cache entries with `useGetCompanyInfoQuery`.
 *
 * @param params - Endpoint parameters.
 * @param params.companyIds - Ids of the companies. One query is created per id.
 * @returns The query results in the order of `companyIds`.
 */
export function useGetCompanyInfoQueries(params: {
  companyIds: MaybeRefOrGetter<string[]>;
}): Readonly<Ref<QueryObserverResult<CompanyInformation, Error>[]>> {
  const apiClientProvider = useApiClient();

  return useQueries({
    queries: computed(() =>
      toValue(params.companyIds).map((companyId) => ({
        queryKey: companyDataKeys.info(companyId),
        queryFn: async (): Promise<CompanyInformation> => {
          const response = await apiClientProvider.backendClients.companyDataController.getCompanyInfo(companyId);
          return response.data;
        },
      }))
    ),
  });
}

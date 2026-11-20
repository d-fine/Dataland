import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { companyDataKeys } from '@/api-queries/backend/company-data/companyDataKeys';
import { type CompanyInformation } from '@clients/backend';

/**
 * Vue Query hook that fetches the information of a company (GET /companies/{companyId}/info).
 * The query is disabled while `companyId` is empty.
 *
 * @param params - Endpoint parameters.
 * @param params.companyId - Id of the company.
 * @param options - Additional TanStack query options. A passed `enabled` replaces the default.
 * @returns Vue Query result containing the {@link CompanyInformation}.
 */
export function useGetCompanyInfoQuery(
  params: { companyId: MaybeRefOrGetter<string | undefined> },
  options: Omit<UseQueryOptions<CompanyInformation, Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<CompanyInformation, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<CompanyInformation, Error>({
    queryKey: computed(() => companyDataKeys.info(toValue(params.companyId))),
    queryFn: async () => {
      const companyId = toValue(params.companyId);
      if (!companyId) throw new Error('companyId is undefined');
      const response = await apiClientProvider.backendClients.companyDataController.getCompanyInfo(companyId);
      return response.data;
    },
    enabled: computed(() => !!toValue(params.companyId)),
    ...options,
  });
}

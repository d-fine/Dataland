import { type Ref, computed } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient.ts';
import type { ApiClientProvider } from '@/services/ApiClients.ts';
import { companyDataKeys } from '@/api-queries/backend/company-data/companyDataKeys.ts';
import type { CompanyInformation } from '@clients/backend';

/**
 * Builds the query options (key and fetch function) for loading company information by id.
 * Shared between the single query hook and multi-query usages (e.g. useQueries).
 *
 * @param {string} companyId - The id of the company.
 * @param {ApiClientProvider} apiClientProvider - The provider used to access the backend clients.
 * @returns the query options
 */
export function getCompanyInformationQueryOptions(
  companyId: string,
  apiClientProvider: ApiClientProvider
): { queryKey: ReturnType<typeof companyDataKeys.byCompanyId>; queryFn: () => Promise<CompanyInformation> } {
  return {
    queryKey: companyDataKeys.byCompanyId(companyId),
    queryFn: async (): Promise<CompanyInformation> => {
      const response = await apiClientProvider.backendClients.companyDataController.getCompanyInfo(companyId);
      return response.data;
    },
  };
}

/**
 * Vue Query hook that fetches stored company information for a given company id.
 *
 * @param {Ref<string|undefined>} companyId - Reactive reference containing the company id; query runs when truthy.
 * @returns {UseQueryReturnType<CompanyInformation, Error>} Query result containing the company information.
 */
export function useGetCompanyInformationByIdInfoQuery(
  companyId: Ref<string | undefined>
): UseQueryReturnType<CompanyInformation, Error> {
  const apiClientProvider = useApiClient();
  const queryKey = computed(() => companyDataKeys.byCompanyId(companyId.value));
  const enabled = computed(() => !!companyId.value);

  return useQuery<CompanyInformation, Error>({
    queryKey,
    enabled,
    queryFn: async () => {
      if (!companyId.value) {
        throw new Error('companyId is undefined');
      }
      return getCompanyInformationQueryOptions(companyId.value, apiClientProvider).queryFn();
    },
  });
}

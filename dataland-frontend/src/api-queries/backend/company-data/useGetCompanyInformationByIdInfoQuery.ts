import { type Ref, computed } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient.ts';
import { companyDataKeys } from '@/api-queries/backend/company-data/companyDataKeys.ts';
import type { CompanyInformation } from '@clients/backend';

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
      const response = await apiClientProvider.backendClients.companyDataController.getCompanyInfo(companyId.value);
      return response.data;
    },
  });
}

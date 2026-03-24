import { type Ref, computed } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient.ts';
import { companyDataKeys } from '@/api-queries/backend/company-data/companyDataKeys.ts';
import type { StoredCompany } from '@clients/backend';

/**
 * Vue Query hook that fetches stored company information for a given company id.
 *
 * @param {Ref<string|undefined>} companyId - Reactive reference containing the company id; query runs when truthy.
 * @returns {UseQueryReturnType<StoredCompany, Error>} Query result containing the stored company.
 */
export function useGetCompanyInformationQuery(
  companyId: Ref<string | undefined>
): UseQueryReturnType<StoredCompany, Error> {
  const apiClientProvider = useApiClient();
  const queryKey = computed(() => companyDataKeys.byCompanyId(companyId.value));
  const enabled = computed(() => !!companyId.value);

  return useQuery<StoredCompany, Error>({
    queryKey,
    enabled,
    queryFn: async () => {
      if (!companyId.value) {
        throw new Error('companyId is undefined');
      }
      const response = await apiClientProvider.backendClients.companyDataController.getCompanyById(companyId.value);
      return response.data;
    },
  });
}

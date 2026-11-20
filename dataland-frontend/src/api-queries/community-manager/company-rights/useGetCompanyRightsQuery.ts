import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { companyRightsKeys } from '@/api-queries/community-manager/company-rights/companyRightsKeys';

/**
 * Vue Query hook that fetches the rights of a company, e.g. its Dataland membership
 * (GET /community/company-rights/{companyId}).
 * The query is disabled while `companyId` is empty.
 *
 * @param params - Endpoint parameters.
 * @param params.companyId - Id of the company.
 * @param options - Additional TanStack query options. A passed `enabled` replaces the default.
 * @returns Vue Query result containing the names of the company rights.
 */
export function useGetCompanyRightsQuery(
  params: { companyId: MaybeRefOrGetter<string | undefined> },
  options: Omit<UseQueryOptions<string[], Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<string[], Error> {
  const apiClientProvider = useApiClient();

  return useQuery<string[], Error>({
    queryKey: computed(() => companyRightsKeys.byCompanyId(toValue(params.companyId))),
    queryFn: async () => {
      const companyId = toValue(params.companyId);
      if (!companyId) throw new Error('companyId is undefined');
      const response = await apiClientProvider.apiClients.companyRightsController.getCompanyRights(companyId);
      return response.data;
    },
    enabled: computed(() => !!toValue(params.companyId)),
    ...options,
  });
}

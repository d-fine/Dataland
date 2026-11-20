import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { isAxiosError } from 'axios';
import { useApiClient } from '@/utils/useApiClient';
import { companyRolesKeys } from '@/api-queries/community-manager/company-roles/companyRolesKeys';

/**
 * Vue Query hook that checks whether a company has at least one company owner (HEAD /company-ownership/{companyId}).
 * The endpoint answers with 404 if the company has no owner. This hook maps that response to `false`, so that the
 * answer is cached like any other result; all other errors are thrown. Role assignment mutations invalidate the
 * result, because it lives under the company role keys.
 * The query is disabled while `companyId` is empty.
 *
 * @param params - Endpoint parameters.
 * @param params.companyId - Id of the company.
 * @param options - Additional TanStack query options. A passed `enabled` replaces the default.
 * @returns Vue Query result containing whether the company has at least one owner.
 */
export function useHasCompanyAtLeastOneOwnerQuery(
  params: { companyId: MaybeRefOrGetter<string | undefined> },
  options: Omit<UseQueryOptions<boolean, Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<boolean, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<boolean, Error>({
    queryKey: computed(() => companyRolesKeys.hasCompanyAtLeastOneOwner(toValue(params.companyId))),
    queryFn: async () => {
      const companyId = toValue(params.companyId);
      if (!companyId) throw new Error('companyId is undefined');
      try {
        await apiClientProvider.apiClients.companyRolesController.hasCompanyAtLeastOneOwner(companyId);
        return true;
      } catch (error) {
        if (isAxiosError(error) && error.response?.status === 404) return false;
        throw error;
      }
    },
    enabled: computed(() => !!toValue(params.companyId)),
    ...options,
  });
}

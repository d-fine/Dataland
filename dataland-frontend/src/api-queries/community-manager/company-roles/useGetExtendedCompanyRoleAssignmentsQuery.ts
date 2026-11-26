import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { companyRolesKeys } from '@/api-queries/community-manager/company-roles/companyRolesKeys';
import { type CompanyRole, type CompanyRoleAssignmentExtended } from '@clients/communitymanager';

/**
 * Vue Query hook that fetches company role assignments including user details, filtered by role, company and user
 * (GET /community/company-role-assignments).
 * The query is disabled while all filters are empty, because an unfiltered request lists all assignments on Dataland.
 *
 * @param params - Endpoint parameters. Undefined filters are not applied.
 * @param params.role - Only return assignments of this role.
 * @param params.companyId - Only return assignments for this company.
 * @param params.userId - Only return assignments of this user.
 * @param options - Additional TanStack query options. A passed `enabled` replaces the default.
 * @returns Vue Query result containing the matching {@link CompanyRoleAssignmentExtended}s.
 */
export function useGetExtendedCompanyRoleAssignmentsQuery(
  params: {
    role?: MaybeRefOrGetter<CompanyRole | undefined>;
    companyId?: MaybeRefOrGetter<string | undefined>;
    userId?: MaybeRefOrGetter<string | undefined>;
  },
  options: Omit<UseQueryOptions<CompanyRoleAssignmentExtended[], Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<CompanyRoleAssignmentExtended[], Error> {
  const apiClientProvider = useApiClient();

  return useQuery<CompanyRoleAssignmentExtended[], Error>({
    queryKey: computed(() =>
      companyRolesKeys.assignments({
        role: toValue(params.role),
        companyId: toValue(params.companyId),
        userId: toValue(params.userId),
      })
    ),
    queryFn: async () => {
      const response = await apiClientProvider.apiClients.companyRolesController.getExtendedCompanyRoleAssignments(
        toValue(params.role),
        toValue(params.companyId),
        toValue(params.userId)
      );
      return response.data;
    },
    enabled: computed(() => !!toValue(params.role) || !!toValue(params.companyId) || !!toValue(params.userId)),
    ...options,
  });
}

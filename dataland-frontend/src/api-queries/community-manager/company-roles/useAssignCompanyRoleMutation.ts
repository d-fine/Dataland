import { useMutation, useQueryClient, type UseMutationReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { companyRolesKeys } from '@/api-queries/community-manager/company-roles/companyRolesKeys';
import { inheritedRolesKeys } from '@/api-queries/community-manager/inherited-roles/inheritedRolesKeys';
import { type CompanyRole, type CompanyRoleAssignment } from '@clients/communitymanager';

export interface AssignCompanyRoleVariables {
  role: CompanyRole;
  companyId: string;
  userId: string;
}

/**
 * Vue Query mutation hook that assigns a company role to a user, replacing the user's previous role for the company
 * (POST /community/company-role-assignments/{role}/{companyId}/{userId}).
 * On success, all company role and inherited role queries are invalidated. The invalidation is not awaited, because the
 * refreshed roles can remove the calling component, e.g. when users change their own role.
 *
 * @returns Mutation result resolving to the created {@link CompanyRoleAssignment}.
 */
export function useAssignCompanyRoleMutation(): UseMutationReturnType<
  CompanyRoleAssignment,
  Error,
  AssignCompanyRoleVariables,
  unknown
> {
  const queryClient = useQueryClient();
  const apiClientProvider = useApiClient();

  return useMutation({
    mutationFn: async ({ role, companyId, userId }: AssignCompanyRoleVariables) => {
      const response = await apiClientProvider.apiClients.companyRolesController.assignCompanyRole(
        role,
        companyId,
        userId
      );
      return response.data;
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: companyRolesKeys.all });
      void queryClient.invalidateQueries({ queryKey: inheritedRolesKeys.all });
    },
  });
}

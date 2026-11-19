import { useMutation, useQueryClient, type UseMutationReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { companyRolesKeys } from '@/api-queries/community-manager/company-roles/companyRolesKeys';
import { type CompanyRole, type CompanyRoleAssignment } from '@clients/communitymanager';

export interface AssignCompanyRoleVariables {
  role: CompanyRole;
  companyId: string;
  userId: string;
}

/**
 * Vue Query mutation hook that assigns a company role to a user, replacing the user's previous role for the company
 * (POST /community/company-role-assignments/{role}/{companyId}/{userId}).
 * On success, all company role queries are invalidated, because the user moves between role lists.
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
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: companyRolesKeys.all });
    },
  });
}

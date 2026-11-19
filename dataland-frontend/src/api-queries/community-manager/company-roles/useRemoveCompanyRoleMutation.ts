import { useMutation, useQueryClient, type UseMutationReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { companyRolesKeys } from '@/api-queries/community-manager/company-roles/companyRolesKeys';
import { type CompanyRole } from '@clients/communitymanager';

export interface RemoveCompanyRoleVariables {
  role: CompanyRole;
  companyId: string;
  userId: string;
}

/**
 * Vue Query mutation hook that removes a company role from a user
 * (DELETE /community/company-role-assignments/{role}/{companyId}/{userId}).
 * On success, all company role queries are invalidated.
 *
 * @returns Mutation result resolving once the role is removed.
 */
export function useRemoveCompanyRoleMutation(): UseMutationReturnType<
  void,
  Error,
  RemoveCompanyRoleVariables,
  unknown
> {
  const queryClient = useQueryClient();
  const apiClientProvider = useApiClient();

  return useMutation({
    mutationFn: async ({ role, companyId, userId }: RemoveCompanyRoleVariables) => {
      const response = await apiClientProvider.apiClients.companyRolesController.removeCompanyRole(
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

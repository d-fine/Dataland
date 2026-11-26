import { useMutation, useQueryClient, type UseMutationReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { companyRolesKeys } from '@/api-queries/community-manager/company-roles/companyRolesKeys';
import { inheritedRolesKeys } from '@/api-queries/community-manager/inherited-roles/inheritedRolesKeys';
import { type CompanyRole } from '@clients/communitymanager';

export interface RemoveCompanyRoleVariables {
  role: CompanyRole;
  companyId: string;
  userId: string;
}

/**
 * Vue Query mutation hook that removes a company role from a user
 * (DELETE /community/company-role-assignments/{role}/{companyId}/{userId}).
 * On success, all company role and inherited role queries are invalidated. The invalidation is not awaited, because the
 * refreshed roles can remove the calling component, e.g. when users change their own role.
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
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: companyRolesKeys.all });
      void queryClient.invalidateQueries({ queryKey: inheritedRolesKeys.all });
    },
  });
}

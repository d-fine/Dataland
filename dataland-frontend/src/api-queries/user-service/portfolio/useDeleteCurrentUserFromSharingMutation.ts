import { useMutation, useQueryClient, type UseMutationReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { portfolioKeys } from '@/api-queries/user-service/portfolio/portfolioKeys';

export interface DeleteCurrentUserFromSharingVariables {
  portfolioId: string;
}

/**
 * Vue Query mutation hook that removes the current user's access to a portfolio shared with them
 * (DELETE /users/portfolios/shared/{portfolioId}).
 * On success, the shared portfolio names of the current user are invalidated. The portfolio's own queries are not
 * invalidated, as the user can no longer read them.
 *
 * @returns Mutation result resolving once the access is removed.
 */
export function useDeleteCurrentUserFromSharingMutation(): UseMutationReturnType<
  void,
  Error,
  DeleteCurrentUserFromSharingVariables,
  unknown
> {
  const queryClient = useQueryClient();
  const apiClientProvider = useApiClient();

  return useMutation({
    mutationFn: async ({ portfolioId }: DeleteCurrentUserFromSharingVariables) => {
      const response = await apiClientProvider.apiClients.portfolioController.deleteCurrentUserFromSharing(portfolioId);
      return response.data;
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: portfolioKeys.sharedNames() });
    },
  });
}

import { useMutation, useQueryClient, type UseMutationReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { portfolioKeys } from '@/api-queries/user-service/portfolio/portfolioKeys';

export interface DeletePortfolioVariables {
  portfolioId: string;
}

/**
 * Vue Query mutation hook that deletes a portfolio (DELETE /users/portfolios/{portfolioId}/).
 * On success, the portfolio names of the current user are invalidated. The deleted portfolio's own queries are not
 * invalidated, as refetching them would fail; they are garbage collected once no component uses them anymore.
 *
 * @returns Mutation result resolving once the portfolio is deleted.
 */
export function useDeletePortfolioMutation(): UseMutationReturnType<void, Error, DeletePortfolioVariables, unknown> {
  const queryClient = useQueryClient();
  const apiClientProvider = useApiClient();

  return useMutation({
    mutationFn: async ({ portfolioId }: DeletePortfolioVariables) => {
      const response = await apiClientProvider.apiClients.portfolioController.deletePortfolio(portfolioId);
      return response.data;
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: portfolioKeys.names() });
    },
  });
}

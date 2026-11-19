import { useMutation, useQueryClient, type UseMutationReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { portfolioKeys } from '@/api-queries/user-service/portfolio/portfolioKeys';
import { type BasePortfolio, type PortfolioUpload } from '@clients/userservice';

export interface CreatePortfolioVariables {
  portfolioUpload: PortfolioUpload;
}

/**
 * Vue Query mutation hook that creates a portfolio for the current user (POST /users/portfolios/).
 * On success, the portfolio names of the current user are invalidated.
 *
 * @returns Mutation result resolving to the created {@link BasePortfolio}.
 */
export function useCreatePortfolioMutation(): UseMutationReturnType<
  BasePortfolio,
  Error,
  CreatePortfolioVariables,
  unknown
> {
  const queryClient = useQueryClient();
  const apiClientProvider = useApiClient();

  return useMutation({
    mutationFn: async ({ portfolioUpload }: CreatePortfolioVariables) => {
      const response = await apiClientProvider.apiClients.portfolioController.createPortfolio(portfolioUpload);
      return response.data;
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: portfolioKeys.names() });
    },
  });
}

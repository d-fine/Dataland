import { useMutation, useQueryClient, type UseMutationReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { portfolioKeys } from '@/api-queries/user-service/portfolio/portfolioKeys';
import { type BasePortfolio, type PortfolioUpload } from '@clients/userservice';

export interface ReplacePortfolioVariables {
  portfolioId: string;
  portfolioUpload: PortfolioUpload;
}

/**
 * Vue Query mutation hook that replaces an existing portfolio (PUT /users/portfolios/{portfolioId}/).
 * On success, the portfolio names of the current user and the enriched portfolio are invalidated.
 *
 * @returns Mutation result resolving to the updated {@link BasePortfolio}.
 */
export function useReplacePortfolioMutation(): UseMutationReturnType<
  BasePortfolio,
  Error,
  ReplacePortfolioVariables,
  unknown
> {
  const queryClient = useQueryClient();
  const apiClientProvider = useApiClient();

  return useMutation({
    mutationFn: async ({ portfolioId, portfolioUpload }: ReplacePortfolioVariables) => {
      const response = await apiClientProvider.apiClients.portfolioController.replacePortfolio(
        portfolioId,
        portfolioUpload
      );
      return response.data;
    },
    onSuccess: async (_data, variables) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: portfolioKeys.names() }),
        queryClient.invalidateQueries({ queryKey: portfolioKeys.enriched(variables.portfolioId) }),
      ]);
    },
  });
}

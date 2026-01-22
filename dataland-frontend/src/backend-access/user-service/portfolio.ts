import {
    keepPreviousData,
    QueryClient,
    useMutation,
    UseMutationReturnType,
    useQuery,
    UseQueryReturnType
} from '@tanstack/vue-query';
import { MaybeRef, unref } from 'vue';
import { useApiClientProvider } from '@/backend-access/apiClientProviderHelper.ts';
import type {BasePortfolio, BasePortfolioName, EnrichedPortfolio, PortfolioUpload} from "@clients/userservice";

export const portfolioControllerKeys = {
  all: ['portfolioController'] as const,
  enriched: (id: string) => [...portfolioControllerKeys.all, 'getEnrichedPortfolio', id] as const,
  base: (id: string) => [...portfolioControllerKeys.all, 'getPortfolio', id] as const,
  allForUser: () => [...portfolioControllerKeys.all, 'getAllPortfolioNamesForCurrentUser'] as const,
};

/**
 * Factory function that creates a query to fetch an enriched portfolio by its ID.
 * @param portfolioId the ID of the portfolio to fetch.
 */
export function useGetEnrichedPortfolio(portfolioId: MaybeRef<string>): UseQueryReturnType<EnrichedPortfolio, Error> {
  const apiClientProvider = useApiClientProvider();

  return useQuery({
    queryKey: portfolioControllerKeys.enriched(unref(portfolioId)),
    enabled: () => !!unref(portfolioId),
    queryFn: async () => {
      const id = unref(portfolioId);
      const apiResponse = await apiClientProvider.apiClients.portfolioController.getEnrichedPortfolio(id);
      return apiResponse.data;
    },
    placeholderData: keepPreviousData,
  });
}

/**
 * Factory function that creates a query to fetch a portfolio by its ID.
 * @param portfolioId the ID of the portfolio to fetch.
 */
export function useGetPortfolio(portfolioId: MaybeRef<string>): UseQueryReturnType<BasePortfolio, Error> {
  const apiClientProvider = useApiClientProvider();

  return useQuery({
    queryKey: portfolioControllerKeys.base(unref(portfolioId)),
    enabled: () => !!unref(portfolioId),
    queryFn: async () => {
      const id = unref(portfolioId);
      const apiResponse = await apiClientProvider.apiClients.portfolioController.getPortfolio(id);
      return apiResponse.data;
    },
    placeholderData: keepPreviousData,
  });
}

/**
 * Factory function that creates a query to fetch all portfolio names for the current user.
 */
export function useGetAllPortfolioNamesForCurrentUser(): UseQueryReturnType<BasePortfolioName[], Error> {
  const apiClientProvider = useApiClientProvider();

  return useQuery({
    queryKey: portfolioControllerKeys.allForUser(),
    queryFn: async () => {
      const apiResponse = await apiClientProvider.apiClients.portfolioController.getAllPortfolioNamesForCurrentUser();
      return apiResponse.data;
    },
  });
}

/**
 * Factory function that creates a mutation to create a new portfolio.
 */
export function useCreatePortfolio(queryClient: QueryClient) {
  const apiClientProvider = useApiClientProvider();

  return useMutation({
      mutationFn: (variables: { portfolioUpload: PortfolioUpload }) => {
          return apiClientProvider.apiClients.portfolioController.createPortfolio(variables.portfolioUpload);
      },
      onSuccess: async () => {
          await queryClient.invalidateQueries({queryKey: portfolioControllerKeys.allForUser()});
      },
  });
}

/**
 * Factory function that creates a mutation to replace an existing portfolio.
 * @param portfolioId the ID of the portfolio to replace.
 */
export function useReplacePortfolio(portfolioId: MaybeRef<string>, queryClient: QueryClient) {
    const apiClientProvider = useApiClientProvider();

    return useMutation({
        mutationFn: (variables: { portfolioUpload: PortfolioUpload }) => {
            const id = unref(portfolioId);
            return apiClientProvider.apiClients.portfolioController.replacePortfolio(id, variables.portfolioUpload);
        },
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: portfolioControllerKeys.allForUser()});
            await queryClient.invalidateQueries({queryKey: portfolioControllerKeys.base(unref(portfolioId))});
            await queryClient.invalidateQueries({queryKey: portfolioControllerKeys.enriched(unref(portfolioId))});
        },
    });
}
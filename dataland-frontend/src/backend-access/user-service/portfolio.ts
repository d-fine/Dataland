import { keepPreviousData, useQuery } from '@tanstack/vue-query';
import { MaybeRef, type Ref, unref } from 'vue';
import { useApiClientProvider } from '@/backend-access/apiClientProviderHelper.ts';

export const portfolioControllerKeys = {
  all: ['portfolioController'] as const,
  enriched: (id: string) => [...portfolioControllerKeys.all, 'enriched', id] as const,
  base: (id: string) => [...portfolioControllerKeys.all, 'base', id] as const,
  allForUser: () => [...portfolioControllerKeys.all, 'allForUser'] as const,
};

export function useGetEnrichedPortfolio(portfolioId: MaybeRef<string>) {
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

export function useGetPortfolio(portfolioId: MaybeRef<string>) {
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

export function useGetAllPortfolioNamesForCurrentUser() {
  const apiClientProvider = useApiClientProvider();

  return useQuery({
    queryKey: portfolioControllerKeys.allForUser(),
    queryFn: async () => {
      const apiResponse = await apiClientProvider.apiClients.portfolioController.getAllPortfolioNamesForCurrentUser();
      return apiResponse.data;
    },
  });
}

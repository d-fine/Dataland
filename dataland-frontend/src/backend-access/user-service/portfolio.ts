import { keepPreviousData, useQuery } from '@tanstack/vue-query';
import { type Ref, unref } from 'vue';
import { useApiClientProvider } from '@/backend-access/apiClientProviderHelper.ts';

export const portfolioControllerKeys = {
  all: ['portfolioController'] as const,
  enriched: (id: string) => [...portfolioControllerKeys.all, 'enriched', id] as const,
  base: (id: string) => [...portfolioControllerKeys.all, 'base', id] as const,
};

export function useGetEnrichedPortfolio(portfolioId: Ref<string>) {
  const apiClientProvider = useApiClientProvider();

  return useQuery({
    queryKey: portfolioControllerKeys.enriched(unref(portfolioId)),
    queryFn: async () => {
      const id = unref(portfolioId);
      const apiResponse = await apiClientProvider.apiClients.portfolioController.getEnrichedPortfolio(id);
      return apiResponse.data;
    },
    placeholderData: keepPreviousData,
  });
}

export function useGetPortfolio(portfolioId: Ref<string>) {
  const apiClientProvider = useApiClientProvider();

  return useQuery({
    queryKey: portfolioControllerKeys.base(unref(portfolioId)),
    queryFn: async () => {
      const id = unref(portfolioId);
      const apiResponse = await apiClientProvider.apiClients.portfolioController.getPortfolio(id);
      return apiResponse.data;
    },
    placeholderData: keepPreviousData,
  });
}

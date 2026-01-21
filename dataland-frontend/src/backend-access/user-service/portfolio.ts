import { keepPreviousData, useQuery } from '@tanstack/vue-query';
import { inject, type Ref, unref } from 'vue';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { apiClientProviderKey } from '@/services/ApiClients.ts';

export const portfolioControllerKeys = {
  all: ['portfolioController'] as const,
  detail: (portFolioId: string) => [...portfolioControllerKeys.all, portFolioId] as const,
};

export function useGetEnrichedPortfolio(portfolioId: Ref<string>) {
  const apiClientProvider = assertDefined(inject(apiClientProviderKey));

  return useQuery({
    queryKey: portfolioControllerKeys.detail(unref(portfolioId)),
    queryFn: async () => {
      const id = unref(portfolioId);
      const apiResponse = await apiClientProvider.apiClients.portfolioController.getEnrichedPortfolio(id);

      return apiResponse.data;
    },
    placeholderData: keepPreviousData,
  });
}

export function useGetPortfolio(portfolioId: Ref<string>) {
  const apiClientProvider = assertDefined(inject(apiClientProviderKey));

  return useQuery({
    queryKey: portfolioControllerKeys.detail(unref(portfolioId)),
    queryFn: async () => {
      const id = unref(portfolioId);
      const apiResponse = await apiClientProvider.apiClients.portfolioController.getPortfolio(id);
      return apiResponse.data;
    },
    placeholderData: keepPreviousData,
  });
}

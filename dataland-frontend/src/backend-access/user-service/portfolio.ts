import { keepPreviousData, useQuery } from '@tanstack/vue-query';
import { inject, type Ref, unref } from 'vue';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { ApiClientProvider } from '@/services/ApiClients';
import type Keycloak from 'keycloak-js';

export const portfolioControllerKeys = {
  all: ['portfolioController'] as const,
  enriched: (id: string) => [...portfolioControllerKeys.all, 'enriched', id] as const,
  base: (id: string) => [...portfolioControllerKeys.all, 'base', id] as const,
};

export function useGetEnrichedPortfolio(portfolioId: Ref<string>) {
  const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
  const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

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
  const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
  const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

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

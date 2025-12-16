import { computed, inject, Ref, unref } from 'vue';
import Keycloak from 'keycloak-js';
import { useQuery } from '@tanstack/vue-query';
import { queryKeys } from '@/queries/queryKeys.ts';
import { useApiClient } from '@/utils/api/useApiClient.ts';

export function useIsDatalandMemberQuery(companyId: Ref<string> | string) {
  const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!;
  if (!getKeycloakPromise) {
    throw new Error('Keycloak not provided!');
  }

  const apiClientProvider = useApiClient();
  const id = computed(() => unref(companyId));

  return useQuery<boolean>({
    queryKey: queryKeys.companyRights(id.value),
    enabled: computed(() => !!id.value),
    initialData: false,
    queryFn: async () => {
      const response = await apiClientProvider.apiClients.companyRightsController.getCompanyRights(id.value);
      return response.data.some((right) => right.includes('Member'));
    },
  });
}

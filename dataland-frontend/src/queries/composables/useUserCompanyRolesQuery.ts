// src/queries/composables/useUserCompanyRolesQuery.ts
import { useQuery } from '@tanstack/vue-query';
import { computed, inject, unref, type Ref } from 'vue';
import type Keycloak from 'keycloak-js';

import { queryKeys } from '@/queries/queryKeys';
import { useApiClient } from '@/utils/api/useApiClient.ts'; // adjust type if needed

export function useUserCompanyRolesQuery(companyId: Ref<string> | string) {
  const id = computed(() => unref(companyId));

  const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
  if (!getKeycloakPromise) throw new Error('Keycloak not provided!');
  const apiClientProvider = useApiClient();

  return useQuery({
    queryKey: computed(() => queryKeys.userCompanyRoles(id.value)),
    enabled: computed(() => !!id.value),
    initialData: [],
    queryFn: async () => {
      const keycloak = await getKeycloakPromise();
      const userId = keycloak.idTokenParsed?.sub;
      if (!userId) return [];

      const response = await apiClientProvider.apiClients.companyRolesController.getExtendedCompanyRoleAssignments(
        undefined,
        id.value,
        userId
      );
      return response.data;
    },
  });
}

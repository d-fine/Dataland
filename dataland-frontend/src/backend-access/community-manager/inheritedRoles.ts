import { useQuery } from '@tanstack/vue-query';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { inject } from 'vue';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils.ts';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakRoles.ts';

export const inheritedRolesKeys = {
  all: ['inheritedRoles'] as const,
  permissions: () => [...inheritedRolesKeys.all, 'permissions'] as const,
};

export function useIsUserDatalandMemberOrAdmin() {
  const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
  const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

  return useQuery({
    queryKey: inheritedRolesKeys.permissions(),
    queryFn: async () => {
      const keycloakFn = assertDefined(getKeycloakPromise);
      const keycloak = await keycloakFn();

      const keyCloakUserId = keycloak.idTokenParsed?.sub;

      if (!keyCloakUserId) {
        return false;
      }

      const [apiResponse, isAdmin] = await Promise.all([
        apiClientProvider.apiClients.inheritedRolesController.getInheritedRoles(keyCloakUserId),
        checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, keycloakFn),
      ]);

      const inheritedRolesMap = apiResponse.data;
      const isMember = Object.values(inheritedRolesMap).flat().includes('DatalandMember');

      return isMember || isAdmin;
    },
  });
}

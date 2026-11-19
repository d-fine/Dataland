import type Keycloak from 'keycloak-js';
import { computed, inject, ref, type ComputedRef } from 'vue';
import { assertDefined } from '@/utils/TypeScriptUtils';

export interface CurrentUser {
  /** Keycloak id of the logged-in user. Undefined for anonymous users and until Keycloak is resolved. */
  userId: ComputedRef<string | undefined>;
  /** Keycloak realm roles of the logged-in user. Empty for anonymous users and until Keycloak is resolved. */
  roles: ComputedRef<readonly string[]>;
  /** True once Keycloak is resolved, for logged-in and anonymous users alike. */
  isResolved: ComputedRef<boolean>;
}

/**
 * Resolves the logged-in user from the injected Keycloak promise once per component. Use the returned refs as
 * parameters or `enabled` conditions of queries, so that user-specific queries start as soon as Keycloak is resolved.
 * @returns the id, the realm roles and the resolution state of the current user
 */
export function useCurrentUser(): CurrentUser {
  const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
  const userId = ref<string | undefined>(undefined);
  const roles = ref<string[]>([]);
  const isResolved = ref(false);

  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      if (keycloak.authenticated) {
        userId.value = keycloak.idTokenParsed?.sub;
        roles.value = keycloak.realmAccess?.roles ?? [];
      }
    })
    .catch((error) => console.log(error))
    .finally(() => {
      isResolved.value = true;
    });

  return {
    userId: computed(() => userId.value),
    roles: computed(() => roles.value),
    isResolved: computed(() => isResolved.value),
  };
}

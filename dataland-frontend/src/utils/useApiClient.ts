import type Keycloak from 'keycloak-js';
import { inject } from 'vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';

/**
 * Helper that constructs and returns an ApiClientProvider using the
 * Keycloak instance promise injected into the current Vue app context.
 * @returns {ApiClientProvider} An ApiClientProvider initialized with the
 * Keycloak promise from the Vue injection.
 * @throws {Error} If the `getKeycloakPromise` injection is not present.
 */
export function useApiClient(): ApiClientProvider {
  const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
  return new ApiClientProvider(assertDefined(getKeycloakPromise)());
}

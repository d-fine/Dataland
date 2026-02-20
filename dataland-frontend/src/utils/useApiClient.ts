import Keycloak from 'keycloak-js';
import { inject } from 'vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';

export function useApiClient() {
  const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
  return new ApiClientProvider(assertDefined(getKeycloakPromise)());
}

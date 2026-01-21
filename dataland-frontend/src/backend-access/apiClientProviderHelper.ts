import { ApiClientProvider } from '@/services/ApiClients.ts';
import { inject } from 'vue';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import Keycloak from 'keycloak-js';

export function useApiClientProvider(): ApiClientProvider {
  const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
  return new ApiClientProvider(assertDefined(getKeycloakPromise)());
}

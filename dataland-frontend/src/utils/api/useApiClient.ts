import { inject } from 'vue';
import Keycloak from 'keycloak-js';
import { ApiClientProvider } from '@/services/ApiClients';

export function useApiClient() {
    const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
    if (!getKeycloakPromise) throw new Error("Keycloak not provided!");

    return new ApiClientProvider(getKeycloakPromise());
}
import { ApiClientProvider } from '@/services/ApiClients';
import { type RequestState } from '@clients/datasourcingservice';
import type Keycloak from 'keycloak-js';

/**
 * Patches the RequestState of a DataRequest
 * @param dataRequestId
 * @param requestState
 * @param keycloakPromiseGetter
 */
export async function patchRequestState(
    dataRequestId: string,
    requestState: RequestState,
    keycloakPromiseGetter?: () => Promise<Keycloak>
): Promise<void> {
  try {
    if (keycloakPromiseGetter) {
      await new ApiClientProvider(
          keycloakPromiseGetter()
      ).apiClients.requestController.patchRequestState(dataRequestId, requestState);
    }
  } catch (error) {
    console.error(error);
    throw error;
  }
}

/**
 * Defines the color of p-badge
 * @param requestState state of a request
 * @returns p-badge class
 */
export function badgeClass(requestState: RequestState): string {
  switch (requestState) {
    case 'Processing':
      return 'p-badge badge-blue outline rounded';
    case 'Open':
      return 'p-badge badge-yellow outline rounded';
    case 'Processed':
      return 'p-badge badge-light-green outline rounded';
    case 'Withdrawn':
      return 'p-badge badge-gray outline rounded';
    default:
      return 'p-badge outline rounded';
  }
}


import { ApiClientProvider } from '@/services/ApiClients';
import { RequestStatus } from '@clients/communitymanager';
import type Keycloak from 'keycloak-js';

/**
 * Patches the RequestStatus of a StoredDataRequest
 * @param dataRequestId the dataland dataRequestId
 * @param requestStatus the desired requestStatus
 * @param contacts set of email contacts
 * @param message context of the email
 * @param notifyMeImmediately
 * @param requestStatusChangeReason provided reason why data should be available
 * @param keycloakPromiseGetter the getter-function which returns a Keycloak-Promise
 */
export async function patchDataRequest(
  dataRequestId: string,
  requestStatus: RequestStatus | undefined,
  contacts: Set<string> | undefined,
  message: string | undefined,
  notifyMeImmediately: boolean | undefined,
  requestStatusChangeReason: string | undefined,
  keycloakPromiseGetter?: () => Promise<Keycloak>
): Promise<void> {
  try {
    if (keycloakPromiseGetter) {
      await new ApiClientProvider(
        keycloakPromiseGetter()
      ).apiClients.communityManagerRequestController.patchDataRequest(dataRequestId, {
        requestStatus: requestStatus,
        contacts: contacts,
        message: message,
        notifyMeImmediately: notifyMeImmediately,
        requestStatusChangeReason: requestStatusChangeReason,
      });
    }
  } catch (error) {
    console.error(error);
    throw error;
  }
}

/**
 * Defines the color of p-badge
 * @param requestStatus status of a request
 * @returns p-badge class
 */
export function badgeClass(requestStatus: RequestStatus): string {
  switch (requestStatus) {
    case 'Answered':
      return 'p-badge badge-blue outline rounded';
    case 'Open':
      return 'p-badge badge-yellow outline rounded';
    case 'Resolved':
      return 'p-badge badge-light-green outline rounded';
    case 'Withdrawn':
      return 'p-badge badge-gray outline rounded';
    case 'Closed':
      return 'p-badge badge-brown outline rounded';
    case 'NonSourceable':
      return 'p-badge badge-gray outline rounded';
    default:
      return 'p-badge outline rounded';
  }
}

/**
 * Gives back a different string for status nonSourceable, otherwise a string that similar to requestStatus
 * @param requestStatus request status of a request
 * @returns the label of the request status
 */
export function getRequestStatusLabel(requestStatus: RequestStatus): string {
  return requestStatus === RequestStatus.NonSourceable ? 'No sources available' : requestStatus;
}

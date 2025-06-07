import type Keycloak from 'keycloak-js';
import {
  type AccessStatus,
  RequestStatus,
  type RequestPriority,
  type BulkDataRequestDataTypesEnum,
  type BulkDataRequest,
} from '@clients/communitymanager';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { inject } from 'vue';

/**
 * Patches the RequestStatus of a StoredDataRequest
 * @param dataRequestId the dataland dataRequestId
 * @param requestStatus the desired requestStatus
 * @param accessStatus the desired access status
 * @param contacts set of email contacts
 * @param message context of the email
 * @param notifyMeImmediately
 * @param requestStatusChangeReason provided reason why data should be available
 * @param keycloakPromiseGetter the getter-function which returns a Keycloak-Promise
 */
export async function patchDataRequest(
  dataRequestId: string,
  requestStatus: RequestStatus | undefined,
  accessStatus: AccessStatus | undefined,
  contacts: Set<string> | undefined,
  message: string | undefined,
  notifyMeImmediately: boolean | undefined,
  requestStatusChangeReason: string | undefined,
  keycloakPromiseGetter?: () => Promise<Keycloak>
): Promise<void> {
  try {
    if (keycloakPromiseGetter) {
      await new ApiClientProvider(keycloakPromiseGetter()).apiClients.requestController.patchDataRequest(
        dataRequestId,
        {
          requestStatus: requestStatus,
          accessStatus: accessStatus,
          contacts: contacts,
          message: message,
          notifyMeImmediately: notifyMeImmediately,
          requestStatusChangeReason: requestStatusChangeReason,
        }
      );
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
 * Defines the color of p-badge
 * @param accessStatus access status of a request
 * @returns p-badge class
 */
export function accessStatusBadgeClass(accessStatus: AccessStatus): string {
  switch (accessStatus) {
    case 'Public':
      return 'p-badge badge-blue outline rounded';
    case 'Pending':
      return 'p-badge badge-yellow outline rounded';
    case 'Granted':
      return 'p-badge badge-light-green outline rounded';
    case 'Revoked':
      return 'p-badge badge-gray outline rounded';
    case 'Declined':
      return 'p-badge badge-brown outline rounded';
    default:
      return 'p-badge outline rounded';
  }
}

/**
 * Defines the color of p-badge
 * @param priority priority of a request
 * @returns p-badge class
 */
export function priorityBadgeClass(priority: RequestPriority): string {
  switch (priority) {
    case 'Low':
      return 'p-badge badge-blue outline rounded';
    case 'Baseline':
      return 'p-badge badge-yellow outline rounded';
    case 'High':
      return 'p-badge badge-orange outline rounded';
    case 'Urgent':
      return 'p-badge badge-red outline rounded';
    default:
      return 'p-badge badge-blue outline rounded';
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

/**
 * BulkRequest Handler
 * @param reportingPeriods
 * @param dataTypes
 * @param companyIdentifiers
 */
export async function sendBulkRequest(
  reportingPeriods: Set<string>,
  dataTypes: Set<BulkDataRequestDataTypesEnum>,
  companyIdentifiers: Set<string>,
  getKeycloakPromise: () => Promise<Keycloak> // <-- hier übergeben
): Promise<void> {
  const payloadBulkDataRequest: BulkDataRequest = {
    reportingPeriods: Array.from(reportingPeriods) as unknown as Set<string>,
    dataTypes: Array.from(dataTypes) as unknown as Set<BulkDataRequestDataTypesEnum>,
    companyIdentifiers: Array.from(companyIdentifiers) as unknown as Set<string>,
    notifyMeImmediately: false,
  };

  const requestDataControllerApi = new ApiClientProvider(getKeycloakPromise()).apiClients.requestController;
  await requestDataControllerApi.postBulkDataRequest(payloadBulkDataRequest);
}

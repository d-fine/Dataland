import type Keycloak from 'keycloak-js';
import {
  type AccessStatus,
  type ExtendedStoredDataRequest,
  RequestStatus,
  type RequestPriority,
} from '@clients/communitymanager';
import { ApiClientProvider } from '@/services/ApiClients';
import { type DataTypeEnum } from '@clients/backend';

/**
 * Returns the List of StoredDataRequest from user with ReqeustStatus 'answered' and matching framework and companyId
 * @param companyId the dataland companyId
 * @param framework the dataland framework
 * @param reportingPeriods list of reporting periods in the view page
 * @param keycloakPromiseGetter the getter-function which returns a Keycloak-Promise
 * @returns a promise, which resolves to an array of StoredDataRequest
 */
export async function getAnsweredDataRequestsForViewPage(
  companyId: string,
  framework: DataTypeEnum,
  reportingPeriods: string[],
  keycloakPromiseGetter?: () => Promise<Keycloak>
): Promise<ExtendedStoredDataRequest[]> {
  try {
    if (keycloakPromiseGetter) {
      return (
        await new ApiClientProvider(
          keycloakPromiseGetter()
        ).apiClients.requestController.getDataRequestsForRequestingUser()
      ).data.filter(
        (dataRequest) =>
          dataRequest.dataType == framework &&
          dataRequest.datalandCompanyId == companyId &&
          reportingPeriods.includes(dataRequest.reportingPeriod) &&
          dataRequest.requestStatus == RequestStatus.Answered
      );
    }
  } catch (error) {
    console.error(error);
  }
  return [];
}

/**
 * Patches the RequestStatus of a StoredDataRequest
 * @param dataRequestId the dataland dataRequestId
 * @param requestStatus the desired requestStatus
 * @param accessStatus the desired access status
 * @param contacts set of email contacts
 * @param message context of the email
 * @param keycloakPromiseGetter the getter-function which returns a Keycloak-Promise
 */
export async function patchDataRequest(
  dataRequestId: string,
  requestStatus: RequestStatus | undefined,
  accessStatus: AccessStatus | undefined,
  contacts: Set<string> | undefined,
  message: string | undefined,
  keycloakPromiseGetter?: () => Promise<Keycloak>
): Promise<void> {
  try {
    if (keycloakPromiseGetter) {
      await new ApiClientProvider(keycloakPromiseGetter()).apiClients.requestController.patchDataRequest(
        dataRequestId,
        requestStatus,
        accessStatus,
        contacts,
        message
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
      return 'p-badge badge-lightgray outline rounded';
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
      return 'p-badge badge-light-green outline rounded';
    case 'Normal':
      return 'p-badge badge-blue outline rounded';
    case 'High':
      return 'p-badge badge-yellow outline rounded';
    case 'VeryHigh':
      return 'p-badge badge-orange outline rounded';
    case 'Urgent':
      return 'p-badge badge-red outline rounded';
    default:
      return 'p-badge outline rounded';
  }
}

/**
 * Gives back a different string for status nonSourceable, otherwise a string that similar to requestStatus
 * @param requestStatus request status of a request
 */
export function getRequestStatusLabel(requestStatus: RequestStatus): string {
  return requestStatus === 'NonSourceable' ? 'No sources available' : requestStatus;
}

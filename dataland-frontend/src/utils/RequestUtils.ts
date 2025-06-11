import { type CompanyIdAndNameAndSector } from '@/types/CompanyTypes.ts';
import { type AxiosPromise } from 'axios';
import type Keycloak from 'keycloak-js';
import {
  type AccessStatus,
  RequestStatus,
  type RequestPriority,
  type BulkDataRequestDataTypesEnum,
  type BulkDataRequestResponse,
} from '@clients/communitymanager';
import { ApiClientProvider } from '@/services/ApiClients';
import {
  EU_TAXONOMY_FRAMEWORKS,
  EU_TAXONOMY_FRAMEWORKS_FINANCIALS,
  EU_TAXONOMY_FRAMEWORKS_NON_FINANCIALS,
  LATEST_PERIOD,
} from '@/utils/Constants.ts';

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
 * Sends BulkDataRequest and handles sector-framework assignments
 * @param startingMonitoringPeriod
 * @param monitoredFrameworks
 * @param companies
 * @param keycloakPromiseGetter
 */
export function sendBulkRequestForPortfolio(
  startingMonitoringPeriod: string,
  monitoredFrameworks: string[],
  companies: CompanyIdAndNameAndSector[],
  keycloakPromiseGetter: () => Promise<Keycloak>
): AxiosPromise<BulkDataRequestResponse>[] {
  const requestController = new ApiClientProvider(keycloakPromiseGetter()).apiClients.requestController;

  const reportingPeriods = Array.from({ length: LATEST_PERIOD - Number(startingMonitoringPeriod) + 1 }, (_, i) =>
    (Number(startingMonitoringPeriod) + i).toString()
  );

  const companyIdsForFinancial = companies
    .filter((company) => company.sector?.toLowerCase() === 'financials')
    .map((company) => company.companyId);
  const companyIdsForNonFinancial = companies
    .filter((company) => company.sector && company.sector.toLowerCase() !== 'financials')
    .map((company) => company.companyId);
  const companyIdsWithNoSector = companies
    .filter(
      (company) =>
        !companyIdsForFinancial.includes(company.companyId) && !companyIdsForNonFinancial.includes(company.companyId)
    )
    .map((company) => company.companyId);

  const requests = [];
  if (monitoredFrameworks.includes('sfdr')) {
    requests.push(
      requestController.postBulkDataRequest({
        reportingPeriods: reportingPeriods as unknown as Set<string>,
        dataTypes: new Set(['sfdr']) as unknown as Set<BulkDataRequestDataTypesEnum>,
        companyIdentifiers: companies.map((company) => company.companyId) as unknown as Set<string>,
        notifyMeImmediately: false,
      })
    );
  }

  if (monitoredFrameworks.includes('eutaxonomy')) {
    if (companyIdsForFinancial.length > 0) {
      requests.push(
        requestController.postBulkDataRequest({
          reportingPeriods: reportingPeriods as unknown as Set<string>,
          dataTypes: EU_TAXONOMY_FRAMEWORKS_FINANCIALS as unknown as Set<BulkDataRequestDataTypesEnum>,
          companyIdentifiers: companyIdsForFinancial as unknown as Set<string>,
          notifyMeImmediately: false,
        })
      );
    }

    if (companyIdsForNonFinancial.length > 0) {
      requests.push(
        requestController.postBulkDataRequest({
          reportingPeriods: reportingPeriods as unknown as Set<string>,
          dataTypes: EU_TAXONOMY_FRAMEWORKS_NON_FINANCIALS as unknown as Set<BulkDataRequestDataTypesEnum>,
          companyIdentifiers: companyIdsForNonFinancial as unknown as Set<string>,
          notifyMeImmediately: false,
        })
      );
    }

    if (companyIdsWithNoSector.length > 0) {
      requests.push(
        requestController.postBulkDataRequest({
          reportingPeriods: reportingPeriods as unknown as Set<string>,
          dataTypes: EU_TAXONOMY_FRAMEWORKS as unknown as Set<BulkDataRequestDataTypesEnum>,
          companyIdentifiers: companyIdsWithNoSector as unknown as Set<string>,
          notifyMeImmediately: false,
        })
      );
    }
  }
  return requests;
}

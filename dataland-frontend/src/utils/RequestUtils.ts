import type Keycloak from 'keycloak-js';
import {
  type AccessStatus,
  RequestStatus,
  type RequestPriority,
  type BulkDataRequestDataTypesEnum,
  type BulkDataRequest,
} from '@clients/communitymanager';
import { ApiClientProvider } from '@/services/ApiClients';
import {
  EU_TAXONOMY_FRAMEWORKS,
  EU_TAXONOMY_FRAMEWORKS_FINANCIALS,
  EU_TAXONOMY_FRAMEWORKS_NON_FINANCIALS,
  LATEST_PERIOD,
} from '@/utils/Constants.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { type EnrichedPortfolio } from '@clients/userservice';

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
 * Helper function for fetching reporting period and company ids
 * @param portfolio enriched object of processed portfolio
 */
function extractPeriodandCompanyIds(portfolio: EnrichedPortfolio): {
  reportingPeriodsSet: Set<string>;
  monitoredFrameworks: Set<string>;
  allIds: Set<string>;
  financialIds: Set<string>;
  nonFinancialIds: Set<string>;
  noSectorIds: Set<string>;
} {
  const reportingPeriodsSet = new Set<string>(
    Array.from({ length: LATEST_PERIOD - Number(portfolio.startingMonitoringPeriod) + 1 }, (_, i) =>
      (Number(portfolio.startingMonitoringPeriod) + i).toString()
    )
  );

  const monitoredFrameworks = new Set(portfolio.monitoredFrameworks);

  const allIds = new Set(portfolio.entries.map((c) => c.companyId));
  const financialIds = new Set(
    portfolio.entries.filter((c) => c.sector?.toLowerCase() === 'financials').map((c) => c.companyId)
  );
  const nonFinancialIds = new Set(
    portfolio.entries.filter((c) => c.sector?.toLowerCase() !== 'financials').map((c) => c.companyId)
  );
  const noSectorIds = new Set(portfolio.entries.filter((c) => !c.sector).map((c) => c.companyId));

  return {
    reportingPeriodsSet,
    monitoredFrameworks,
    allIds,
    financialIds,
    nonFinancialIds,
    noSectorIds,
  };
}

/**
 * Constructs and sends bulk data requests based on portfolio configuration.
 *
 * @param portfolio - Enriched portfolio object containing entries and monitoring info.
 * @param getKeycloakPromise - Function returning a promise that resolves with a Keycloak instance.
 * @returns Array of promises representing the API requests.
 */
export function sendBulkRequestForPortfolio(
  portfolio: EnrichedPortfolio,
  getKeycloakPromise: () => Promise<Keycloak>
): Promise<unknown>[] {
  const requestDataControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)());
  const requests: Promise<unknown>[] = [];

  const { reportingPeriodsSet, monitoredFrameworks, allIds, financialIds, nonFinancialIds, noSectorIds } =
    extractPeriodandCompanyIds(portfolio);

  const makeRequest = async (
    companyIdentifiers: Set<string>,
    dataTypes: Set<BulkDataRequestDataTypesEnum>
  ): Promise<unknown> => {
    const payload: BulkDataRequest = {
      reportingPeriods: Array.from(reportingPeriodsSet) as unknown as Set<string>,
      dataTypes: Array.from(dataTypes) as unknown as Set<BulkDataRequestDataTypesEnum>,
      companyIdentifiers: Array.from(companyIdentifiers) as unknown as Set<string>,
      notifyMeImmediately: false,
    };
    console.log("Sending bulk request with payload:", payload);

    try {
      const response = await requestDataControllerApi.apiClients.requestController.postBulkDataRequest(payload);
      console.log("Bulk request successful, response:", response);
      return response;
    } catch (err) {
      console.error("Bulk request failed:", err);
      throw err;
    }
  };

  if (monitoredFrameworks.has('eutaxonomy')) {
    if (financialIds.size > 0) {
      requests.push(
        makeRequest(financialIds, new Set(EU_TAXONOMY_FRAMEWORKS_FINANCIALS) as Set<BulkDataRequestDataTypesEnum>)
      );
    }

    if (nonFinancialIds.size > 0) {
      requests.push(
        makeRequest(
          nonFinancialIds,
          new Set(EU_TAXONOMY_FRAMEWORKS_NON_FINANCIALS) as Set<BulkDataRequestDataTypesEnum>
        )
      );
    }

    if (noSectorIds.size > 0) {
      requests.push(makeRequest(noSectorIds, new Set(EU_TAXONOMY_FRAMEWORKS) as Set<BulkDataRequestDataTypesEnum>));
    }
  }

  if (monitoredFrameworks.has('sfdr')) {
    requests.push(makeRequest(allIds, new Set(['sfdr']) as Set<BulkDataRequestDataTypesEnum>));
  }
  console.log(requests);
  return requests;
}

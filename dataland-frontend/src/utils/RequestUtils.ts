import type Keycloak from "keycloak-js";
import { RequestStatus, type StoredDataRequest } from "@clients/communitymanager";
import { ApiClientProvider } from "@/services/ApiClients";
import { type DataTypeEnum } from "@clients/backend";

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
  keycloakPromiseGetter?: () => Promise<Keycloak>,
): Promise<StoredDataRequest[]> {
  try {
    if (keycloakPromiseGetter) {
      return (
        await new ApiClientProvider(keycloakPromiseGetter()).apiClients.requestController.getDataRequestsForUser()
      ).data.filter(
        (dataRequest) =>
          dataRequest.dataType == framework &&
          dataRequest.dataRequestCompanyIdentifierValue == companyId &&
          reportingPeriods.includes(dataRequest.reportingPeriod) &&
          dataRequest.requestStatus == RequestStatus.Answered,
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
 * @param keycloakPromiseGetter the getter-function which returns a Keycloak-Promise
 */
export async function patchDataRequestStatus(
  dataRequestId: string,
  requestStatus: RequestStatus,
  keycloakPromiseGetter?: () => Promise<Keycloak>,
): Promise<void> {
  try {
    if (keycloakPromiseGetter) {
      await new ApiClientProvider(keycloakPromiseGetter()).apiClients.requestController.patchDataRequestStatus(
        dataRequestId,
        requestStatus,
      );
    }
  } catch (error) {
    console.error(error);
    throw error;
  }
}

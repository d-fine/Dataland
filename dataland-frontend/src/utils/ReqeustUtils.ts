import type Keycloak from "keycloak-js";
import { RequestStatus, type StoredDataRequest } from "@clients/communitymanager";
import { ApiClientProvider } from "@/services/ApiClients";
import { type DataTypeEnum } from "@clients/backend";

/**
 * Returns the List of StoredDataRequest from user with matching framework and companyId
 * @param companyId the dataland companyId
 * @param framework the dataland framework
 * @param keycloakPromiseGetter the getter-function which returns a Keycloak-Promise
 * @returns a promise, which resolves to an array of StoredDataRequest
 */
export async function getAnsweredDataRequestsForViewPage(
  companyId: string,
  framework: DataTypeEnum,
  keycloakPromiseGetter?: () => Promise<Keycloak>,
): Promise<StoredDataRequest[]> {
  let listOfStoredDataRequest: StoredDataRequest[] = [];
  try {
    if (keycloakPromiseGetter) {
      listOfStoredDataRequest = (
        await new ApiClientProvider(keycloakPromiseGetter()).apiClients.requestController.getDataRequestsForUser()
      ).data.filter(
        (dataRequest) =>
          dataRequest.dataType == framework &&
          dataRequest.dataRequestCompanyIdentifierValue == companyId &&
          dataRequest.requestStatus == RequestStatus.Answered,
      );
    }
  } catch (error) {
    console.error(error);
    throw error;
  }
  return listOfStoredDataRequest;
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
      await new ApiClientProvider(keycloakPromiseGetter()).apiClients.requestController.patchDataRequest(
        dataRequestId,
        requestStatus,
      );
    }
  } catch (error) {
    console.error(error);
    throw error;
  }
}

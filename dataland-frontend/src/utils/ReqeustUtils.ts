import type Keycloak from "keycloak-js";
import { RequestStatus, type StoredDataRequest } from "@clients/communitymanager";

import { ApiClientProvider } from "@/services/ApiClients";
import type { AxiosError } from "axios";
import { type DataTypeEnum } from "@clients/backend";

/**
 * Returns the List of StoredDataRequest from user with matching framework and companyId
 * @param companyId the dataland companyId
 * @param framework the dataland framework
 * @param keycloakPromiseGetter the getter-function which returns a Keycloak-Promise
 * @returns a promise, which resolves to an array of StoredDataRequest
 */
export async function getOpenDataRequestsForViewPage(
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
          dataRequest.requestStatus == RequestStatus.Open,
      );
    }
  } catch (error) {
    if ((error as AxiosError)?.response?.status == 404) {
      return listOfStoredDataRequest;
    }
    throw error;
  }
  return listOfStoredDataRequest;
}

/**
 * Patches the RequestStatus of a StoredDataRequest
 * @param dataRequestId the dataland dataRequestId
 * @param requestStatus the desired requestStatus
 * @param keycloakPromiseGetter the getter-function which returns a Keycloak-Promise
 * @returns a promise, which resolves to a boolean
 */
export async function patchDataRequestStatus(
  dataRequestId: string,
  requestStatus: RequestStatus,
  keycloakPromiseGetter?: () => Promise<Keycloak>,
): Promise<boolean> {
  try {
    if (keycloakPromiseGetter) {
      if (
        (
          await new ApiClientProvider(keycloakPromiseGetter()).apiClients.requestController.patchDataRequest(
            dataRequestId,
            requestStatus,
          )
        ).status == 200
      ) {
        return true;
      }
    }
  } catch (error) {
    if ((error as AxiosError)?.response?.status == 404) {
      return false;
    }
    throw error;
  }
  return false;
}

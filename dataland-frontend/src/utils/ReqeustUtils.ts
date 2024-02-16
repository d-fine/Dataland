import type Keycloak from "keycloak-js";
import { type StoredDataRequest } from "@clients/communitymanager";

import { ApiClientProvider } from "@/services/ApiClients";
import type { AxiosError } from "axios";

/**
 * Returns the List of StoredDataRequest from user with matching framework and companyId
 * @param companyId the dataland companyId
 * @param framework the dataland framework
 * @param keycloakPromiseGetter the getter-function which returns a Keycloak-Promise
 * @returns a promise, which resolves to an array of StoredDataRequest
 */
export async function getDataRequestsForViewPage(
  companyId: string,
  framework: string,
  keycloakPromiseGetter?: () => Promise<Keycloak>,
): Promise<StoredDataRequest[]> {
  let listOfStoredDataRequest: StoredDataRequest[] = [];
  try {
    if (keycloakPromiseGetter) {
      listOfStoredDataRequest = (
        await new ApiClientProvider(keycloakPromiseGetter()).apiClients.requestController.getDataRequestsForUser()
      ).data.filter(
        (dataRequest) =>
          dataRequest.dataRequestCompanyIdentifierValue == framework &&
          dataRequest.dataRequestCompanyIdentifierValue == companyId,
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

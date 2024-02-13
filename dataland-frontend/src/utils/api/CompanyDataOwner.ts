import type Keycloak from "keycloak-js";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { checkIfUserHasRole, KEYCLOAK_ROLE_ADMIN } from "@/utils/KeycloakUtils";

/**
 * Get the Information about Data-ownership
 * @param getKeycloakPromise getter for a keycloak promise
 * @param companyId identifier of the company
 * @returns a promise which resolves to a boolean if the company has at least one data owner
 */
export async function getCompanyDataOwnerInformation(
  getKeycloakPromise: () => Promise<Keycloak>,
  companyId: string,
): Promise<boolean> {
  const companyDataControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)()).backendClients
    .companyDataController;
  let atLeastOneDataOwner: boolean | undefined;
  try {
    if (await checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, getKeycloakPromise)) {
      atLeastOneDataOwner = ((await companyDataControllerApi.getDataOwners(companyId)).data.length > 0) as
        | boolean
        | undefined;
    } else {
      atLeastOneDataOwner = ((await companyDataControllerApi.getDataOwners(companyId)).status == 200) as
        | boolean
        | undefined;
    }

    if (atLeastOneDataOwner !== undefined) {
      return true;
    } else {
      atLeastOneDataOwner = false;
    }
  } catch (error) {
    console.error(error);
    atLeastOneDataOwner = false;
  }
  return atLeastOneDataOwner;
}

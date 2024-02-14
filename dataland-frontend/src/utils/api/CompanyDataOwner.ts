import type Keycloak from "keycloak-js";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { isCompanyIdValid } from "@/utils/ValidationsUtils";

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
  if (!isCompanyIdValid(companyId)) {
    return false;
  }
  const companyDataControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)()).backendClients
    .companyDataController;
  let atLeastOneDataOwner: boolean | undefined;
  try {
    atLeastOneDataOwner = ((await companyDataControllerApi.hasCompanyDataOwner(companyId)).status == 200) as
      | boolean
      | undefined;
    if (atLeastOneDataOwner == undefined) {
      atLeastOneDataOwner = false;
    }
  } catch (error) {
    console.error(error);
    atLeastOneDataOwner = false;
  }
  return atLeastOneDataOwner;
}

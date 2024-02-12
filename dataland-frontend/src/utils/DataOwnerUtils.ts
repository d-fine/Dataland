import { assertDefined } from "@/utils/TypeScriptUtils";
import type Keycloak from "keycloak-js";
import { ApiClientProvider } from "@/services/ApiClients";
import { type AxiosError } from "axios";
import { waitForAndReturnResolvedKeycloakPromise } from "@/utils/KeycloakUtils";

/**
 * Check if a user is data owner of a company
 * @param companyId the dataland companyId of the company for which ownership should be checked
 * @param keycloakPromiseGetter the getter-function which returns a Keycloak-Promise
 * @returns a promise, which resolves to a boolean
 */
export async function isUserDataOwnerForCompany(
  companyId: string,
  keycloakPromiseGetter?: () => Promise<Keycloak>,
): Promise<boolean> {
  const uuidRegexExp = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
  const isCompanyIdValid = uuidRegexExp.test(companyId);
  if (keycloakPromiseGetter && isCompanyIdValid) {
    const resolvedKeycloakPromise = await waitForAndReturnResolvedKeycloakPromise(keycloakPromiseGetter);
    const userId = resolvedKeycloakPromise?.idTokenParsed?.sub;
    if (userId) {
      try {
        await new ApiClientProvider(
          keycloakPromiseGetter(),
        ).backendClients.companyDataController.isUserDataOwnerForCompany(companyId, assertDefined(userId));
        return true;
      } catch (error) {
        if ((error as AxiosError)?.response?.status == 404) {
          return false;
        }
        throw error;
      }
    } else return false;
  } else return false;
}

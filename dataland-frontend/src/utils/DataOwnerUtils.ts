import type Keycloak from "keycloak-js";
import { ApiClientProvider } from "@/services/ApiClients";
import { type AxiosError } from "axios";
import { waitForAndReturnResolvedKeycloakPromise } from "@/utils/KeycloakUtils";
import {isCompanyIdValid} from "@/utils/ValidationsUtils";

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
  if (keycloakPromiseGetter && isCompanyIdValid(companyId)) {
    const resolvedKeycloakPromise = await waitForAndReturnResolvedKeycloakPromise(keycloakPromiseGetter);
    const userId = resolvedKeycloakPromise?.idTokenParsed?.sub;
    if (userId) {
      try {
        await new ApiClientProvider(
          keycloakPromiseGetter(),
        ).backendClients.companyDataController.isUserDataOwnerForCompany(companyId, userId);
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
/**
 * Get the Information about Data-ownership
 * @param keyCloakPromiseGetter getter for a keycloak promise
 * @param companyId identifier of the company
 * @returns a promise which resolves to a boolean if the company has at least one data owner
 */
export async function hasCompanyAtLeastOneDataOwner(
    companyId: string,
    keyCloakPromiseGetter?: () => Promise<Keycloak>,
): Promise<boolean> {
  if (keyCloakPromiseGetter && isCompanyIdValid(companyId)) {
    const resolvedKeycloakPromise = await waitForAndReturnResolvedKeycloakPromise(keyCloakPromiseGetter);
    const userId = resolvedKeycloakPromise?.idTokenParsed?.sub;
    if (userId) {
      try {
        await new ApiClientProvider(keyCloakPromiseGetter()).backendClients
            .companyDataController.hasCompanyDataOwner(companyId);
        return true

      } catch (error) {
        if ((error as AxiosError)?.response?.status == 404) {
          return false
        }
        throw error;

      }
    } else return false;
  }else return false;}
import type Keycloak from 'keycloak-js';
import { ApiClientProvider } from '@/services/ApiClients';
import { type AxiosError } from 'axios';
import { waitForAndReturnResolvedKeycloakPromise } from '@/utils/KeycloakUtils';
import { isCompanyIdValid } from '@/utils/ValidationsUtils';
import { type CompanyRole } from '@clients/communitymanager';

/**
 * Check if current user has a certain company role for a company
 * @param companyRole to check for
 * @param companyId of the company for which the company role assignment should be checked
 * @param keycloakPromiseGetter the getter-function which returns a Keycloak-Promise
 * @returns a promise, which resolves to a boolean
 */
export async function hasUserCompanyRoleForCompany(
  companyRole: CompanyRole,
  companyId: string,
  keycloakPromiseGetter?: () => Promise<Keycloak>
): Promise<boolean> {
  if (keycloakPromiseGetter && isCompanyIdValid(companyId)) {
    const resolvedKeycloakPromise = await waitForAndReturnResolvedKeycloakPromise(keycloakPromiseGetter);
    const userId = resolvedKeycloakPromise?.idTokenParsed?.sub;
    if (userId) {
      try {
        await new ApiClientProvider(keycloakPromiseGetter()).apiClients.companyRolesController.hasUserCompanyRole(
          companyRole,
          companyId,
          userId
        );
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
 * Get the Information about company ownership
 * @param companyId identifier of the company
 * @param keyCloakPromiseGetter getter for a keycloak promise
 * @returns a promise which resolves to a boolean if the company has at least one company owner
 */
export async function hasCompanyAtLeastOneCompanyOwner(
  companyId: string,
  keyCloakPromiseGetter?: () => Promise<Keycloak>
): Promise<boolean> {
  if (keyCloakPromiseGetter && isCompanyIdValid(companyId)) {
    try {
      await new ApiClientProvider(keyCloakPromiseGetter()).apiClients.companyRolesController.hasCompanyAtLeastOneOwner(
        companyId
      );
      return true;
    } catch (error) {
      if ((error as AxiosError)?.response?.status == 404) {
        return false;
      }
      throw error;
    }
  } else return false;
}

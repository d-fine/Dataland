import type Keycloak from 'keycloak-js';
import { ApiClientProvider } from '@/services/ApiClients';
import { type AxiosError } from 'axios';
import { waitForAndReturnResolvedKeycloakPromise } from '@/utils/KeycloakUtils';
import { isCompanyIdValid } from '@/utils/ValidationsUtils';
import { CompanyRole, type CompanyRoleAssignment } from '@clients/communitymanager';

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
 * Check if current user is either the company Owner or a company data uploader
 * @param companyId of the company
 * @param keycloakPromiseGetter the getter-function which returns a Keycloak-Promise
 * @returns a promise, which resolves to a boolean
 */
export async function hasUserCompanyOwnerOrDataUploaderRole(
  companyId: string,
  keycloakPromiseGetter?: () => Promise<Keycloak>
): Promise<boolean> {
  const hasCompanyOwnerRole = await hasUserCompanyRoleForCompany(
    CompanyRole.CompanyOwner,
    companyId,
    keycloakPromiseGetter
  );

  const hasCompanyDataUploaderRole = await hasUserCompanyRoleForCompany(
    CompanyRole.DataUploader,
    companyId,
    keycloakPromiseGetter
  );

  return hasCompanyOwnerRole || hasCompanyDataUploaderRole;
}

/**
 * Get the Information about company ownership
 * @param companyId identifier of the company
 * @param keycloakPromiseGetter getter for a keycloak promise
 * @returns a promise which resolves to a boolean if the company has at least one company owner
 */
export async function hasCompanyAtLeastOneCompanyOwner(
  companyId: string,
  keycloakPromiseGetter?: () => Promise<Keycloak>
): Promise<boolean> {
  if (keycloakPromiseGetter && isCompanyIdValid(companyId)) {
    try {
      await new ApiClientProvider(keycloakPromiseGetter()).apiClients.companyRolesController.hasCompanyAtLeastOneOwner(
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

/**
 * Get company role assignments for the currently logged in user
 * @param companyId defines the company for which to check
 * @param keycloakPromiseGetter getter for a keycloak promise
 * @returns a promise which resolves to an array of company role assignments for this user and company
 */
export async function getCompanyRoleAssignmentsForCurrentUserAndCompany(
  companyId: string,
  keycloakPromiseGetter?: () => Promise<Keycloak>
): Promise<Array<CompanyRoleAssignment>> {
  if (keycloakPromiseGetter) {
    const resolvedKeycloakPromise = await waitForAndReturnResolvedKeycloakPromise(keycloakPromiseGetter);
    const userId = resolvedKeycloakPromise?.idTokenParsed?.sub;
    if (userId) {
      try {
        const response = await new ApiClientProvider(
          keycloakPromiseGetter()
        ).apiClients.companyRolesController.getCompanyRoleAssignments(undefined, companyId, userId);
        return response.data;
      } catch (error) {
        if ((error as AxiosError)?.response?.status == 403) {
          return [];
        }
        throw error;
      }
    } else {
      return [];
    }
  } else return [];
}

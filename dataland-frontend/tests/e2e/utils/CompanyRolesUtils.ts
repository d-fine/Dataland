import {
  Configuration,
  CompanyRolesControllerApi,
  type CompanyRoleAssignment,
  CompanyRole,
} from '@clients/communitymanager';
import { admin_userId } from '@e2e/utils/Cypress';
import { hasUserCompanyRoleForCompany } from '@/utils/CompanyRolesUtils';
import type Keycloak from 'keycloak-js/dist/keycloak';

/**
 * Method that assigns a company role for a specified company to a user
 * @param token authentication token of the user doing the post request
 * @param companyRole to assign
 * @param companyId of the company for which the role shall be assigned to the user
 * @param userId of the user
 * @returns the api response of the assignCompanyRole endpoint
 */
export async function assignCompanyRole(
  token: string,
  companyRole: CompanyRole,
  companyId: string,
  userId: string
): Promise<CompanyRoleAssignment> {
  const apiResponse = await new CompanyRolesControllerApi(new Configuration({ accessToken: token })).assignCompanyRole(
    companyRole,
    companyId,
    userId
  );
  return apiResponse.data;
}

/**
 * Assigns company ownership to the Dataland admin
 * @param token authentication token of the user doing the post request
 * @param companyId of the company for which the role shall be assigned to the user
 * @returns the api response of the assignCompanyRole endpoint
 */
export async function assignCompanyOwnershipToDatalandAdmin(
  token: string,
  companyId: string
): Promise<CompanyRoleAssignment> {
  const apiResponse = await assignCompanyRole(token, CompanyRole.CompanyOwner, companyId, admin_userId);
  return apiResponse;
}

/**
 * Bypasses the QA step if the user is the company owner or data uploader
 * @param companyId of the company for which the role shall be assigned to the user
 * @param keycloakPromise the Keycloak promise
 * @returns whether bypassQA should be bypassed
 */
export async function canUserBypassQA(companyId: string, keycloakPromise: () => Promise<Keycloak>): Promise<Boolean> {
  return (
    hasUserCompanyRoleForCompany(CompanyRole.CompanyOwner, companyId, keycloakPromise) ||
    hasUserCompanyRoleForCompany(CompanyRole.DataUploader, companyId, keycloakPromise)
  );
}

/**
 * Checks that the QA status of the uploaded dataset is automatically set to APPROVED
 */
export function isDatasetApproved(): void {
  cy.get('[data-test="qa-status"]').first().should('have.text', 'APPROVED');
}

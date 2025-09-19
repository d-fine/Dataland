import {
  Configuration,
  CompanyRolesControllerApi,
  type CompanyRoleAssignment,
  CompanyRole,
} from '@clients/communitymanager';
import { admin_userId } from '@e2e/utils/Cypress';
import { type AxiosError, type HttpStatusCode, isAxiosError } from 'axios';

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
 * Method that removes all company roles for a specified company to a user
 * @param token authentication token of the user doing the post request
 * @param companyId of the company for which the role shall be assigned to the user
 * @param userId of the user
 */
export async function removeAllCompanyRoles(token: string, companyId: string, userId: string): Promise<void> {
  const api = new CompanyRolesControllerApi(new Configuration({ accessToken: token }));
  const roles = Object.values(CompanyRole) as CompanyRole[];

  for (const role of roles) {
    try {
      await api.removeCompanyRole(role, companyId, userId);
    } catch (error: unknown) {
      if (isHttpStatus(error, 404)) continue;

      throw error;
    }
  }
}

/**
 * Determines if a given error is an AxiosError with a specific HTTP status code.
 * @param err The error object to check.
 * @param code The HTTP status code to compare against.
 * @returns True if the error is an AxiosError with the provided status code, false otherwise.
 */
function isHttpStatus(err: unknown, code: HttpStatusCode): err is AxiosError {
  return isAxiosError(err) && err.response?.status === code;
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
 * Checks that the QA status of the uploaded dataset is automatically set to Accepted
 */
export function isDatasetAccepted(): void {
  cy.get('[data-test="qa-status"]', { timeout: Cypress.env('medium_timeout_in_ms') as number })
    .first()
    .should('have.text', 'Accepted');
}

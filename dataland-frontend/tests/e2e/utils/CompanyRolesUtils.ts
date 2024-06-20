import {
  Configuration,
  CompanyRolesControllerApi,
  type CompanyRoleAssignment,
  type CompanyRole,
} from "@clients/communitymanager";

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
  userId: string,
): Promise<CompanyRoleAssignment> {
  const apiResponse = await new CompanyRolesControllerApi(new Configuration({ accessToken: token })).assignCompanyRole(
    companyRole,
    companyId,
    userId,
  );
  return apiResponse.data;
}

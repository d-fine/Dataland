import {
  type CompanyRightAssignmentString,
  type CompanyRightAssignmentStringCompanyRightEnum,
  CompanyRightsControllerApi,
  Configuration,
} from '@clients/communitymanager';

/**
 * Method that assigns a company right for a specified company
 * @param token authentication token of the user making the post request
 * @param companyRight to assign
 * @param companyId of the company for which the right shall be assigned
 * @returns the api response of the assignCompanyRight endpoint
 */
export async function assignCompanyRight(
  token: string,
  companyRight: CompanyRightAssignmentStringCompanyRightEnum,
  companyId: string
): Promise<CompanyRightAssignmentString> {
  const apiResponse = await new CompanyRightsControllerApi(new Configuration({ accessToken: token })).postCompanyRight({
    companyId: companyId,
    companyRight: companyRight,
  });
  return apiResponse.data;
}

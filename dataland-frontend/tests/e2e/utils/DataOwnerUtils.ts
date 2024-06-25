import { CompanyDataControllerApi, Configuration, type CompanyDataOwners } from '@clients/backend';

/**
 * Method that sets a user as a data owner of the specified company
 * @param token authentication token of the user doing the post request
 * @param userId of the user that should be set as a data owner
 * @param companyId of the company for which the user should be set as a data owner
 * @returns the api response of the postDataOwner endpoint
 */
export async function postDataOwner(token: string, userId: string, companyId: string): Promise<CompanyDataOwners> {
  const apiResponse = await new CompanyDataControllerApi(new Configuration({ accessToken: token })).postDataOwner(
    companyId,
    userId
  );
  return apiResponse.data;
}

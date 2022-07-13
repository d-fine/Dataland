/**
 * Module description todo
 */

import { StoredCompany } from "@/../build/clients/backend/api";

/**
 * description todo
 *
 * @param  {array} inputArray      description todo
 */
function returnPermIdOrEmptyString(inputArray: Array<string>): string {
  if (inputArray[0]) {
    return inputArray[0];
  } else return "";
}

/**
 * description todo
 *
 * @param  {-} response      description todo
 */
export function searchTaxonomyPageResponseMapper(responseData: Array<StoredCompany>): Array<object> {
  return responseData.map((company) => ({
    companyName: company.companyInformation.companyName,
    companyInformation: company.companyInformation,
    companyId: company.companyId,
    permId: returnPermIdOrEmptyString(
      company.companyInformation.identifiers
        .filter((identifier) => identifier.identifierType === "PermId")
        .map((e) => {
          return e.identifierType === "PermId" ? e.identifierValue : "";
        })
    ),
  }));
}

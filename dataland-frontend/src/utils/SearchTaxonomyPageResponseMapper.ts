/**
 * Module to map the received stored companies of an API-call to a specific schema which is needed on the taxonomy
 * search page
 */

import { StoredCompany } from "@/../build/clients/backend/api";

/**
 * retrieve the value of the Perm Id of a company
 *
 * @param  {StoredCompany} storedCompany      is the company object for which the Perm Id should be retrieved
 */
function retrievePermIdFromStoredCompany(storedCompany: StoredCompany): string {
  const permIdIdentifier = storedCompany.companyInformation.identifiers.filter(
    (identifier) => identifier.identifierType === "PermId"
  );
  if (permIdIdentifier.length == 1) {
    return permIdIdentifier[0].identifierValue;
  } else if (permIdIdentifier.length == 0) {
    return "";
  } else {
    console.error("More than one PermId found for a specific company");
    return permIdIdentifier[0].identifierValue;
  }
}

/**
 * map the received stored companies of an API-call
 *
 * @param  {Array<StoredCompany>} responseData      the received data with the companiy objects
 */
export function searchTaxonomyPageResponseMapper(responseData: Array<StoredCompany>): Array<object> {
  return responseData.map((company) => ({
    companyName: company.companyInformation.companyName,
    companyInformation: company.companyInformation,
    companyId: company.companyId,
    permId: retrievePermIdFromStoredCompany(company),
  }));
}

/**
 * Module description todo
 */

import {StoredCompany} from "@/../build/clients/backend/api";

/**
 * description todo
 *
 * @param  {array} inputArray      description todo
 */
function retrievePermIdFromStoredCompany(storedCompany: StoredCompany): string {
    const permIdIdentifier = storedCompany.companyInformation.identifiers
        .filter((identifier) => identifier.identifierType === "PermId")
    if (permIdIdentifier.length == 1) {
        return permIdIdentifier[0].identifierValue
    }
    else if (permIdIdentifier.length == 0) {
        return ""
    }
    else {console.error("More than one PermId found for a specific company")}
}

/**
 * description todo
 *
 * @param  {-} response      description todo
 */
export function searchTaxonomyPageResponseMapper(responseData: Array<StoredCompany>): Array<object> {
    return responseData.map((company) => (
            {
                companyName: company.companyInformation.companyName,
                companyInformation: company.companyInformation,
                companyId: company.companyId,
                permId: retrievePermIdFromStoredCompany(company)
            }
        )
    )
}



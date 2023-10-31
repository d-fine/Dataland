package org.dataland.datalandcommunitymanager.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Class for handling the upload of the company information retrieved from GLEIF to the Dataland backend
 */
@Service("CompanyGetter")
class CompanyGetter(
    @Value("\${dataland.backend.base-url}") private val backendBaseUrl: String,
) {
    // TODO Memo an mich: Das handling der internal base url vom Backend ist inkonsistent und seltsam in der code base

    /** This method checks if a Dataland companyId is associated with a specific company identifier value.
     * @param identifierValue to check for
     * @returns a company Id if it could find one, otherwise null
     */

    // TODO: The company endpoint that is required for this does not exist currently!
    //  We need to introduce it in the backend.
    //  Other questions: Shall we also pass the expected type here?
    fun getCompanyIdByIdentifier(identifierValue: String): String? {
        return "TODO"
    }
}

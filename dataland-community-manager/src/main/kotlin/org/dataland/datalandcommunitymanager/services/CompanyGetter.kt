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

    fun getCompanyIdByIdentifier(searchString: String): String {
        // TODO: This endpoint does not exist currently! We need to introduce it in the backend.
        return "TODO" // TODO
    }
}

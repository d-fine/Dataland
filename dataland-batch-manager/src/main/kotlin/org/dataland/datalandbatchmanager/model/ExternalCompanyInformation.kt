package org.dataland.datalandbatchmanager.model

import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyInformationPatch

/**
 * Interface defining how the CompanyUploader accesses the transformation to CompanyInformation
 * or CompanyInformationPatch for the externally parsed data types
 */
interface ExternalCompanyInformation {
    /**
     * Method to transform the external data to CopmanyInformation.
     * @return the Dataland CompanyInformation object with the information of the corresponding external data
     */
    fun toCompanyPost(): CompanyInformation

    /**
     * Method to transform the external data structure to a CopmanyInformationPatch.
     * It accepts a list of identifiers already present on dataland to decide how to update the company information.
     * @param conflictingIdentifiers the set of conflicting identifiers
     * @return the Dataland CompanyInformationPatch object or null if no update is applicable
     */
    fun toCompanyPatch(conflictingIdentifiers: Set<String?>? = null): CompanyInformationPatch?

    /**
     * Method returning the name and main identifier of a company associated with the external data structure
     * @return the uniquely identifying string
     */
    fun getNameAndIdentifier(): String
}

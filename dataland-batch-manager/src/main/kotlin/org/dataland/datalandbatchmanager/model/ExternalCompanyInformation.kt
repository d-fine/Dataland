package org.dataland.datalandbatchmanager.model

import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyInformationPatch

/**
 * Interface holding companyInformation for Northdata handling
 */
// TODO make this doc more detailed
public interface ExternalCompanyInformation {
    /**
     * function to transform a combined company information object to CopmanyInformation.
     * @return the Dataland CompanyInformation object with the information of the corresponding combined object
     */
    fun toCompanyPost(): CompanyInformation

    /**
     * function to transform a combined company information object to CopmanyInformation.
     * @return the Dataland CompanyInformation object with the information of the corresponding combined object
     */
    fun toCompanyPatch(): CompanyInformationPatch

    /**
     * Method to get name and identifier of a company
     */
    // TODO make this doc more detailed
    fun getNameAndIdentifier(): String
}

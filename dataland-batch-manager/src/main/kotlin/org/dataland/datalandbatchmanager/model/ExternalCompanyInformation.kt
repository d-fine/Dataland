package org.dataland.datalandbatchmanager.model

import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyInformationPatch

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

    fun getNameAndIdentifier(): String
}

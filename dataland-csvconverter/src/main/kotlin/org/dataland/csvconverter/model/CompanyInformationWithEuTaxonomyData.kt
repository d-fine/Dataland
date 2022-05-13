package org.dataland.csvconverter.model

import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.EuTaxonomyData

/**
 * Class to help to parse json file containing companyInformation and euTaxonomyData
 * @param companyInformation contains information of company
 * @param euTaxonomyData contains eu taxonomy dat for the company
 */
data class CompanyInformationWithEuTaxonomyData(
    val companyInformation: CompanyInformation,
    val euTaxonomyData: EuTaxonomyData
)

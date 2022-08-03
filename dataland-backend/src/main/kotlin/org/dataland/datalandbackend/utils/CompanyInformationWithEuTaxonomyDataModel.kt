package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.EuTaxonomyDataForNonFinancials

/**
 * --- Non-API model ---
 * Class to help to parse json file containing companyInformation and euTaxonomyData
 * @param companyInformation contains information of company
 * @param euTaxonomyData contains eu taxonomy dat for the company
 */
data class CompanyInformationWithEuTaxonomyDataModel(
    val companyInformation: CompanyInformation,
    val euTaxonomyData: EuTaxonomyDataForNonFinancials
)

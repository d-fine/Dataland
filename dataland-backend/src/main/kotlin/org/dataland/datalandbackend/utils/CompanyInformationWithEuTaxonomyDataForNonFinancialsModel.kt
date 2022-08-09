package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.EuTaxonomyDataForNonFinancials

/**
 * --- Non-API model ---
 * Class to help to parse json file containing companyInformation and euTaxonomyData
 * @param companyInformation contains information of company
 * @param t contains eu taxonomy data for non-financials for the company
 */
data class CompanyInformationWithEuTaxonomyDataForNonFinancialsModel(
    val companyInformation: CompanyInformation,
    val t: EuTaxonomyDataForNonFinancials
)

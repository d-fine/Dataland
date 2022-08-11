package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.model.CompanyInformation

/**
 * --- Non-API model ---
 * Class to help to parse json file containing companyInformation and euTaxonomyData
 * @param companyInformation contains information of company
 * @param t contains eu taxonomy data for non-financials for the company
 */
data class CompanyInformationWithData<T>(
    val companyInformation: CompanyInformation,
    val t: T
)

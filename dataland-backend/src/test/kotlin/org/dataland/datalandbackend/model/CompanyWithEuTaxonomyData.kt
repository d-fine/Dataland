package org.dataland.datalandbackend.model

/**
 * --- Non-API model ---
 * Class to help to parse json file containing companyInformation and euTaxonomyData
 * @param companyInformation contains information of company
 * @param euTaxonomyData contains eu taxonomy dat for the company
 */
data class CompanyWithEuTaxonomyData(
    val companyInformation: CompanyInformation,
    val euTaxonomyData: EuTaxonomyData
)

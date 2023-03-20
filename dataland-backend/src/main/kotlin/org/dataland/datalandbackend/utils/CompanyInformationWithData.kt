package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.model.CompanyInformation

/**
 * --- Non-API model ---
 * Class to help to parse json file containing companyInformation and framework data
 * @param companyInformation contains information of company
 * @param t is a generic placeholder for any framework type, for which data can be uploaded
 */
data class CompanyInformationWithData<T>(
    val companyInformation: CompanyInformation,
    val t: T,
    val reportingPeriod: String,
)

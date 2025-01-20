package org.dataland.datalanddataexporter
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation

data class CompanyInformationWithTAndReportingPeriod<T>(
    var companyInformation: CompanyInformation,
    var t: T,
    var reportingPeriod: String,
)

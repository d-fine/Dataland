package org.dataland.datalandbackend

import org.dataland.datalandbackend.model.DataType
import org.springframework.stereotype.Component

@Component("LogMessageBuilder")
class LogMessageBuilder {
    val accessDeniedExceptionMessage = "You are trying to access a unreviewed dataset"

    fun generatedCorrelationIdMessage(correlationId: String, companyId: String): String {
        return "Generated correlation ID '$correlationId' for the received request with company ID: $companyId."
    }

    fun postCompanyAssociatedDataMessage(userId: String, dataType: DataType, companyId: String, reportingPeriod: String)
    : String {
        return "Received a request from user '$userId' to post company associated data of type $dataType " +
            "for company ID '$companyId' and the reporting period $reportingPeriod"
    }

    fun postCompanyAssociatedDataSuccessMessage(companyId: String, correlationId: String): String {
        return "Posted company associated data for companyId '$companyId'. Correlation ID: $correlationId"
    }

    fun getFrameworkDatasetsForCompanyMessage(dataType: DataType, companyId: String, reportingPeriod: String): String {
        return "Received a request to get all datasets together with meta info for framework '$dataType', " +
            "companyId '$companyId' and reporting period '$reportingPeriod'"
    }

    fun getCompanyAssociatedDataMessage(dataId: String, companyId: String): String {
        return "Received a request to get company data with dataId '$dataId' for companyId '$companyId'. "
    }

    fun getCompanyAssociatedDataSuccessMessage(dataId: String, companyId: String, correlationId: String): String {
        return "Received company data with dataId '$dataId' for companyId '$companyId' from framework data storage. " +
            "Correlation ID '$correlationId'"
    }
}

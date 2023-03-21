package org.dataland.datalandbackend

import org.dataland.datalandbackend.model.DataType
import org.springframework.stereotype.Component

/**
 * Enables a centralized generation of log messages for all Dataland backend operations.
 */

@Component("LogMessageBuilder")
class LogMessageBuilder {
    val accessDeniedExceptionMessage = "You are trying to access a unreviewed dataset"

    /**
     * Generates a message to inform that a correlation ID was generated for a request in association with a company ID
     * @param correlationId The generated correlation ID
     * @param companyId The company ID associated with the request
     * @returns the message to log
     */
    fun generatedCorrelationIdMessage(correlationId: String, companyId: String): String {
        return "Generated correlation ID '$correlationId' for the received request with company ID: $companyId."
    }

    /**
     * Generates a message to inform that a dataset for a specific company shall be posted
     * @param userId The ID of the user who requests the post
     * @param dataType The data type for which a dataset shall be posted
     * @param companyId The ID of the company for which a dataset shall be posted
     * @param reportingPeriod The reporting period for which a dataset shall be posted
     * @returns the message to log
     */
    fun postCompanyAssociatedDataMessage(
        userId: String,
        dataType: DataType,
        companyId: String,
        reportingPeriod: String,
    ): String {
        return "Received a request from user '$userId' to post company associated data of type $dataType " +
            "for company ID '$companyId' and the reporting period $reportingPeriod"
    }

    /**
     * Generates a message to inform that a dataset for a specific company has been successfully posted
     * @param companyId The ID of the company for which a dataset has been posted
     * @param correlationId The correlation ID in association with this operation
     * @returns the message to log
     */
    fun postCompanyAssociatedDataSuccessMessage(companyId: String, correlationId: String): String {
        return "Posted company associated data for companyId '$companyId'. Correlation ID: $correlationId"
    }

    /**
     * Generates a message to inform that a request was received to return all datasets together with meta info for
     * a specific company, data type and reporting period
     * @param dataType The data type for which all datasets shall be returned
     * @param companyId The ID of the company for which all datasets shall be returned
     * @param reportingPeriod The reporting period for which all datasets shall be returned
     * @returns the message to log
     */
    fun getFrameworkDatasetsForCompanyMessage(dataType: DataType, companyId: String, reportingPeriod: String): String {
        return "Received a request to get all datasets together with meta info for framework '$dataType', " +
            "companyId '$companyId' and reporting period '$reportingPeriod'"
    }

    /**
     * Generates a message to inform that a request was received to return a dataset by its data ID
     * @param dataId The ID of the dataset that shall be returned
     * @param companyId The ID of the company for which this dataset shall be returned
     * @returns the message to log
     */
    fun getCompanyAssociatedDataMessage(dataId: String, companyId: String): String {
        return "Received a request to get company data with dataId '$dataId' for companyId '$companyId'. "
    }

    /**
     * Generates a message to inform that a dataset has been successfully returned by its data ID
     * @param dataId The ID of the dataset that has been returned
     * @param companyId The ID of the company with which the dataset is associated with
     * @param correlationId The correlation ID in association with this operation
     * @returns the message to log
     */
    fun getCompanyAssociatedDataSuccessMessage(dataId: String, companyId: String, correlationId: String): String {
        return "Received company data with dataId '$dataId' for companyId '$companyId' from framework data storage. " +
            "Correlation ID '$correlationId'"
    }
}

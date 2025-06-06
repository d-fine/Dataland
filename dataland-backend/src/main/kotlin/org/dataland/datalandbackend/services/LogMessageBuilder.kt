package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackendutils.interfaces.DataDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.springframework.stereotype.Component

/**
 * Enables a centralized generation of log messages for all Dataland backend operations.
 */

@Suppress("TooManyFunctions")
@Component("LogMessageBuilder")
class LogMessageBuilder {
    /**
     * Generates an "access denied" message if a user without the necessary role tries to bypass the QA process
     * @param qaStatus the qa status of the respective dataset
     */
    fun generateAccessDeniedExceptionMessage(qaStatus: QaStatus) = "You are trying to access a ${qaStatus.toString().lowercase()} dataset."

    val bypassQaDeniedExceptionMessage = "You do not have the required permissions to bypass QA checks."
    val dynamicDatasetNotFoundSummary = "No dataset found."

    /**
     * Generates a message to inform that a correlationId has generated been for an operation and potentially
     * logs the companyId and/or dataId in association with this operation to improve traceability.
     * @param correlationId that has been generated
     * @param companyId associated with the operation
     * @param dataId associated with the operation
     * @returns the message to log
     */
    fun generateCorrelationIdMessage(
        correlationId: String,
        companyId: String?,
        dataId: String?,
    ): String {
        val parts = mutableListOf<String>()

        companyId?.let { parts.add("companyId: $it") }
        dataId?.let { parts.add("dataId: $it") }

        val idParts = parts.joinToString(" and ")

        return "Generated correlationId '$correlationId' for an operation associated with $idParts."
    }

    /**
     * Generates a message to inform that a correlationId has been generated for an operation and which [dataDimensions]
     * are associated with it.
     * @param correlationId that has been generated
     * @param dataDimensions associated with the operation
     * @returns the message to log
     */
    fun generateCorrelationIdMessage(
        dataDimensions: DataDimensions,
        correlationId: String,
    ): String =
        "Generated correlationId '$correlationId' for an operation associated with company ID '${dataDimensions.companyId}' " +
            "for reporting period '${dataDimensions.reportingPeriod}' and data type '${dataDimensions.dataType}'."

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
    ): String =
        "Received a request from user '$userId' to post company associated data of type $dataType " +
            "for company ID '$companyId' and the reporting period $reportingPeriod"

    /**
     * Generates a message to inform that a dataset for a specific company shall be posted
     * @param userId The ID of the user who requests the post
     * @param uploadedDatapoint The data point that shall be posted
     * @param bypassQa Whether the QA process shall be bypassed
     * @param correlationId The correlation ID in association with this operation
     * @returns the message to log
     */
    fun postDataPointMessage(
        userId: String,
        uploadedDatapoint: UploadedDataPoint,
        bypassQa: Boolean,
        correlationId: String,
    ): String =
        "Received a request from user '$userId' to post a '${uploadedDatapoint.dataPointType}' data point with bypassQa: " +
            "$bypassQa for company ID '${uploadedDatapoint.companyId}' and reporting period '${uploadedDatapoint.reportingPeriod}'. " +
            "The correlation ID is '$correlationId'."

    /**
     * Generates a message to inform that a dataset for a specific company has been successfully posted
     * @param companyId The ID of the company for which a dataset has been posted
     * @param correlationId The correlation ID in association with this operation
     * @returns the message to log
     */
    fun postCompanyAssociatedDataSuccessMessage(
        companyId: String,
        correlationId: String,
    ): String = "Posted company associated data for companyId '$companyId'. Correlation ID: $correlationId"

    /**
     * Generates a message to inform that a request was received to return all datasets together with meta info for
     * a specific company, data type and reporting period
     * @param dataType The data type for which all datasets shall be returned
     * @param companyId The ID of the company for which all datasets shall be returned
     * @param reportingPeriod The reporting period for which all datasets shall be returned
     * @returns the message to log
     */
    fun getFrameworkDatasetsForCompanyMessage(
        dataType: DataType,
        companyId: String,
        reportingPeriod: String,
    ): String =
        "Received a request to get all datasets together with meta info for framework '$dataType', " +
            "companyId '$companyId' and reporting period '$reportingPeriod'"

    /**
     * Generates a message to inform that a request was received to return a dataset by its data ID
     * @param dataId The ID of the dataset that shall be returned
     * @param companyId The ID of the company for which this dataset shall be returned
     * @returns the message to log
     */
    fun getCompanyAssociatedDataMessage(
        dataId: String,
        companyId: String,
    ): String = "Received a request to get company data with dataId '$dataId' for companyId '$companyId'. "

    /**
     * Generates a message to inform that a dataset has been successfully returned by its data ID
     * @param dataId The ID of the dataset that has been returned
     * @param companyId The ID of the company with which the dataset is associated with
     * @param correlationId The correlation ID in association with this operation
     * @returns the message to log
     */
    fun getCompanyAssociatedDataSuccessMessage(
        dataId: String,
        companyId: String,
        correlationId: String,
    ): String =
        "Received company data with dataId '$dataId' for companyId '$companyId' from framework data storage. " +
            "Correlation ID '$correlationId'"

    /**
     * Generates a message to inform that no dataset is available for the provided parameters
     * @param dataDimensions The data dimensions for which no dataset is available
     */
    fun getDynamicDatasetNotFoundMessage(dataDimensions: DataDimensions) =
        "No dataset available for data type ${dataDimensions.dataType} " +
            "reporting period ${dataDimensions.reportingPeriod} and company ID ${dataDimensions.companyId}."

    /**
     * Generates a message to inform that a request was received to return patch the meta info of a dataset
     * @param dataId The ID of the dataset that shall be returned
     * @param companyId The ID of the company for which this dataset shall be returned
     * @returns the message to log
     */
    fun patchDataMetaInformationMessage(
        userId: String?,
        dataId: String,
        dataType: String,
        companyId: String,
        reportingPeriod: String,
        correlationId: String,
    ): String =
        "Received a request to patchMetaDataInfo by user $userId to patch metadata of dataset with dataId $dataId and " +
            "data dimensions (companyId $companyId, dataType $dataType, reportingPeriod $reportingPeriod). " +
            "CorrelationID $correlationId."
}

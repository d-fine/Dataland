package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.services.messaging.BulkDataRequestEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Implementation of a request manager service for all operations concerning the processing of bulk data requests
 */
@Service("BulkDataRequestManager")
class BulkDataRequestManager(
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val emailMessageSender: BulkDataRequestEmailMessageSender,
    @Autowired private val utils: DataRequestProcessingUtils,
) {
    /**
     * Processes a bulk data request from a user
     * @param bulkDataRequest info provided by a user in order to request a bulk of datasets on Dataland.
     * @return relevant info to the user as a response after posting a bulk data request
     */
    @Transactional
    fun processBulkDataRequest(bulkDataRequest: BulkDataRequest): BulkDataRequestResponse {
        utils.throwExceptionIfNotJwtAuth()
        assureValidityOfRequests(bulkDataRequest)
        val correlationId = UUID.randomUUID().toString()
        dataRequestLogger.logMessageForBulkDataRequest(correlationId)
        val acceptedIdentifiers = mutableListOf<String>()
        val rejectedIdentifiers = mutableListOf<String>()
        val userProvidedIdentifierToDatalandCompanyIdMapping = mutableMapOf<String, CompanyIdAndName>()
        for (userProvidedIdentifier in bulkDataRequest.companyIdentifiers) {
            val datalandCompanyIdAndName =
                utils.getDatalandCompanyIdAndNameForIdentifierValue(userProvidedIdentifier, returnOnlyUnique = true)
            if (datalandCompanyIdAndName == null) {
                rejectedIdentifiers.add(userProvidedIdentifier)
                continue
            }
            userProvidedIdentifierToDatalandCompanyIdMapping[userProvidedIdentifier] = datalandCompanyIdAndName
            acceptedIdentifiers.add(userProvidedIdentifier)
            storeDataRequests(
                dataTypes = bulkDataRequest.dataTypes,
                reportingPeriods = bulkDataRequest.reportingPeriods,
                datalandCompanyId = datalandCompanyIdAndName.companyId,
            )
        }
        if (acceptedIdentifiers.isEmpty()) throwInvalidInputApiExceptionBecauseAllIdentifiersRejected()
        sendBulkDataRequestInternalEmailMessage(
            bulkDataRequest, userProvidedIdentifierToDatalandCompanyIdMapping.values.toList(), correlationId,
        )
        return buildResponseForBulkDataRequest(bulkDataRequest, rejectedIdentifiers, acceptedIdentifiers)
    }

    private fun storeDataRequests(
        dataTypes: Set<DataTypeEnum>,
        reportingPeriods: Set<String>,
        datalandCompanyId: String,
    ) {
        for (framework in dataTypes) {
            for (reportingPeriod in reportingPeriods) {
                if (!utils.existsDataRequestWithNonFinalStatus(datalandCompanyId, framework, reportingPeriod)) {
                    utils.storeDataRequestEntityAsOpen(datalandCompanyId, framework, reportingPeriod)
                }
            }
        }
    }

    private fun errorMessageForEmptyInputConfigurations(
        identifiers: Set<String>,
        frameworks: Set<DataTypeEnum>,
        reportingPeriods: Set<String>,
    ): String =
        when {
            identifiers.isEmpty() && frameworks.isEmpty() && reportingPeriods.isEmpty() ->
                "All " +
                    "provided lists are empty."

            identifiers.isEmpty() && frameworks.isEmpty() ->
                "The lists of company identifiers and " +
                    "frameworks are empty."

            identifiers.isEmpty() && reportingPeriods.isEmpty() ->
                "The lists of company identifiers and " +
                    "reporting periods are empty."

            frameworks.isEmpty() && reportingPeriods.isEmpty() ->
                "The lists of frameworks and reporting " +
                    "periods are empty."

            identifiers.isEmpty() -> "The list of company identifiers is empty."
            frameworks.isEmpty() -> "The list of frameworks is empty."
            else -> "The list of reporting periods is empty."
        }

    private fun assureValidityOfRequests(bulkDataRequest: BulkDataRequest) {
        val identifiers = bulkDataRequest.companyIdentifiers
        val frameworks = bulkDataRequest.dataTypes
        val reportingPeriods = bulkDataRequest.reportingPeriods
        if (identifiers.isEmpty() || frameworks.isEmpty() || reportingPeriods.isEmpty()) {
            val errorMessage =
                errorMessageForEmptyInputConfigurations(
                    identifiers, frameworks, reportingPeriods,
                )
            throw InvalidInputApiException(
                "No empty lists are allowed as input for bulk data request.",
                errorMessage,
            )
        }
    }

    private fun buildResponseMessageForBulkDataRequest(
        totalNumberOfRequestedCompanyIdentifiers: Int,
        numberOfRejectedCompanyIdentifiers: Int,
    ): String =
        when (numberOfRejectedCompanyIdentifiers) {
            0 -> "All of your $totalNumberOfRequestedCompanyIdentifiers distinct company identifiers were accepted."
            1 ->
                "One of your $totalNumberOfRequestedCompanyIdentifiers distinct company identifiers was rejected " +
                    "because it could not be uniquely matched with an existing company on Dataland."

            else ->
                "$numberOfRejectedCompanyIdentifiers of your $totalNumberOfRequestedCompanyIdentifiers distinct " +
                    "company identifiers were rejected because they could not be uniquely matched with existing " +
                    "companies on Dataland."
        }

    private fun buildResponseForBulkDataRequest(
        bulkDataRequest: BulkDataRequest,
        rejectedCompanyIdentifiers: List<String>,
        acceptedCompanyIdentifiers: List<String>,
    ): BulkDataRequestResponse =
        BulkDataRequestResponse(
            message =
                buildResponseMessageForBulkDataRequest(
                    bulkDataRequest.companyIdentifiers.size,
                    rejectedCompanyIdentifiers.size,
                ),
            acceptedCompanyIdentifiers = acceptedCompanyIdentifiers,
            rejectedCompanyIdentifiers = rejectedCompanyIdentifiers,
        )

    private fun sendBulkDataRequestInternalEmailMessage(
        bulkDataRequest: BulkDataRequest,
        acceptedDatalandCompanyIdsAndNames: List<CompanyIdAndName>,
        correlationId: String,
    ) {
        emailMessageSender.sendBulkDataRequestInternalMessage(
            bulkDataRequest,
            acceptedDatalandCompanyIdsAndNames,
            correlationId,
        )
        dataRequestLogger.logMessageForSendBulkDataRequestEmailMessage(correlationId)
    }

    private fun throwInvalidInputApiExceptionBecauseAllIdentifiersRejected() {
        val summary = "All provided company identifiers are not unique or could not be recognized."
        val message =
            "The company identifiers you provided could not be uniquely matched with an existing " +
                "company on dataland"
        throw InvalidInputApiException(
            summary,
            message,
        )
    }
}

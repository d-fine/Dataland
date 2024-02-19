package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandemail.email.EmailSender
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Implementation of a request manager service for all operations concerning the processing of bulk data requests
 */
@Service("BulkDataRequestManager")
class BulkDataRequestManager(
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val emailBuilder: BulkDataRequestEmailBuilder,
    @Autowired private val emailSender: EmailSender,
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
        val bulkDataRequestId = UUID.randomUUID().toString()
        dataRequestLogger.logMessageForBulkDataRequest(bulkDataRequestId)
        val acceptedIdentifiers = mutableListOf<String>()
        val rejectedIdentifiers = mutableListOf<String>()
        for (userProvidedIdentifierValue in bulkDataRequest.companyIdentifiers) {
            val matchedIdentifierType = utils.determineIdentifierTypeViaRegex(userProvidedIdentifierValue)
            if (matchedIdentifierType == null) {
                rejectedIdentifiers.add(userProvidedIdentifierValue)
                continue
            }
            acceptedIdentifiers.add(userProvidedIdentifierValue)
            processAcceptedIdentifier(
                userProvidedIdentifierValue,
                matchedIdentifierType,
                bulkDataRequest.dataTypes,
                bulkDataRequest.reportingPeriods,
            )
        }
        if (acceptedIdentifiers.isEmpty()) {
            throwInvalidInputApiExceptionBecauseAllIdentifiersRejected()
        }
        sendBulkDataRequestNotificationMail(bulkDataRequest, acceptedIdentifiers, bulkDataRequestId)
        return buildResponseForBulkDataRequest(bulkDataRequest, rejectedIdentifiers, acceptedIdentifiers)
    }

    private fun errorMessageForEmptyInputConfigurations(
        identifiers: Set<String>,
        frameworks: Set<DataTypeEnum>,
        reportingPeriods: Set<String>,
    ): String {
        return when {
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
    }

    private fun assureValidityOfRequests(bulkDataRequest: BulkDataRequest) {
        val identifiers = bulkDataRequest.companyIdentifiers
        val frameworks = bulkDataRequest.dataTypes
        val reportingPeriods = bulkDataRequest.reportingPeriods
        if (identifiers.isEmpty() || frameworks.isEmpty() || reportingPeriods.isEmpty()) {
            val errorMessage = errorMessageForEmptyInputConfigurations(
                identifiers, frameworks, reportingPeriods,
            )
            throw InvalidInputApiException(
                "No empty lists are allowed as input for bulk data request.",
                errorMessage,
            )
        }
    }

    private fun processAcceptedIdentifier(
        userProvidedIdentifierValue: String,
        matchedIdentifierType: DataRequestCompanyIdentifierType,
        requestedFrameworks: Set<DataTypeEnum>,
        requestedReportingPeriods: Set<String>,
    ) {
        val datalandCompanyId = utils.getDatalandCompanyIdForIdentifierValue(userProvidedIdentifierValue)
        val identifierTypeToStore = datalandCompanyId?.let {
            DataRequestCompanyIdentifierType.DatalandCompanyId
        } ?: matchedIdentifierType
        val identifierValueToStore = datalandCompanyId ?: userProvidedIdentifierValue
        for (framework in requestedFrameworks) {
            for (reportingPeriod in requestedReportingPeriods) {
                utils.storeDataRequestEntityIfNotExisting(
                    identifierValueToStore,
                    identifierTypeToStore,
                    framework,
                    reportingPeriod,
                )
            }
        }
    }

    private fun buildResponseMessageForBulkDataRequest(
        totalNumberOfRequestedCompanyIdentifiers: Int,
        numberOfRejectedCompanyIdentifiers: Int,
    ): String {
        return when (numberOfRejectedCompanyIdentifiers) {
            0 -> "$totalNumberOfRequestedCompanyIdentifiers distinct company identifiers were accepted."
            else ->
                "$numberOfRejectedCompanyIdentifiers of your $totalNumberOfRequestedCompanyIdentifiers " +
                    "distinct company identifiers were rejected because of a format that is not matching a valid " +
                    "LEI, ISIN or PermId."
        }
    }

    private fun buildResponseForBulkDataRequest(
        bulkDataRequest: BulkDataRequest,
        rejectedCompanyIdentifiers: List<String>,
        acceptedCompanyIdentifiers: List<String>,
    ): BulkDataRequestResponse {
        return BulkDataRequestResponse(
            message = buildResponseMessageForBulkDataRequest(
                bulkDataRequest.companyIdentifiers.size,
                rejectedCompanyIdentifiers.size,
            ),
            rejectedCompanyIdentifiers = rejectedCompanyIdentifiers,
            acceptedCompanyIdentifiers = acceptedCompanyIdentifiers,
        )
    }

    private fun sendBulkDataRequestNotificationMail(
        bulkDataRequest: BulkDataRequest,
        acceptedCompanyIdentifiers: List<String>,
        bulkDataRequestId: String,
    ) {
        val emailToSend = emailBuilder.buildBulkDataRequestEmail(
            bulkDataRequest,
            acceptedCompanyIdentifiers,
        )
        dataRequestLogger.logMessageForSendBulkDataRequestEmail(bulkDataRequestId)
        emailSender.sendEmail(emailToSend)
    }

    private fun throwInvalidInputApiExceptionBecauseAllIdentifiersRejected() {
        val summary = "All provided company identifiers have an invalid format."
        val message = "The company identifiers you provided do not match the patterns " +
            "of a valid LEI, ISIN or PermId."
        throw InvalidInputApiException(
            summary,
            message,
        )
    }
}

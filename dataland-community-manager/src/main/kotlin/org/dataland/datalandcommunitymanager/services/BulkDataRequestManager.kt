package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestManagerUtils
import org.dataland.datalandemail.email.EmailSender
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Implementation of a request manager service for all operations concerning the processing of bulk data requests
 */
@Service("BulkDataRequestManager")
class BulkDataRequestManager(
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val companyGetter: CompanyGetter,
    @Autowired private val emailBuilder: BulkDataRequestEmailBuilder,
    @Autowired private val emailSender: EmailSender,
    @Autowired private val objectMapper: ObjectMapper,
) {
    private val utils = DataRequestManagerUtils(dataRequestRepository, dataRequestLogger, companyGetter, objectMapper)

    /**
     * Processes a bulk data request from a user
     * @param bulkDataRequest info provided by a user in order to request a bulk of datasets on Dataland.
     * @return relevant info to the user as a response after posting a bulk data request
     */
    @Transactional
    fun processBulkDataRequest(bulkDataRequest: BulkDataRequest): BulkDataRequestResponse {
        val cleanedBulkDataRequest = runValidationsAndRemoveDuplicates(bulkDataRequest)
        val bulkDataRequestId = UUID.randomUUID().toString()
        dataRequestLogger.logMessageForBulkDataRequest(bulkDataRequestId)
        val acceptedIdentifiers = mutableListOf<String>()
        val rejectedIdentifiers = mutableListOf<String>()
        for (userProvidedIdentifierValue in cleanedBulkDataRequest.listOfCompanyIdentifiers) {
            val datalandCompanyId = utils.getDatalandCompanyIdForIdentifierValue(userProvidedIdentifierValue)
            if (datalandCompanyId == null) {
                rejectedIdentifiers.add(userProvidedIdentifierValue)
                continue
            }
            acceptedIdentifiers.add(userProvidedIdentifierValue)
            for (framework in cleanedBulkDataRequest.listOfFrameworkNames) {
                for (reportingPeriod in cleanedBulkDataRequest.listOfReportingPeriods) {
                    utils.storeDataRequestEntityIfNotExisting(
                        datalandCompanyId,
                        framework,
                        reportingPeriod,
                    )
                }
            }
        }
        if (!acceptedIdentifiers.isNotEmpty()) {
            throwInvalidInputApiExceptionBecauseAllIdentifiersRejected()
        }
        sendBulkDataRequestNotificationMail(cleanedBulkDataRequest, acceptedIdentifiers, bulkDataRequestId)
        return buildResponseForBulkDataRequest(cleanedBulkDataRequest, rejectedIdentifiers, acceptedIdentifiers)
    }

    /** This method retrieves all the data requests for the current user from the database and logs a message.
     * @returns all data requests for the current user
     */
    fun getDataRequestsForUser(): List<StoredDataRequest> {
        val currentUserId = DatalandAuthentication.fromContext().userId
        val retrievedStoredDataRequestEntitiesForUser = dataRequestRepository.findByUserId(currentUserId)
        val retrievedStoredDataRequestsForUser = retrievedStoredDataRequestEntitiesForUser.map { dataRequestEntity ->
            StoredDataRequest(
                dataRequestEntity.dataRequestId,
                dataRequestEntity.userId,
                dataRequestEntity.creationTimestamp,
                utils.getDataTypeEnumForFrameworkName(dataRequestEntity.dataTypeName),
                dataRequestEntity.reportingPeriod,
                dataRequestEntity.datalandCompanyId,
                objectMapper.readValue(
                    dataRequestEntity.messageHistory,
                    object : TypeReference<MutableList<StoredDataRequestMessageObject>>() {},
                ),
                dataRequestEntity.lastModifiedDate,
                dataRequestEntity.requestStatus,
            )
        }
        dataRequestLogger.logMessageForRetrievingDataRequestsForUser()
        return retrievedStoredDataRequestsForUser
    }

    /** This method triggers a query to get aggregated data requests.
     * @param identifierValue can be used to filter via substring matching
     * @param dataTypes can be used to filter on frameworks
     * @returns aggregated data requests
     */
    fun getAggregatedDataRequests(
        identifierValue: String?,
        dataTypes: Set<DataTypeEnum>?,
        reportingPeriod: String?,
    ): List<AggregatedDataRequest> {
        val dataTypesFilterForQuery = if (dataTypes != null && dataTypes.isEmpty()) {
            null
        } else {
            dataTypes?.map { it.value }
        }
        val aggregatedDataRequestEntities =
            dataRequestRepository.getAggregatedDataRequests(identifierValue, dataTypesFilterForQuery, reportingPeriod)
        val aggregatedDataRequests = aggregatedDataRequestEntities.map { aggregatedDataRequestEntity ->
            AggregatedDataRequest(
                utils.getDataTypeEnumForFrameworkName(aggregatedDataRequestEntity.dataTypeName),
                aggregatedDataRequestEntity.reportingPeriod,
                aggregatedDataRequestEntity.datalandCompanyId,
                aggregatedDataRequestEntity.count,
            )
        }
        return aggregatedDataRequests
    }

    private fun throwExceptionIfNotJwtAuth() {
        if (DatalandAuthentication.fromContext() !is DatalandJwtAuthentication) {
            throw AuthenticationMethodNotSupportedException()
        }
    }

    private fun errorMessageForEmptyInputConfigurations(
        listOfIdentifiers: List<String>,
        listOfFrameworks: List<DataTypeEnum>,
        listOfReportingPeriods: List<String>,
    ): String {
        return when {
            listOfIdentifiers.isEmpty() && listOfFrameworks.isEmpty() && listOfReportingPeriods.isEmpty() ->
                "All " +
                    "provided lists are empty."

            listOfIdentifiers.isEmpty() && listOfFrameworks.isEmpty() ->
                "The lists of company identifiers and " +
                    "frameworks are empty."

            listOfIdentifiers.isEmpty() && listOfReportingPeriods.isEmpty() ->
                "The lists of company identifiers and " +
                    "reporting periods are empty."

            listOfFrameworks.isEmpty() && listOfReportingPeriods.isEmpty() ->
                "The lists of frameworks and reporting " +
                    "periods are empty."

            listOfIdentifiers.isEmpty() -> "The list of company identifiers is empty."
            listOfFrameworks.isEmpty() -> "The list of frameworks is empty."
            else -> "The list of reporting periods is empty."
        }
    }

    private fun assureValidityOfRequestLists(bulkDataRequest: BulkDataRequest) {
        val listOfIdentifiers = bulkDataRequest.listOfCompanyIdentifiers
        val listOfFrameworks = bulkDataRequest.listOfFrameworkNames
        val listOfReportingPeriods = bulkDataRequest.listOfReportingPeriods
        if (listOfIdentifiers.isEmpty() || listOfFrameworks.isEmpty() || listOfReportingPeriods.isEmpty()) {
            val errorMessage = errorMessageForEmptyInputConfigurations(
                listOfIdentifiers, listOfFrameworks, listOfReportingPeriods,
            )
            throw InvalidInputApiException(
                "No empty lists are allowed as input for bulk data request.",
                errorMessage,
            )
        }
    }

    private fun removeDuplicatesInRequestLists(bulkDataRequest: BulkDataRequest): BulkDataRequest {
        val distinctCompanyIdentifiers = bulkDataRequest.listOfCompanyIdentifiers.distinct()
        val distinctFrameworkNames = bulkDataRequest.listOfFrameworkNames.distinct()
        val distinctReportingPeriods = bulkDataRequest.listOfReportingPeriods.distinct()
        return bulkDataRequest.copy(
            listOfCompanyIdentifiers = distinctCompanyIdentifiers,
            listOfFrameworkNames = distinctFrameworkNames,
            listOfReportingPeriods = distinctReportingPeriods,
        )
    }

    private fun runValidationsAndRemoveDuplicates(bulkDataRequest: BulkDataRequest): BulkDataRequest {
        throwExceptionIfNotJwtAuth()
        assureValidityOfRequestLists(bulkDataRequest)
        return removeDuplicatesInRequestLists(bulkDataRequest)
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
                bulkDataRequest.listOfCompanyIdentifiers.size,
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
        val summary = "All provided company identifiers have an invalid format or could not be recognized."
        val message = "The company identifiers you provided do not match the patterns of a valid LEI, ISIN or PermId."
        throw InvalidInputApiException(
            summary,
            message,
        )
    }
}

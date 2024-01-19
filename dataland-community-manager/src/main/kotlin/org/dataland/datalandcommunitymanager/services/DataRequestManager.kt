package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

/**
 * Implementation of a request manager service for all operations concerning the processing of data requests
 */
@Service("DataRequestManager")
class DataRequestManager(
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val companyGetter: CompanyGetter,
    @Autowired private val emailBuilder: EmailBuilder,
    @Autowired private val emailSender: EmailSender,
) {
    val isinRegex = Regex("^[A-Z]{2}[A-Z\\d]{10}$")
    val leiRegex = Regex("^[0-9A-Z]{18}[0-9]{2}$")
    val permIdRegex = Regex("^\\d+$")
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Processes a bulk data request from a user
     * @param bulkDataRequest info provided by a user in order to request a bulk of datasets on Dataland.
     * @return relevant info to the user as a response after posting a bulk data request
     */
    @Transactional
    fun processBulkDataRequest(bulkDataRequest: BulkDataRequest): BulkDataRequestResponse {
        val cleanedBulkDataRequest = runValidationsAndRemoveDuplicates(bulkDataRequest)
        val bulkDataRequestId = UUID.randomUUID().toString()
        val userId = DatalandAuthentication.fromContext().userId
        dataRequestLogger.logMessageForBulkDataRequest(bulkDataRequestId)
        val acceptedIdentifiers = mutableListOf<String>()
        val rejectedIdentifiers = mutableListOf<String>()
        for (userProvidedIdentifierValue in cleanedBulkDataRequest.listOfCompanyIdentifiers) {
            val matchedIdentifierType = determineIdentifierTypeViaRegex(userProvidedIdentifierValue)
            if (matchedIdentifierType == null) {
                rejectedIdentifiers.add(userProvidedIdentifierValue)
                continue
            }
            acceptedIdentifiers.add(userProvidedIdentifierValue)
            processAcceptedIdentifier(
                userProvidedIdentifierValue,
                matchedIdentifierType,
                cleanedBulkDataRequest.listOfFrameworkNames,
                cleanedBulkDataRequest.listOfReportingPeriods,
                userId,
                bulkDataRequestId,
            )
        }
        if (acceptedIdentifiers.isNotEmpty()) {
            sendBulkDataRequestNotificationMail(cleanedBulkDataRequest, acceptedIdentifiers, bulkDataRequestId)
        } else {
            throwInvalidInputApiExceptionBecauseAllIdentifiersRejected()
        }
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
                getDataTypeEnumForFrameworkName(dataRequestEntity.dataTypeName),
                dataRequestEntity.reportingPeriod,
                dataRequestEntity.dataRequestCompanyIdentifierType,
                dataRequestEntity.dataRequestCompanyIdentifierValue,
                dataRequestEntity.messageHistory,
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
                getDataTypeEnumForFrameworkName(aggregatedDataRequestEntity.dataTypeName),
                aggregatedDataRequestEntity.reportingPeriod,
                aggregatedDataRequestEntity.dataRequestCompanyIdentifierType,
                aggregatedDataRequestEntity.dataRequestCompanyIdentifierValue,
                aggregatedDataRequestEntity.count,
            )
        }
        return aggregatedDataRequests
    }

    /**
     * Method to patch the status of a data request.
     * @param dataRequestId the id of the data request to patch
     * @param requestStatus the status to apply to the data request
     * @return the updated data request object
     */
    @Transactional
    fun patchDataRequest(dataRequestId: String, requestStatus: String): StoredDataRequest {
        if (!dataRequestRepository.existsById(dataRequestId)) {
            throw ResourceNotFoundApiException("Data request not found", "Dataland does not know the Data request ID $dataRequestId")
        }
        var dataRequestEntity = dataRequestRepository.findById(dataRequestId).get()
        logger.info("Patching Company ${dataRequestEntity.dataRequestId} with status $requestStatus")
        if (requestStatus.lowercase() == "open") {
            dataRequestEntity.requestStatus = RequestStatus.Open
        } else if (requestStatus.lowercase() == "resolved") {
            dataRequestEntity.requestStatus = RequestStatus.Resolved
        } else {
            throw InvalidInputApiException("Invalid data request status", "$requestStatus is invalid")
        }
        dataRequestRepository.save(dataRequestEntity)
        dataRequestEntity = dataRequestRepository.findById(dataRequestId).get()
        return StoredDataRequest(
            dataRequestEntity.dataRequestId,
            dataRequestEntity.userId,
            dataRequestEntity.creationTimestamp,
            getDataTypeEnumForFrameworkName(dataRequestEntity.dataTypeName),
            dataRequestEntity.reportingPeriod,
            dataRequestEntity.dataRequestCompanyIdentifierType,
            dataRequestEntity.dataRequestCompanyIdentifierValue,
            dataRequestEntity.messageHistory,
            dataRequestEntity.lastModifiedDate,
            dataRequestEntity.requestStatus,
        )
    }

    private fun throwExceptionIfNotJwtAuth() {
        if (DatalandAuthentication.fromContext() !is DatalandJwtAuthentication) {
            throw AuthenticationMethodNotSupportedException()
        }
    }

    private fun assureValidityOfRequestLists(bulkDataRequest: BulkDataRequest) {
        val listOfIdentifiers = bulkDataRequest.listOfCompanyIdentifiers
        val listOfFrameworks = bulkDataRequest.listOfFrameworkNames
        val listOfReportingPeriods = bulkDataRequest.listOfReportingPeriods
        if (listOfIdentifiers.isEmpty() || listOfFrameworks.isEmpty() || listOfReportingPeriods.isEmpty()) {
            val errorMessage = when {
                listOfIdentifiers.isEmpty() && listOfFrameworks.isEmpty() && listOfReportingPeriods.isEmpty() -> "All provided lists are empty."
                listOfIdentifiers.isEmpty() && listOfFrameworks.isEmpty() -> "The lists of company identifiers and frameworks are empty."
                listOfIdentifiers.isEmpty() && listOfReportingPeriods.isEmpty() -> "The lists of company identifiers and reporting periods are empty."
                listOfFrameworks.isEmpty() && listOfReportingPeriods.isEmpty() -> "The lists of frameworks and reporting periods are empty."
                listOfIdentifiers.isEmpty() -> "The list of company identifiers is empty."
                listOfFrameworks.isEmpty() -> "The list of frameworks is empty."
                else -> "The list of reporting periods is empty."
            }
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

    private fun isDataRequestAlreadyExisting(
        requestingUserId: String,
        identifierValue: String,
        framework: DataTypeEnum,
        reportingPeriod: String,
    ): Boolean {
        val isAlreadyExisting = dataRequestRepository
            .existsByUserIdAndDataRequestCompanyIdentifierValueAndDataTypeNameAndReportingPeriod(
                requestingUserId, identifierValue, framework.name, reportingPeriod,
            )
        if (isAlreadyExisting) {
            dataRequestLogger
                .logMessageForCheckingIfDataRequestAlreadyExists(identifierValue, framework)
        }
        return isAlreadyExisting
    }

    private fun storeDataRequestIfNotExisting(
        identifierValue: String,
        identifierType: DataRequestCompanyIdentifierType,
        dataType: DataTypeEnum,
        reportingPeriod: String,
        userId: String,
        bulkDataRequestId: String,
    ) {
        if (!isDataRequestAlreadyExisting(userId, identifierValue, dataType, reportingPeriod)) {
            storeDataRequestEntity(
                buildDataRequestEntity(
                    userId,
                    dataType,
                    reportingPeriod,
                    identifierType,
                    identifierValue,
                ),
                bulkDataRequestId,
            )
        }
    }

    private fun processAcceptedIdentifier(
        userProvidedIdentifierValue: String,
        matchedIdentifierType: DataRequestCompanyIdentifierType,
        requestedFrameworks: List<DataTypeEnum>,
        requestedReportingPeriods: List<String>,
        userId: String,
        bulkDataRequestId: String,
    ) {
        val datalandCompanyId = getDatalandCompanyIdForIdentifierValue(userProvidedIdentifierValue)
        val identifierTypeToStore = datalandCompanyId?.let {
            DataRequestCompanyIdentifierType.DatalandCompanyId
        } ?: matchedIdentifierType
        val identifierValueToStore = datalandCompanyId ?: userProvidedIdentifierValue
        for (framework in requestedFrameworks) {
            for (reportingPeriod in requestedReportingPeriods) {
                storeDataRequestIfNotExisting(
                    identifierValueToStore,
                    identifierTypeToStore,
                    framework,
                    reportingPeriod,
                    userId,
                    bulkDataRequestId,
                )
            }
        }
    }

    private fun getDatalandCompanyIdForIdentifierValue(identifierValue: String): String? {
        var datalandCompanyId: String? = null
        val bearerTokenOfRequestingUser = DatalandAuthentication.fromContext().credentials as String
        val matchingCompanyIdsAndNamesOnDataland =
            companyGetter.getCompanyIdsAndNamesForSearchString(identifierValue, bearerTokenOfRequestingUser)
        if (matchingCompanyIdsAndNamesOnDataland.size == 1) {
            datalandCompanyId = matchingCompanyIdsAndNamesOnDataland.first().companyId
        }
        dataRequestLogger
            .logMessageWhenCrossReferencingIdentifierValueWithDatalandCompanyId(identifierValue, datalandCompanyId)
        return datalandCompanyId
    }

    private fun storeDataRequestEntity(dataRequestEntity: DataRequestEntity, bulkDataRequestId: String? = null) {
        dataRequestRepository.save(dataRequestEntity)
        dataRequestLogger.logMessageForStoringDataRequest(dataRequestEntity.dataRequestId, bulkDataRequestId)
    }

    private fun buildDataRequestEntity(
        currentUserId: String,
        framework: DataTypeEnum,
        reportingPeriod: String,
        identifierType: DataRequestCompanyIdentifierType,
        identifierValue: String,
    ): DataRequestEntity {
        val currentTimestamp = Instant.now().toEpochMilli()
        val dataRequestId = UUID.randomUUID().toString()
        return DataRequestEntity(
            dataRequestId = dataRequestId,
            userId = currentUserId,
            creationTimestamp = currentTimestamp,
            dataTypeName = framework.value,
            reportingPeriod = reportingPeriod,
            dataRequestCompanyIdentifierType = identifierType,
            dataRequestCompanyIdentifierValue = identifierValue,
            messageHistory = mutableListOf(),
            lastModifiedDate = currentTimestamp,
            requestStatus = RequestStatus.Open,
        )
    }

    private fun determineIdentifierTypeViaRegex(identifierValue: String): DataRequestCompanyIdentifierType? {
        val matchingRegexes = listOf(leiRegex, isinRegex, permIdRegex).filter { it.matches(identifierValue) }
        return when (matchingRegexes.size) {
            0 -> null
            1 -> {
                when {
                    matchingRegexes[0] == leiRegex -> DataRequestCompanyIdentifierType.Lei
                    matchingRegexes[0] == isinRegex -> DataRequestCompanyIdentifierType.Isin
                    matchingRegexes[0] == permIdRegex -> DataRequestCompanyIdentifierType.PermId
                    else -> null
                }
            }
            else -> DataRequestCompanyIdentifierType.MultipleRegexMatches
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
        val bulkDataRequestNotificationMailLoggerFunction = {
            dataRequestLogger
                .logMessageForBulkDataRequestNotificationMail(emailToSend, bulkDataRequestId)
        }
        emailSender.sendEmail(emailToSend, bulkDataRequestNotificationMailLoggerFunction)
    }

    private fun throwInvalidInputApiExceptionBecauseAllIdentifiersRejected() {
        val summary = "All provided company identifiers have an invalid format."
        val message = "The company identifiers you provided do not match the patterns of a valid LEI, ISIN or PermId."
        throw InvalidInputApiException(
            summary,
            message,
        )
    }

    private fun getDataTypeEnumForFrameworkName(frameworkName: String): DataTypeEnum? {
        return DataTypeEnum.values().find { it.value == frameworkName }
    }
}

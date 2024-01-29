package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandemail.email.EmailSender
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
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
    @Autowired private val bulkDataRequestEmailBuilder: BulkDataRequestEmailBuilder,
    @Autowired private val singleDataRequestEmailBuilder: SingleDataRequestEmailBuilder,
    @Autowired private val emailSender: EmailSender,
    @Autowired private val objectMapper: ObjectMapper,
) {
    val isinRegex = Regex("^[A-Z]{2}[A-Z\\d]{10}$")
    val leiRegex = Regex("^[0-9A-Z]{18}[0-9]{2}$")
    val permIdRegex = Regex("^\\d+$")
    val companyIdRegex = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\$")
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
     * Method to retrieve a data request by its ID
     * @param dataRequestId the ID of the data request to retrieve
     * @return the data request corresponding to the provided ID
     */
    @Transactional
    fun getDataRequestById(dataRequestId: String): StoredDataRequest {
        throwResourceNotFoundExceptionIfDataRequestIdUnknown(dataRequestId)
        val dataRequestEntity = dataRequestRepository.findById(dataRequestId).get()
        return buildStoredDataRequestFromDataRequestEntity(dataRequestEntity)
    }

    /**
     * Method to patch the status of a data request.
     * @param dataRequestId the id of the data request to patch
     * @param requestStatus the status to apply to the data request
     * @return the updated data request object
     */
    @Transactional
    fun patchDataRequest(dataRequestId: String, requestStatus: RequestStatus): StoredDataRequest {
        throwResourceNotFoundExceptionIfDataRequestIdUnknown(dataRequestId)
        var dataRequestEntity = dataRequestRepository.findById(dataRequestId).get()
        logger.info("Patching Company ${dataRequestEntity.dataRequestId} with status $requestStatus")
        dataRequestEntity.requestStatus = requestStatus
        dataRequestRepository.save(dataRequestEntity)
        dataRequestEntity = dataRequestRepository.findById(dataRequestId).get()
        return buildStoredDataRequestFromDataRequestEntity(dataRequestEntity)
    }

    /**
     * Method to get all data requests based on filters.
     * @param dataType the framework to apply to the data request
     * @param requestStatus the status to apply to the data request
     * @param userId the user to apply to the data request
     * @param reportingPeriod the reporting period to apply to the data request
     * @param dataRequestCompanyIdentifierValue the company identifier value to apply to the data request
     * @return all filtered data requests
     */
    fun getDataRequests(
        dataType: DataTypeEnum?,
        userId: String?,
        requestStatus: RequestStatus?,
        reportingPeriod: String?,
        dataRequestCompanyIdentifierValue: String?,
    ): List<StoredDataRequest>? {
        var result = dataRequestRepository.findAll()
        fun updateResult(otherList: List<DataRequestEntity>) {
            result = result.intersect(otherList.toSet()).toMutableList()
        }

        if (dataType != null) {
            updateResult(dataRequestRepository.findByDataTypeName(dataType.toString()))
        }
        if (userId != null) {
            updateResult(dataRequestRepository.findByUserId(userId))
        }
        if (requestStatus != null) {
            updateResult(dataRequestRepository.findByRequestStatus(requestStatus))
        }
        if (reportingPeriod != null) {
            updateResult(dataRequestRepository.findByReportingPeriod(reportingPeriod))
        }
        if (dataRequestCompanyIdentifierValue != null) {
            result = result.intersect(
                dataRequestRepository.findByDataRequestCompanyIdentifierValue(dataRequestCompanyIdentifierValue),
            ).toMutableList()
            updateResult(
                dataRequestRepository.findByDataRequestCompanyIdentifierValue(dataRequestCompanyIdentifierValue),
            )
        }
        return result.map { buildStoredDataRequestFromDataRequestEntity(it) }
    }

    private fun throwResourceNotFoundExceptionIfDataRequestIdUnknown(dataRequestId: String) {
        if (!dataRequestRepository.existsById(dataRequestId)) {
            throw ResourceNotFoundApiException(
                "Data request not found",
                "Dataland does not know the Data request ID $dataRequestId",
            )
        }
    }

    private fun buildStoredDataRequestFromDataRequestEntity(dataRequestEntity: DataRequestEntity): StoredDataRequest {
        return StoredDataRequest(
            dataRequestEntity.dataRequestId,
            dataRequestEntity.userId,
            dataRequestEntity.creationTimestamp,
            getDataTypeEnumForFrameworkName(dataRequestEntity.dataTypeName),
            dataRequestEntity.reportingPeriod,
            dataRequestEntity.dataRequestCompanyIdentifierType,
            dataRequestEntity.dataRequestCompanyIdentifierValue,
            objectMapper.readValue(
                dataRequestEntity.messageHistory,
                object : TypeReference<MutableList<StoredDataRequestMessageObject>>() {},
            ),
            dataRequestEntity.lastModifiedDate,
            dataRequestEntity.requestStatus,
        )
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

    private fun storeDataRequestEntityIfNotExisting(
        identifierValue: String,
        identifierType: DataRequestCompanyIdentifierType,
        dataType: DataTypeEnum,
        reportingPeriod: String,
        userId: String,
        bulkDataRequestId: String,
        contactList: List<String>? = null,
        message: String? = null,
    ): DataRequestEntity {
        val dataRequestEntity = buildDataRequestEntity(
            userId,
            dataType,
            reportingPeriod,
            identifierType,
            identifierValue,
            contactList,
            message,
        )
        if (!isDataRequestAlreadyExisting(userId, identifierValue, dataType, reportingPeriod)) {
            storeDataRequestEntity(dataRequestEntity, bulkDataRequestId)
        }
        return dataRequestEntity
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
                storeDataRequestEntityIfNotExisting(
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

    private fun storeDataRequestEntity(dataRequestEntity: DataRequestEntity, dataRequestId: String? = null) {
        dataRequestRepository.save(dataRequestEntity)
        dataRequestLogger.logMessageForStoringDataRequest(dataRequestEntity.dataRequestId, dataRequestId)
    }

    private fun buildDataRequestEntity(
        currentUserId: String,
        framework: DataTypeEnum,
        reportingPeriod: String,
        identifierType: DataRequestCompanyIdentifierType,
        identifierValue: String,
        contactList: List<String>?,
        message: String?,
    ): DataRequestEntity {
        val currentTimestamp = Instant.now().toEpochMilli()
        val dataRequestId = UUID.randomUUID().toString()
        val messageHistory = if (contactList != null || message != null) {
            mutableListOf(StoredDataRequestMessageObject(contactList, message, currentTimestamp))
        } else {
            mutableListOf()
        }
        return DataRequestEntity(
            dataRequestId = dataRequestId,
            userId = currentUserId,
            creationTimestamp = currentTimestamp,
            dataTypeName = framework.value,
            reportingPeriod = reportingPeriod,
            dataRequestCompanyIdentifierType = identifierType,
            dataRequestCompanyIdentifierValue = identifierValue,
            messageHistory = objectMapper.writeValueAsString(messageHistory),
            lastModifiedDate = currentTimestamp,
            requestStatus = RequestStatus.Open,
        )
    }

    private fun determineIdentifierTypeViaRegex(identifierValue: String): DataRequestCompanyIdentifierType? {
        val matchingRegexes =
            listOf(leiRegex, isinRegex, permIdRegex).filter { it.matches(identifierValue) }
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
        val emailToSend = bulkDataRequestEmailBuilder.buildBulkDataRequestEmail(
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

    private fun throwInvalidInputApiExceptionBecauseIdentifierRejected() {
        val summary = "The provided company identifier has an invalid format."
        val message = "The company identifier you provided does not match the patterns " +
            "of a valid LEI, ISIN, PermId or Dataland CompanyID."
        throw InvalidInputApiException(
            summary,
            message,
        )
    }

    private fun getDataTypeEnumForFrameworkName(frameworkName: String): DataTypeEnum? {
        return DataTypeEnum.entries.find { it.value == frameworkName }
    }

    private fun checkIfCompanyIsValid(companyId: String) {
        val bearerTokenOfRequestingUser = DatalandAuthentication.fromContext().credentials as String
        try {
            companyGetter.getCompanyById(companyId, bearerTokenOfRequestingUser)
        } catch (e: ClientException) { if (e.statusCode == HttpStatus.NOT_FOUND.value()) {
            throw ResourceNotFoundApiException(
                "Company not found",
                "Dataland-backend does not know the company ID $companyId",
            )
        }
        }
    }

    /**
     * Processes a single data request from a user
     * @param singleDataRequest info provided by a user in order to request a single dataset on Dataland
     * @return the stored data request object
     */

    @Transactional
    fun processSingleDataRequest(singleDataRequest: SingleDataRequest): List<StoredDataRequest> {
        throwExceptionIfNotJwtAuth()
        val listOfReportingPeriods = singleDataRequest.listOfReportingPeriods.distinct()
        val singleDataRequestId = UUID.randomUUID().toString()
        val userId = DatalandAuthentication.fromContext().userId
        lateinit var identifierTypeToStore: DataRequestCompanyIdentifierType
        lateinit var identifierValueToStore: String
        val storedDataRequests = mutableListOf<StoredDataRequest>()
        var datalandCompanyIdIfExists: String? = null
        if (companyIdRegex.matches(singleDataRequest.companyIdentifier)) {
            checkIfCompanyIsValid(singleDataRequest.companyIdentifier)
            identifierTypeToStore = DataRequestCompanyIdentifierType.DatalandCompanyId
            identifierValueToStore = singleDataRequest.companyIdentifier
            datalandCompanyIdIfExists = singleDataRequest.companyIdentifier
        } else {
            val matchedIdentifierType = determineIdentifierTypeViaRegex(singleDataRequest.companyIdentifier)
            dataRequestLogger.logMessageForSingleDataRequest(singleDataRequest.companyIdentifier)
            if (matchedIdentifierType != null) {
                datalandCompanyIdIfExists = getDatalandCompanyIdForIdentifierValue(singleDataRequest.companyIdentifier)
                identifierTypeToStore = datalandCompanyIdIfExists?.let {
                    DataRequestCompanyIdentifierType.DatalandCompanyId
                } ?: matchedIdentifierType
                identifierValueToStore = datalandCompanyIdIfExists ?: singleDataRequest.companyIdentifier
            } else {
                throwInvalidInputApiExceptionBecauseIdentifierRejected()
            }
        }
        for (reportingPeriod in listOfReportingPeriods) {
            storedDataRequests.add(
                buildStoredDataRequestFromDataRequestEntity(
                    storeDataRequestEntityIfNotExisting(
                        identifierValueToStore,
                        identifierTypeToStore,
                        singleDataRequest.frameworkName,
                        reportingPeriod,
                        userId,
                        singleDataRequestId,
                        singleDataRequest.contactList,
                        singleDataRequest.message,
                    ),
                ),
            )
        }
        emailSender.sendEmail(
            singleDataRequestEmailBuilder.buildSingleDataRequestEmail(
                requesterEmail = (DatalandAuthentication.fromContext() as DatalandJwtAuthentication).username,
                companyId = datalandCompanyIdIfExists,
                singleDataRequest = singleDataRequest,
            ),
        )
        return storedDataRequests
    }
}

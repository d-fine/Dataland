package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.email.EmailSender
import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
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
    @Autowired private val emailBuilder: BulkDataRequestEmailBuilder,
    @Autowired private val emailSender: EmailSender,
) {
    val isinRegex = Regex("^[A-Z]{2}[A-Z\\d]{10}$")
    val leiRegex = Regex("^[0-9A-Z]{18}[0-9]{2}$")
    val permIdRegex = Regex("^\\d+$")

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
                bulkDataRequest.listOfFrameworkNames,
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
                dataRequestEntity.dataRequestCompanyIdentifierType,
                dataRequestEntity.dataRequestCompanyIdentifierValue,
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
    ): List<AggregatedDataRequest> {
        val dataTypesFilterForQuery = if (dataTypes != null && dataTypes.isEmpty()) {
            null
        } else {
            dataTypes?.map { it.value }
        }
        val aggregatedDataRequestEntities =
            dataRequestRepository.getAggregatedDataRequests(identifierValue, dataTypesFilterForQuery)
        val aggregatedDataRequests = aggregatedDataRequestEntities.map { aggregatedDataRequestEntity ->
            AggregatedDataRequest(
                getDataTypeEnumForFrameworkName(aggregatedDataRequestEntity.dataTypeName),
                aggregatedDataRequestEntity.dataRequestCompanyIdentifierType,
                aggregatedDataRequestEntity.dataRequestCompanyIdentifierValue,
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

    private fun assureValidityOfRequestLists(bulkDataRequest: BulkDataRequest) {
        val listOfIdentifiers = bulkDataRequest.listOfCompanyIdentifiers
        val listOfFrameworks = bulkDataRequest.listOfFrameworkNames
        if (listOfIdentifiers.isEmpty() || listOfFrameworks.isEmpty()) {
            val errorMessage = when {
                listOfIdentifiers.isEmpty() && listOfFrameworks.isEmpty() -> "All provided lists are empty."
                listOfIdentifiers.isEmpty() -> "The list of company identifiers is empty."
                else -> "The list of frameworks is empty."
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
        return bulkDataRequest.copy(
            listOfCompanyIdentifiers = distinctCompanyIdentifiers,
            listOfFrameworkNames = distinctFrameworkNames,
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
    ): Boolean {
        val isAlreadyExisting = dataRequestRepository.existsByUserIdAndDataRequestCompanyIdentifierValueAndDataTypeName(
            requestingUserId, identifierValue, framework.name,
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
        userId: String,
        bulkDataRequestId: String,
    ) {
        if (!isDataRequestAlreadyExisting(userId, identifierValue, dataType)) {
            storeDataRequestEntity(
                buildDataRequestEntity(
                    userId,
                    dataType,
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
        userId: String,
        bulkDataRequestId: String,
    ) {
        val datalandCompanyId = getDatalandCompanyIdForIdentifierValue(userProvidedIdentifierValue)
        val identifierTypeToStore = datalandCompanyId?.let {
            DataRequestCompanyIdentifierType.DatalandCompanyId
        } ?: matchedIdentifierType
        val identifierValueToStore = datalandCompanyId ?: userProvidedIdentifierValue
        for (framework in requestedFrameworks) {
            storeDataRequestIfNotExisting(
                identifierValueToStore,
                identifierTypeToStore,
                framework,
                userId,
                bulkDataRequestId,
            )
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
        identifierType: DataRequestCompanyIdentifierType,
        identifierValue: String,
    ): DataRequestEntity {
        return DataRequestEntity(
            dataRequestId = UUID.randomUUID().toString(),
            userId = currentUserId,
            creationTimestamp = Instant.now().toEpochMilli(),
            dataTypeName = framework.value,
            dataRequestCompanyIdentifierType = identifierType,
            dataRequestCompanyIdentifierValue = identifierValue,
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
        dataRequestLogger
            .logMessageForBulkDataRequestNotificationMail(emailToSend, bulkDataRequestId)
        emailSender.sendEmail(emailToSend)
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

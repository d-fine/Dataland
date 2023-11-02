package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
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

    /**
     * Processes a bulk data request from a user
     * @param bulkDataRequest info provided by a user in order to request a bulk of datasets on Dataland.
     * @return relevant info to the user as a response after posting a bulk data request
     */
    @Transactional
    fun processBulkDataRequest(bulkDataRequest: BulkDataRequest): BulkDataRequestResponse {
        validateBulkDataRequest(bulkDataRequest)
        val cleanedBulkDataRequest = removeDuplicatesInLists(bulkDataRequest)
        val bulkDataRequestId = UUID.randomUUID().toString()
        val userId = DatalandAuthentication.fromContext().userId
        dataRequestLogger.logMessageForBulkDataRequest(userId, bulkDataRequestId)

        val acceptedCompanyIdentifiers = mutableListOf<String>()
        val rejectedCompanyIdentifiers = mutableListOf<String>()

        for (userProvidedIdentifierValue in cleanedBulkDataRequest.listOfCompanyIdentifiers) {
            val matchedIdentifierType = determineIdentifierTypeViaRegex(userProvidedIdentifierValue)
            if (matchedIdentifierType == null) {
                rejectedCompanyIdentifiers.add(userProvidedIdentifierValue)
                continue
            }
            acceptedCompanyIdentifiers.add(userProvidedIdentifierValue)

            val datalandCompanyId = getDatalandCompanyIdForIdentifierValue(userProvidedIdentifierValue)
            val identifierTypeToStore = datalandCompanyId?.let {
                DataRequestCompanyIdentifierType.DatalandCompanyId
            } ?: matchedIdentifierType
            val identifierValueToStore = datalandCompanyId ?: userProvidedIdentifierValue

            for (framework in cleanedBulkDataRequest.listOfFrameworkNames) {
                if (isDataRequestAlreadyExisting(userId, identifierValueToStore, framework)) {
                    continue
                }
                storeDataRequestEntity(
                    buildDataRequestEntity(userId, framework, identifierTypeToStore, identifierValueToStore),
                    bulkDataRequestId,
                )
            }
        }

        if (acceptedCompanyIdentifiers.isNotEmpty()) {
            sendBulkDataRequestNotificationMail(cleanedBulkDataRequest, acceptedCompanyIdentifiers, bulkDataRequestId)
        }
        return buildResponseForBulkDataRequest(
            cleanedBulkDataRequest, rejectedCompanyIdentifiers, acceptedCompanyIdentifiers,
        )
    }

    /** This method retrieves all the data requests for the current user from the database and logs a message.
     * @returns all data requests for the current user
     */
    fun getDataRequestsForUser(): List<DataRequestEntity> {
        val currentUserId = DatalandAuthentication.fromContext().userId
        val retrievedDataRequestsForUser = dataRequestRepository.findByUserId(currentUserId)
        dataRequestLogger.logMessageForRetrievingDataRequestsForUser(currentUserId)
        return retrievedDataRequestsForUser
    }

    private fun removeDuplicatesInLists(bulkDataRequest: BulkDataRequest): BulkDataRequest {
        val distinctCompanyIdentifiers = bulkDataRequest.listOfCompanyIdentifiers.distinct()
        val distinctFrameworkNames = bulkDataRequest.listOfFrameworkNames.distinct()

        return bulkDataRequest.copy(
            listOfCompanyIdentifiers = distinctCompanyIdentifiers,
            listOfFrameworkNames = distinctFrameworkNames,
        )
    }

    private fun validateBulkDataRequest(bulkDataRequest: BulkDataRequest) {
        val listOfIdentifiers = bulkDataRequest.listOfCompanyIdentifiers
        val listOfFrameworks = bulkDataRequest.listOfFrameworkNames
        if (listOfIdentifiers.isEmpty() || listOfFrameworks.isEmpty()) {
            val errorMessage = when {
                listOfIdentifiers.isEmpty() && listOfFrameworks.isEmpty() -> "All provided lists are empty"
                listOfIdentifiers.isEmpty() -> "The list of company identifiers is empty."
                else -> "The list of frameworks is empty."
            }
            throw InvalidInputApiException(
                "No empty lists are allowed as input for bulk data request.",
                errorMessage,
            )
        }
    }

    private fun isDataRequestAlreadyExisting(
        requestingUserId: String,
        identifierValue: String,
        framework: DataTypeEnum,
    ): Boolean {
        val isAlreadyExisting = dataRequestRepository.existsByUserIdAndDataRequestCompanyIdentifierValueAndDataType(
            requestingUserId, identifierValue, framework,
        )
        if (isAlreadyExisting) {
            dataRequestLogger
                .logMessageForCheckingIfDataRequestAlreadyExists(requestingUserId, identifierValue, framework)
        }
        return isAlreadyExisting
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
            dataType = framework,
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
            0 -> "$totalNumberOfRequestedCompanyIdentifiers data requests were accepted."
            else ->
                "$numberOfRejectedCompanyIdentifiers of your $totalNumberOfRequestedCompanyIdentifiers " +
                    "company identifiers were rejected because of a format that is not matching a valid " +
                    "LEI, ISIN or PermID."
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
}

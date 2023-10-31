package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
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
    val isinRegex = Regex("^([A-Z]{2})([0-9A-Z]{9})([0-9])$") // TODO at the end, validate correctness
    val leiRegex = Regex("^[0-9A-Z]{18}$") // TODO at the end, validate correctness
    val permIdRegex = Regex("^\\d{8}-\\d{4}$") // TODO at the end, validate correctness

    /**
     * Processes a bulk data request from a user
     * @param bulkDataRequest info provided by a user in order to request a bulk of datasets on Dataland.
     * @return relevant info to the user as a response after posting a bulk data request
     */
    // @Transactional        // TODO find out if this annotation is required and makes sense
    fun processBulkDataRequest(bulkDataRequest: BulkDataRequest): BulkDataRequestResponse {
        val bulkDataRequestId = UUID.randomUUID().toString()
        val currentUserId = DatalandAuthentication.fromContext().userId
        dataRequestLogger.logMessageForBulkDataRequest(currentUserId, bulkDataRequestId)
        val acceptedCompanyIdentifiers = mutableListOf<String>()
        val rejectedCompanyIdentifiers = mutableListOf<String>()
        val listOfDataRequestEntitiesToStore = mutableListOf<DataRequestEntity>()
        for (identifierValue in bulkDataRequest.listOfCompanyIdentifiers) {
            val identifierType = determineIdentifierTypeViaRegexMatching(identifierValue)
            if (identifierType != null) {
                acceptedCompanyIdentifiers.add(identifierValue)
                for (framework in bulkDataRequest.listOfFrameworkNames) {
                    if (!isDataRequestAlreadyExisting(currentUserId, identifierValue, framework)) {
                        /*
                        TODO commented out because backend cannot do this currently
                        val companyId = companyGetter.getCompanyIdByIdentifier(identifierValue)
                        */
                        listOfDataRequestEntitiesToStore.add(
                            buildDataRequestEntity(currentUserId, framework, identifierType, identifierValue, null),
                        )
                    }
                }
            } else {
                rejectedCompanyIdentifiers.add(identifierValue)
            }
        }
        for (dataRequestEntity in listOfDataRequestEntitiesToStore) {
            storeDataRequestEntity(dataRequestEntity, bulkDataRequestId)
        }
        if (acceptedCompanyIdentifiers.isNotEmpty()) {
            sendBulkDataRequestNotificationMail(
                bulkDataRequest, rejectedCompanyIdentifiers, acceptedCompanyIdentifiers, bulkDataRequestId,
            )
        }
        return buildResponseForBulkDataRequest(bulkDataRequest, rejectedCompanyIdentifiers, acceptedCompanyIdentifiers)
    }

    /** This method retrieves all the data requests for the current user from the database and logs a message.
     * @returns all data requests for the current user
     */
    fun getDataRequestsForUser(): List<DataRequestEntity> {
        // TODO I noticed that we use smth else in the api key manager for getting currentUserId.  Why?
        val currentUserId = DatalandAuthentication.fromContext().userId
        val retrievedDataRequestsForUser = dataRequestRepository.findByUserId(currentUserId)
        dataRequestLogger.logMessageForRetrievingDataRequestsForUser(currentUserId)
        return retrievedDataRequestsForUser
    }

    private fun isDataRequestAlreadyExisting(
        requestingUserId: String,
        identifierValue: String,
        framework: DataTypeEnum,
    ): Boolean {
        val isAlreadyExisting = dataRequestRepository.existsByUserIdAndCompanyIdentifierValueAndDataType(
            requestingUserId, identifierValue, framework,
        )
        if (isAlreadyExisting) {
            dataRequestLogger
                .logMessageForCheckingIfDataRequestAlreadyExists(requestingUserId, identifierValue, framework)
        }
        return isAlreadyExisting
    }

    private fun storeDataRequestEntity(dataRequestEntity: DataRequestEntity, bulkDataRequestId: String? = null) {
        dataRequestRepository.save(dataRequestEntity)
        dataRequestLogger.logMessageForStoringDataRequest(dataRequestEntity.dataRequestId, bulkDataRequestId)
    }

    private fun buildDataRequestEntity(
        currentUserId: String,
        framework: DataTypeEnum,
        identifierType: IdentifierType,
        identifierValue: String,
        companyId: String?,
    ): DataRequestEntity {
        return DataRequestEntity(
            dataRequestId = UUID.randomUUID().toString(),
            userId = currentUserId,
            creationTimestamp = Instant.now().toEpochMilli(),
            dataType = framework,
            companyIdentifierType = identifierType,
            companyIdentifierValue = identifierValue,
            companyIdOnDataland = companyId,
        )
    }

    private fun determineIdentifierTypeViaRegexMatching(identifierValue: String): IdentifierType? {
        return when {
            isinRegex.matches(identifierValue) -> IdentifierType.isin
            leiRegex.matches(identifierValue) -> IdentifierType.lei
            permIdRegex.matches(identifierValue) -> IdentifierType.permId
            else -> null
        }
    }

    private fun buildResponseMessageForBulkDataRequest(
        totalNumberOfRequestedCompanyIdentifiers: Int,
        numberOfRejectedCompanyIdentifiers: Int,
    ): String {
        return when (numberOfRejectedCompanyIdentifiers) {
            0 -> "$totalNumberOfRequestedCompanyIdentifiers data requests were created."
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
        rejectedCompanyIdentifiers: List<String>,
        acceptedCompanyIdentifiers: List<String>,
        bulkDataRequestId: String,
    ) {
        val emailToSend = emailBuilder.buildBulkDataRequestEmail(
            bulkDataRequest,
            rejectedCompanyIdentifiers,
            acceptedCompanyIdentifiers,
        )
        val bulkDataRequestNotificationMailLoggerFunction = {
            dataRequestLogger
                .logMessageForBulkDataRequestNotificationMail(emailToSend, bulkDataRequestId)
        }
        emailSender.sendEmail(emailToSend, bulkDataRequestNotificationMailLoggerFunction)
    }
}

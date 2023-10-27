package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

/**
 * Implementation of a request manager service for all operations concerning the processing of data requests
 */
@Service("RequestManager")
class RequestManager(
    // @Autowired private val communityRepository: CommunityRepository,
) {
    var inMemoryDataRequestStore: MutableMap<String, DataRequestEntity> = mutableMapOf()

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
        val currentUserId = DatalandAuthentication.fromContext().userId
        val acceptedCompanyIdentifiers = mutableListOf<String>()
        val rejectedCompanyIdentifiers = mutableListOf<String>()
        val listOfDataRequestEntitiesToStore = mutableListOf<DataRequestEntity>()
        for (identifierValue in bulkDataRequest.listOfCompanyIdentifiers) {
            val identifierType = determineIdentifierTypeViaRegexMatching(identifierValue)
            if (identifierType != null) {
                acceptedCompanyIdentifiers.add(identifierValue)
                for (framework in bulkDataRequest.listOfFrameworkNames) {
                    if (!isDataRequestAlreadyExisting(identifierValue, framework, currentUserId)) {
                        // TODO try to find a possibly already existing companyId on Dataland that matches the provided company identifier and add it
                        listOfDataRequestEntitiesToStore.add(
                            buildDataRequestEntity(currentUserId, framework, identifierType, identifierValue, null),
                        )
                    }
                }
            } else {
                rejectedCompanyIdentifiers.add(identifierValue)
            }
        }
        for (dataRequestEntity in listOfDataRequestEntitiesToStore) { // TODO: store in DB
            inMemoryDataRequestStore[dataRequestEntity.dataRequestId] = dataRequestEntity
        }
        // TODO send a notification mail
        // TODO build a message that the user should receive inside of the response to the POST-request
        return buildResponseForBulkDataRequest(bulkDataRequest, rejectedCompanyIdentifiers, acceptedCompanyIdentifiers)
    }

    fun getDataRequestsForUser(): List<DataRequestEntity> {
        val currentUserId = DatalandAuthentication.fromContext().userId // TODO =>
        // TODO I noticed that we use smth else in the api key manager for this.  why?
        return inMemoryDataRequestStore
            .filterValues { it.userId == currentUserId }
            .values
            .toList()
    }

    private fun determineIdentifierTypeViaRegexMatching(identifierValue: String): IdentifierType? {
        return when {
            isinRegex.matches(identifierValue) -> IdentifierType.isin
            leiRegex.matches(identifierValue) -> IdentifierType.lei
            permIdRegex.matches(identifierValue) -> IdentifierType.permId
            else -> null
        }
    }

    private fun isDataRequestAlreadyExisting(identifierValue: String, framework: DataTypeEnum, requestingUser: String): Boolean {
        return inMemoryDataRequestStore.values.any { dataRequest ->
            dataRequest.companyIdentifierValue == identifierValue &&
                dataRequest.dataType == framework &&
                dataRequest.userId == requestingUser
        }
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
            timestamp = Instant.now().toEpochMilli(),
            dataType = framework,
            companyIdentifierType = identifierType,
            companyIdentifierValue = identifierValue,
            companyIdOnDataland = companyId,
        )
    }

    private fun buildResponseMessageForBulkDataRequest(
        totalNumberOfRequestedCompanyIdentifiers: Int,
        numberOfRejectedCompanyIdentifiers: Int,
    ): String {
        return when (numberOfRejectedCompanyIdentifiers) {
            0 -> "$totalNumberOfRequestedCompanyIdentifiers data requests were created."
            else ->
                "$numberOfRejectedCompanyIdentifiers of your $totalNumberOfRequestedCompanyIdentifiers company identifiers were " +
                    "rejected because of a format that is not matching a valid LEI, ISIN or PermID."
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
}

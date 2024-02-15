package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.repositories.MessageRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestManagerUtils
import org.dataland.datalandemail.email.isEmailAddress
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of a request manager service for all operations concerning the processing of single data requests
 */
@Service("SingleDataRequestManager")
class SingleDataRequestManager(
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val companyGetter: CompanyGetter,
    @Autowired private val singleDataRequestEmailSender: SingleDataRequestEmailSender,
    @Autowired private val messageRepository: MessageRepository,
) {
    private val utils = DataRequestManagerUtils(dataRequestRepository, messageRepository, dataRequestLogger, companyGetter)
    val companyIdRegex = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\$")

    /**
     * Processes a single data request from a user
     * @param singleDataRequest info provided by a user in order to request a single dataset on Dataland
     * @return the stored data request object
     */
    @Transactional
    fun processSingleDataRequest(singleDataRequest: SingleDataRequest): List<StoredDataRequest> {
        if (DatalandAuthentication.fromContext() !is DatalandJwtAuthentication) {
            throw AuthenticationMethodNotSupportedException("You are not using JWT authentication.")
        }
        assertValidMessage(singleDataRequest)
        dataRequestLogger.logMessageForSingleDataRequestReceived()
        val storedDataRequests = mutableListOf<StoredDataRequest>()
        val (identifierTypeToStore, identifierValueToStore) = identifyIdentifierTypeAndTryGetDatalandCompanyId(
            singleDataRequest.companyIdentifier,
        )
        storeDataRequestsAndAddThemToListForEachReportingPeriodIfNotAlreadyExisting(
            storedDataRequests, singleDataRequest, identifierValueToStore, identifierTypeToStore,
        )
        singleDataRequestEmailSender.sendSingleDataRequestEmails(
            userAuthentication = DatalandAuthentication.fromContext() as DatalandJwtAuthentication,
            singleDataRequest = singleDataRequest,
            companyIdentifierType = identifierTypeToStore,
            companyIdentifierValue = identifierValueToStore,
        )
        return storedDataRequests
    }

    private fun assertValidMessage(singleDataRequest: SingleDataRequest) {
        val contacts = singleDataRequest.contacts
        if (!contacts.isNullOrEmpty() && contacts.any { !it.isEmailAddress() }) {
            throw InvalidInputApiException(
                "You must provide proper email addresses as contacts.",
                "You must provide proper email addresses as contacts.",
            )
        }
        if (contacts.isNullOrEmpty() && !singleDataRequest.message.isNullOrBlank()) {
            throw InvalidInputApiException(
                "Insufficient information to create message object.",
                "Without at least one proper email address being provided no message can be forwarded.",
            )
        }
    }

    private fun identifyIdentifierTypeAndTryGetDatalandCompanyId(
        companyIdentifier: String,
    ): Pair<DataRequestCompanyIdentifierType, String> {
        if (companyIdRegex.matches(companyIdentifier)) {
            checkIfCompanyIsValid(companyIdentifier)
            return Pair(DataRequestCompanyIdentifierType.DatalandCompanyId, companyIdentifier)
        }
        val matchedIdentifierType = utils.determineIdentifierTypeViaRegex(companyIdentifier)
        dataRequestLogger.logMessageForReceivingSingleDataRequest(companyIdentifier)
        if (matchedIdentifierType != null) {
            val datalandCompanyId = utils.getDatalandCompanyIdForIdentifierValue(
                companyIdentifier,
            )
            return Pair(
                datalandCompanyId?.let {
                    DataRequestCompanyIdentifierType.DatalandCompanyId
                } ?: matchedIdentifierType,
                datalandCompanyId ?: companyIdentifier,
            )
        }
        throw InvalidInputApiException(
            "The provided company identifier has an invalid format.",
            "The company identifier you provided does not match the patterns " +
                "of a valid LEI, ISIN, PermId or Dataland CompanyID.",
        )
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

    private fun storeDataRequestsAndAddThemToListForEachReportingPeriodIfNotAlreadyExisting(
        storedDataRequests: MutableList<StoredDataRequest>,
        singleDataRequest: SingleDataRequest,
        identifierValueToStore: String,
        identifierTypeToStore: DataRequestCompanyIdentifierType,
    ) {
        for (reportingPeriod in singleDataRequest.reportingPeriods.distinct()) {
            storedDataRequests.add(
                utils.storeDataRequestEntityIfNotExisting(
                    identifierValueToStore,
                    identifierTypeToStore,
                    singleDataRequest.frameworkName,
                    reportingPeriod,
                    singleDataRequest.contacts,
                    singleDataRequest.message,
                ).toStoredDataRequest(),
            )
        }
    }
}

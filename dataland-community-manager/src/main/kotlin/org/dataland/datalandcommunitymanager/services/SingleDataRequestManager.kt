package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandemail.email.validateIsEmailAddress
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
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val companyApi: CompanyDataControllerApi,
    @Autowired private val singleDataRequestEmailSender: SingleDataRequestEmailSender,
    @Autowired private val utils: DataRequestProcessingUtils,
) {
    val companyIdRegex = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\$")

    /**
     * Processes a single data request from a user
     * @param singleDataRequest info provided by a user in order to request a single dataset on Dataland
     * @return the stored data request object
     */
    @Transactional
    fun processSingleDataRequest(singleDataRequest: SingleDataRequest): List<StoredDataRequest> {
        utils.throwExceptionIfNotJwtAuth()
        dataRequestLogger.logMessageForSingleDataRequestReceived()
        validateContactsAndMessage(singleDataRequest.contacts, singleDataRequest.message)
        val (identifierTypeToStore, identifierValueToStore) = identifyIdentifierTypeAndTryGetDatalandCompanyId(
            singleDataRequest.companyIdentifier,
        )
        val storedDataRequestEntities =
            storeOneDataRequestPerReportingPeriod(singleDataRequest, identifierValueToStore, identifierTypeToStore)
        singleDataRequestEmailSender.sendSingleDataRequestEmails(
            userAuthentication = DatalandAuthentication.fromContext() as DatalandJwtAuthentication,
            singleDataRequest = singleDataRequest,
            companyIdentifierType = identifierTypeToStore,
            companyIdentifierValue = identifierValueToStore,
        )
        return storedDataRequestEntities.map { it.toStoredDataRequest() }
    }

    private fun validateContactsAndMessage(contacts: Set<String>?, message: String?) {
        contacts?.forEach { it.validateIsEmailAddress() }
        if (contacts.isNullOrEmpty() && !message.isNullOrBlank()) {
            throw InvalidInputApiException(
                "No recipients provided for the message",
                "You have provided a message, but no recipients. " +
                    "Without at least one valid email address being provided no message can be forwarded.",
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
        try {
            companyApi.getCompanyById(companyId)
        } catch (e: ClientException) {
            if (e.statusCode == HttpStatus.NOT_FOUND.value()) {
                throw ResourceNotFoundApiException(
                    "Company not found",
                    "Dataland-backend does not know the company ID $companyId",
                )
            }
        }
    }

    private fun storeOneDataRequestPerReportingPeriod(
        singleDataRequest: SingleDataRequest,
        identifierValueToStore: String,
        identifierTypeToStore: DataRequestCompanyIdentifierType,
    ): List<DataRequestEntity> {
        return singleDataRequest.reportingPeriods.map { reportingPeriod ->
            utils.storeDataRequestEntityIfNotExisting(
                identifierValueToStore,
                identifierTypeToStore,
                singleDataRequest.dataType,
                reportingPeriod,
                singleDataRequest.contacts.takeIf { !it.isNullOrEmpty() },
                singleDataRequest.message.takeIf { !it.isNullOrBlank() },
            )
        }
    }
}

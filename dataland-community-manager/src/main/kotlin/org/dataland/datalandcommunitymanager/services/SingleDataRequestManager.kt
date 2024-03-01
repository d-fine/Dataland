package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.utils.validateIsEmailAddress
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
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
    @Autowired private val dataRequestEmailMessageSender: DataRequestEmailMessageSender,
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
        dataRequestLogger.logMessageForReceivingSingleDataRequest(singleDataRequest.companyIdentifier)
        validateContactsAndMessage(singleDataRequest.contacts, singleDataRequest.message)

        dataRequestLogger.logMessageForReceivingSingleDataRequest(singleDataRequest.companyIdentifier)
        val datalandCompanyId = if (companyIdRegex.matches(singleDataRequest.companyIdentifier)) {
            checkIfCompanyIsValid(singleDataRequest.companyIdentifier)
            singleDataRequest.companyIdentifier
        } else {
            utils.getDatalandCompanyIdForIdentifierValue(
                singleDataRequest.companyIdentifier,
            )
        }
        if (datalandCompanyId == null) {
            throw InvalidInputApiException(
                "The specified company is unknown to Dataland",
                "The company with identifier: ${singleDataRequest.companyIdentifier} is unknown to Dataland",
            )
        }
        val storedDataRequests = storeDataRequestsAndAddThemToListForEachReportingPeriodIfNotAlreadyExisting(
            singleDataRequest, datalandCompanyId,
        )
        sendSingleDataRequestEmailMessages(
            userAuthentication = DatalandAuthentication.fromContext() as DatalandJwtAuthentication,
            singleDataRequest = singleDataRequest,
            datalandCompanyId,
        )
        return storedDataRequests
    }

    private fun sendSingleDataRequestEmailMessages(
        userAuthentication: DatalandJwtAuthentication,
        singleDataRequest: SingleDataRequest,
        datalandCompanyId: String,
    ) {
        if (singleDataRequest.reportingPeriods.isEmpty()) return
        if (
            singleDataRequest.contacts.isNullOrEmpty()
        ) {
            sendInternalEmail(
                userAuthentication = userAuthentication,
                singleDataRequest = singleDataRequest,
                datalandCompanyId = datalandCompanyId,
            )
            return
        }
        sendEmailToSpecifiedContacts(userAuthentication, singleDataRequest, datalandCompanyId)
    }

    private fun sendEmailToSpecifiedContacts(
        userAuthentication: DatalandJwtAuthentication,
        singleDataRequest: SingleDataRequest,
        datalandCompanyId: String,
    ) {
        singleDataRequest.contacts?.forEach { contactEmail ->
            dataRequestEmailMessageSender.buildSingleDataRequestExternalMessage(
                receiver = contactEmail,
                userAuthentication = userAuthentication,
                datalandCompanyId = datalandCompanyId,
                dataType = singleDataRequest.dataType,
                reportingPeriods = singleDataRequest.reportingPeriods,
                contactMessage = singleDataRequest.message,
            )
        }
    }

    private fun sendInternalEmail(
        userAuthentication: DatalandJwtAuthentication,
        datalandCompanyId: String,
        singleDataRequest: SingleDataRequest,
    ) {
        dataRequestEmailMessageSender.buildSingleDataRequestInternalMessage(
            userAuthentication = userAuthentication,
            datalandCompanyId,
            dataType = singleDataRequest.dataType,
            reportingPeriods = singleDataRequest.reportingPeriods,
        )
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

    private fun storeDataRequestsAndAddThemToListForEachReportingPeriodIfNotAlreadyExisting(
        singleDataRequest: SingleDataRequest,
        datalandCompanyId: String,
    ): List<StoredDataRequest> {
        return singleDataRequest.reportingPeriods.map { reportingPeriod ->
            utils.storeDataRequestEntityIfNotExisting(
                datalandCompanyId,
                singleDataRequest.dataType,
                reportingPeriod,
                singleDataRequest.contacts.takeIf { !it.isNullOrEmpty() },
                singleDataRequest.message.takeIf { !it.isNullOrBlank() },
            ).toStoredDataRequest()
        }
    }
}

package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestManagerUtils
import org.dataland.datalandcommunitymanager.utils.GetDataRequestsSearchFilter
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandemail.email.validateIsEmailAddress
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.slf4j.LoggerFactory
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
    val logger = LoggerFactory.getLogger(javaClass)
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
        dataRequestLogger.logMessageForReceivingSingleDataRequest(singleDataRequest.companyIdentifier)
        val storedDataRequests = mutableListOf<StoredDataRequest>()
        var datalandCompanyId: String?

        dataRequestLogger.logMessageForReceivingSingleDataRequest(singleDataRequest.companyIdentifier)
        if (companyIdRegex.matches(singleDataRequest.companyIdentifier)) {
            checkIfCompanyIsValid(singleDataRequest.companyIdentifier)
            datalandCompanyId = singleDataRequest.companyIdentifier
        } else {
            datalandCompanyId = utils.getDatalandCompanyIdForIdentifierValue(
                singleDataRequest.companyIdentifier,
            )
        }

        if (datalandCompanyId == null) {
            throw InvalidInputApiException(
                "The specified company is unknown to Dataland",
                "The company with identifier: \"${singleDataRequest.companyIdentifier}\" is unknown to Dataland",
            )
        } else {
            throwInvalidInputApiExceptionIfFinalMessageObjectNotMeaningful(singleDataRequest)
            storeDataRequestsAndAddThemToListForEachReportingPeriodIfNotAlreadyExisting(
                storedDataRequests, singleDataRequest, datalandCompanyId,
            )
            singleDataRequestEmailSender.sendSingleDataRequestEmails(
                userAuthentication = DatalandAuthentication.fromContext() as DatalandJwtAuthentication,
                singleDataRequest = singleDataRequest,
                datalandCompanyId,
            )
        }
        return storedDataRequests
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

    private fun throwInvalidInputApiExceptionIfFinalMessageObjectNotMeaningful(singleDataRequest: SingleDataRequest) {
        if (utils.isContactListTrivial(singleDataRequest.contactList) && !singleDataRequest.message.isNullOrBlank()) {
            throw InvalidInputApiException(
                "Insufficient information to create message object.",
                "Without at least one proper email address being provided no message can be forwarded.",
            )
        }
    }

    private fun storeDataRequestsAndAddThemToListForEachReportingPeriodIfNotAlreadyExisting(
        storedDataRequests: MutableList<StoredDataRequest>,
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

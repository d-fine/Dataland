package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequestResponse
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
    fun processSingleDataRequest(singleDataRequest: SingleDataRequest): SingleDataRequestResponse {
        utils.throwExceptionIfNotJwtAuth()

        if (singleDataRequest.reportingPeriods.isEmpty()) {
            throw InvalidInputApiException(
                "The list of reporting periods must not be empty.",
                "At least one reporting period must be provided. Without, no meaningful request can be created.",
            )
        }

        validateContactsAndMessage(singleDataRequest.contacts, singleDataRequest.message)

        dataRequestLogger.logMessageForReceivingSingleDataRequest(singleDataRequest.companyIdentifier)

        val datalandCompanyId = if (companyIdRegex.matches(singleDataRequest.companyIdentifier)) {
            checkIfCompanyIsValid(singleDataRequest.companyIdentifier)
            singleDataRequest.companyIdentifier
        } else {
            utils.getDatalandCompanyIdForIdentifierValue(singleDataRequest.companyIdentifier)
        }
        if (datalandCompanyId == null) {
            throw InvalidInputApiException(
                "The specified company is unknown to Dataland.",
                "The company with identifier: ${singleDataRequest.companyIdentifier} is unknown to Dataland.",
            )
        }

        val reportingPeriodsOfStoredDataRequests = mutableListOf<String>()
        val reportingPeriodsOfDuplicateDataRequests = mutableListOf<String>()
        singleDataRequest.reportingPeriods.forEach { reportingPeriod ->
            if (
                utils.existsDataRequestWithNonFinalStatus(
                    datalandCompanyId, singleDataRequest.dataType, reportingPeriod,
                )
            ) {
                reportingPeriodsOfDuplicateDataRequests.add(reportingPeriod)
            } else {
                utils.storeDataRequestEntityAsOpen(
                    datalandCompanyId,
                    singleDataRequest.dataType,
                    reportingPeriod,
                    singleDataRequest.contacts.takeIf { !it.isNullOrEmpty() },
                    singleDataRequest.message.takeIf { !it.isNullOrBlank() },
                )
                reportingPeriodsOfStoredDataRequests.add(reportingPeriod)
            }
        }

        singleDataRequestEmailSender.sendSingleDataRequestEmails(
            userAuthentication = DatalandAuthentication.fromContext() as DatalandJwtAuthentication,
            singleDataRequest = singleDataRequest,
            datalandCompanyId,
        )

        return buildResponseForSingleDataRequest(
            singleDataRequest,
            reportingPeriodsOfStoredDataRequests,
            reportingPeriodsOfDuplicateDataRequests,
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

    private fun buildResponseForSingleDataRequest(
        singleDataRequest: SingleDataRequest,
        reportingPeriodsOfStoredDataRequests: List<String>,
        reportingPeriodsOfDuplicateDataRequests: List<String>,
    ): SingleDataRequestResponse {
        return SingleDataRequestResponse(
            buildResponseMessageForSingleDataRequest(
                totalNumberOfReportingPeriods = singleDataRequest.reportingPeriods.size,
                numberOfReportingPeriodsCorrespondingToDuplicates = reportingPeriodsOfDuplicateDataRequests.size,
            ),
            reportingPeriodsOfStoredDataRequests,
            reportingPeriodsOfDuplicateDataRequests,
        )
    }

    private fun buildResponseMessageForSingleDataRequest(
        totalNumberOfReportingPeriods: Int,
        numberOfReportingPeriodsCorrespondingToDuplicates: Int,
    ): String {
        return if (totalNumberOfReportingPeriods == 1) {
            when (numberOfReportingPeriodsCorrespondingToDuplicates) {
                1 -> "Your data request was not stored, as it was already created by you before and exists on Dataland."
                else -> "Your data request was stored successfully."
            }
        } else {
            when (numberOfReportingPeriodsCorrespondingToDuplicates) {
                0 -> "For each of the $totalNumberOfReportingPeriods reporting periods a data request was stored."
                1 ->
                    "The request for one of your $totalNumberOfReportingPeriods reporting periods was not stored, as " +
                        "it was already created by you before and exists on Dataland."
                totalNumberOfReportingPeriods ->
                    "No data request was stored, as all reporting periods correspond to duplicate requests that were " +
                        "already created by you before and exist on Dataland."
                else ->
                    "The data requests for $numberOfReportingPeriodsCorrespondingToDuplicates of your " +
                        "$totalNumberOfReportingPeriods reporting periods were not stored, as they were already " +
                        "created by you before and exist on Dataland."
            }
        }
    }
}

package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.QuotaExceededException
import org.dataland.datalandbackendutils.utils.validateIsEmailAddress
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequestResponse
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.services.messaging.SingleDataRequestEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.CompanyIdValidator
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.ZoneId
import java.util.*

/**
 * Implementation of a request manager service for all operations concerning the processing of single data requests
 */
@Service("SingleDataRequestManager")
class SingleDataRequestManager
@Suppress("LongParameterList")
constructor(
    @Autowired
    private val dataRequestLogger: DataRequestLogger,
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val companyIdValidator: CompanyIdValidator,
    @Autowired private val singleDataRequestEmailMessageSender: SingleDataRequestEmailMessageSender,
    @Autowired private val dataRequestProcessingUtils: DataRequestProcessingUtils,
    @Autowired private val securityUtilsService: SecurityUtilsService,
    @Value("\${dataland.community-manager.max-number-of-data-requests-per-day-for-role-user}") val maxRequestsForUser:
    Int,
) {
    val companyIdRegex = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\$")

    /**
     * Processes a single data request from a user
     * @param singleDataRequest info provided by a user in order to request a single dataset on Dataland
     * @return the stored data request object
     */
    @Transactional
    fun processSingleDataRequest(singleDataRequest: SingleDataRequest): SingleDataRequestResponse {
        val companyId = findDatalandCompanyIdForCompanyIdentifier(singleDataRequest.companyIdentifier)
        val correlationId = UUID.randomUUID().toString()
        checkSingleDataRequest(singleDataRequest, companyId)
        dataRequestLogger.logMessageForReceivingSingleDataRequest(
            singleDataRequest.companyIdentifier, DatalandAuthentication.fromContext().userId, correlationId,
        )
        val reportingPeriodsOfStoredDataRequests = mutableListOf<String>()
        val reportingPeriodsOfDuplicateDataRequests = mutableListOf<String>()
        singleDataRequest.reportingPeriods.forEach { reportingPeriod ->
            if (dataRequestProcessingUtils.existsDataRequestWithNonFinalStatus(
                    companyId,
                    singleDataRequest.dataType,
                    reportingPeriod,
                )
            ) {
                reportingPeriodsOfDuplicateDataRequests.add(reportingPeriod)
            } else {
                dataRequestProcessingUtils.storeDataRequestEntityAsOpen(
                    companyId, singleDataRequest.dataType, reportingPeriod,
                    singleDataRequest.contacts.takeIf { !it.isNullOrEmpty() },
                    singleDataRequest.message.takeIf { !it.isNullOrBlank() },
                )
                reportingPeriodsOfStoredDataRequests.add(reportingPeriod)
            }
        }
        sendSingleDataRequestEmailMessage(
            DatalandAuthentication.fromContext() as DatalandJwtAuthentication, singleDataRequest,
            companyId, correlationId,
        )
        return buildResponseForSingleDataRequest(
            singleDataRequest, reportingPeriodsOfStoredDataRequests, reportingPeriodsOfDuplicateDataRequests,
        )
    }

    private fun checkSingleDataRequest(singleDataRequest: SingleDataRequest, companyId: String) {
        dataRequestProcessingUtils.throwExceptionIfNotJwtAuth()
        validateSingleDataRequestContent(singleDataRequest)
        performQuotaCheckForNonPremiumUser(singleDataRequest.reportingPeriods.size, companyId)
    }

    private fun performQuotaCheckForNonPremiumUser(numberOfReportingPeriods: Int, companyId: String) {
        val userInfo = DatalandAuthentication.fromContext()
        if (!userInfo.roles.contains(DatalandRealmRole.ROLE_PREMIUM_USER) &&
            !securityUtilsService.isUserMemberOfTheCompany(UUID.fromString(companyId))
        ) {
            val numberOfDataRequestsPerformedByUserFromTimestamp =
                dataRequestRepository.getNumberOfDataRequestsPerformedByUserFromTimestamp(
                    userInfo.userId, getEpochTimeStartOfDay(),
                )

            if (numberOfDataRequestsPerformedByUserFromTimestamp + numberOfReportingPeriods
                > maxRequestsForUser
            ) {
                throw QuotaExceededException(
                    "Quota has been reached.",
                    "The daily quota capacity has been reached.",
                )
            }
        }
    }

    private fun getEpochTimeStartOfDay(): Long {
        val instantNow = Instant.ofEpochMilli(System.currentTimeMillis())
        val zoneId = ZoneId.of("Europe/Berlin")
        val instantNowZoned = instantNow.atZone(zoneId)
        val startOfDay = instantNowZoned.toLocalDate().atStartOfDay(zoneId)
        val startOfDayTimestampMillis = startOfDay.toInstant().toEpochMilli()
        return startOfDayTimestampMillis
    }

    private fun validateSingleDataRequestContent(singleDataRequest: SingleDataRequest) {
        if (singleDataRequest.reportingPeriods.isEmpty()) {
            throw InvalidInputApiException(
                "The list of reporting periods must not be empty.",
                "At least one reporting period must be provided. Without, no meaningful request can be created.",
            )
        }

        singleDataRequest.contacts?.forEach { it.validateIsEmailAddress() }
        if (singleDataRequest.contacts.isNullOrEmpty() && !singleDataRequest.message.isNullOrBlank()) {
            throw InvalidInputApiException(
                "No recipients provided for the message",
                "You have provided a message, but no recipients. " +
                    "Without at least one valid email address being provided no message can be forwarded.",
            )
        }
    }

    private fun findDatalandCompanyIdForCompanyIdentifier(companyIdentifier: String): String {
        val datalandCompanyId = if (companyIdRegex.matches(companyIdentifier)) {
            companyIdValidator.checkIfCompanyIdIsValidAndReturnName(companyIdentifier)
            companyIdentifier
        } else {
            dataRequestProcessingUtils.getDatalandCompanyIdForIdentifierValue(companyIdentifier)
        }
        if (datalandCompanyId == null) {
            throw InvalidInputApiException(
                "The specified company is unknown to Dataland.",
                "The company with identifier: $companyIdentifier is unknown to Dataland.",
            )
        } else {
            return datalandCompanyId
        }
    }

    private fun sendSingleDataRequestEmailMessage(
        userAuthentication: DatalandJwtAuthentication,
        singleDataRequest: SingleDataRequest,
        datalandCompanyId: String,
        correlationId: String,
    ) {
        val messageInformation = SingleDataRequestEmailMessageSender.MessageInformation(
            userAuthentication,
            datalandCompanyId,
            singleDataRequest.dataType,
            singleDataRequest.reportingPeriods,
        )
        if (
            singleDataRequest.contacts.isNullOrEmpty()
        ) {
            singleDataRequestEmailMessageSender.sendSingleDataRequestInternalMessage(
                messageInformation,
                correlationId,
            )
            return
        }
        sendExternalEmailMessages(messageInformation, singleDataRequest, correlationId)
    }

    private fun sendExternalEmailMessages(
        messageInformation: SingleDataRequestEmailMessageSender.MessageInformation,
        singleDataRequest: SingleDataRequest,
        correlationId: String,
    ) {
        singleDataRequest.contacts?.forEach { contactEmail ->
            singleDataRequestEmailMessageSender.sendSingleDataRequestExternalMessage(
                messageInformation = messageInformation,
                receiver = contactEmail,
                contactMessage = singleDataRequest.message,
                correlationId = correlationId,
            )
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

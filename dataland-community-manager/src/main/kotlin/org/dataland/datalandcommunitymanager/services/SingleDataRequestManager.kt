package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.QuotaExceededException
import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequestResponse
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.services.messaging.AccessRequestEmailSender
import org.dataland.datalandcommunitymanager.services.messaging.SingleDataRequestEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.CompanyIdValidator
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.ReportingPeriodKeys
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
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val companyIdValidator: CompanyIdValidator,
    @Autowired private val singleDataRequestEmailMessageSender: SingleDataRequestEmailMessageSender,
    @Autowired private val utils: DataRequestProcessingUtils,
    @Autowired private val dataAccessManager: DataAccessManager,
    @Autowired private val accessRequestEmailSender: AccessRequestEmailSender,
    @Autowired private val securityUtilsService: SecurityUtilsService,
    @Autowired private val companyRolesManager: CompanyRolesManager,
    @Value("\${dataland.community-manager.max-number-of-data-requests-per-day-for-role-user}") val maxRequestsForUser:
    Int,
) {
    val companyIdRegex = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\$")

    /**
     * Data structure holding the process request information
     */
    data class PreprocessedRequest(
        val companyId: String,
        val userId: String,
        val dataType: DataTypeEnum,
        val contacts: Set<String>?,
        val message: String?,
        val correlationId: String,
    )

    /**
     * Processes a single data request from a user
     * @param singleDataRequest info provided by a user in order to request a single dataset on Dataland
     * @return the stored data request object
     */
    @Transactional
    fun processSingleDataRequest(singleDataRequest: SingleDataRequest): SingleDataRequestResponse {
        val preprocessedRequest = preprocessSingleDataRequest(singleDataRequest)

        dataRequestLogger.logMessageForReceivingSingleDataRequest(
            singleDataRequest.companyIdentifier, preprocessedRequest.userId, preprocessedRequest.correlationId,
        )

        val reportingPeriodsMap = mutableMapOf<String, MutableList<String>>()

        singleDataRequest.reportingPeriods.forEach { reportingPeriod ->
            val processedReportingPeriod = processReportingPeriod(
                reportingPeriod, preprocessedRequest,
            )
            processedReportingPeriod.forEach { (key, value) ->
                reportingPeriodsMap.getOrPut(key) { mutableListOf() }.add(value)
            }
        }

        sendSingleDataRequestEmailMessage(
            preprocessedRequest,
            reportingPeriodsMap[ReportingPeriodKeys.ReportingPeriodsOfStoredDataRequests],
        )
        sendDataAccessRequestEmailMessage(
            preprocessedRequest,
            reportingPeriodsMap[ReportingPeriodKeys.ReportingPeriodsOfDataAccessRequests],
        )

        return buildResponseForSingleDataRequest(
            singleDataRequest,
            reportingPeriodsMap[ReportingPeriodKeys.ReportingPeriodsOfStoredDataRequests]?.toList() ?: listOf(),
            reportingPeriodsMap[ReportingPeriodKeys.ReportingPeriodsOfDublicateDataRequests]?.toList() ?: listOf(),
            reportingPeriodsMap[ReportingPeriodKeys.ReportingPeriodsOfDataAccessRequests]?.toList() ?: listOf(),
        )
    }

    /**
     * This method preprocesses a singleDataProcess
     * @param singleDataRequest is the single data process which should be preprocessed
     * @return the processed single request
     */
    fun preprocessSingleDataRequest(singleDataRequest: SingleDataRequest): PreprocessedRequest {
        val companyId = findDatalandCompanyIdForCompanyIdentifier(singleDataRequest.companyIdentifier)

        utils.throwExceptionIfNotJwtAuth()
        validateSingleDataRequestContent(singleDataRequest)
        performQuotaCheckForNonPremiumUser(singleDataRequest.reportingPeriods.size, companyId)

        return PreprocessedRequest(
            companyId = companyId,
            userId = DatalandAuthentication.fromContext().userId,
            dataType = singleDataRequest.dataType,
            contacts = singleDataRequest.contacts.takeIf { !it.isNullOrEmpty() },
            message = singleDataRequest.message.takeIf { !it.isNullOrBlank() },
            correlationId = UUID.randomUUID().toString(),
        )
    }

    private fun processReportingPeriod(
        reportingPeriod: String,
        preprocessedRequest: PreprocessedRequest,
    ): Map<String, String> {
        return if (shouldCreateAccessRequestToPrivateDataset(
                dataType = preprocessedRequest.dataType, companyId = preprocessedRequest.companyId,
                reportingPeriod = reportingPeriod, userId = preprocessedRequest.userId,
            )
        ) {
            dataAccessManager.createAccessRequestToPrivateDataset(
                userId = preprocessedRequest.userId, companyId = preprocessedRequest.companyId,
                dataType = preprocessedRequest.dataType, reportingPeriod = reportingPeriod,
                contacts = preprocessedRequest.contacts, message = preprocessedRequest.message,
            )
            mutableMapOf(ReportingPeriodKeys.ReportingPeriodsOfDataAccessRequests to reportingPeriod)
        } else if (utils.existsDataRequestWithNonFinalStatus(
                companyId = preprocessedRequest.companyId, framework = preprocessedRequest.dataType,
                reportingPeriod = reportingPeriod,
            ) || dataAccessManager.existsAccessRequestWithNonPendingStatus(
                companyId = preprocessedRequest.companyId, framework = preprocessedRequest.dataType,
                reportingPeriod = reportingPeriod,
            )
        ) {
            mutableMapOf(ReportingPeriodKeys.ReportingPeriodsOfDublicateDataRequests to reportingPeriod)
        } else {
            utils.storeDataRequestEntityAsOpen(
                datalandCompanyId = preprocessedRequest.companyId, dataType = preprocessedRequest.dataType,
                reportingPeriod = reportingPeriod, contacts = preprocessedRequest.contacts,
                message = preprocessedRequest.message,
            )
            mutableMapOf(ReportingPeriodKeys.ReportingPeriodsOfStoredDataRequests to reportingPeriod)
        }
    }

    private fun shouldCreateAccessRequestToPrivateDataset(
        dataType: DataTypeEnum,
        companyId: String,
        reportingPeriod: String,
        userId: String,

    ): Boolean {
        val matchingDatasetExists = utils.matchingDatasetExists(
            companyId = companyId, reportingPeriod = reportingPeriod,
            dataType = dataType,
        )
        val hasAccessToPrivateDataset = dataAccessManager.hasAccessToPrivateDataset(
            companyId = companyId,
            reportingPeriod = reportingPeriod, dataType = dataType, userId = userId,
        )
        val accessRequestAlreadyInPendingStatus = dataAccessManager.existsAccessRequestWithNonPendingStatus(
            companyId = companyId, framework = dataType,
            reportingPeriod = reportingPeriod,
        )
        return(
            dataType == DataTypeEnum.vsme && matchingDatasetExists && !hasAccessToPrivateDataset &&
                !accessRequestAlreadyInPendingStatus
            )
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
        return startOfDay.toInstant().toEpochMilli()
    }

    private fun validateSingleDataRequestContent(singleDataRequest: SingleDataRequest) {
        if (singleDataRequest.reportingPeriods.isEmpty()) {
            throw InvalidInputApiException(
                "The list of reporting periods must not be empty.",
                "At least one reporting period must be provided. Without, no meaningful request can be created.",
            )
        }

        singleDataRequest.contacts?.forEach {
            MessageEntity.validateContact(it, companyRolesManager, singleDataRequest.companyIdentifier)
        }
        if (singleDataRequest.contacts.isNullOrEmpty() && !singleDataRequest.message.isNullOrBlank()) {
            throw InvalidInputApiException(
                "No recipients provided for the message",
                "You have provided a message, but no recipients. " +
                    "Without at least one valid email address being provided no message can be forwarded.",
            )
        }
    }

    private fun findDatalandCompanyIdForCompanyIdentifier(companyIdentifier: String): String {
        val datalandCompanyId: String? = if (companyIdRegex.matches(companyIdentifier)) {
            companyIdValidator.checkIfCompanyIdIsValidAndReturnName(companyIdentifier)
            companyIdentifier
        } else {
            utils.getDatalandCompanyIdAndNameForIdentifierValue(companyIdentifier)?.companyId
        }

        return datalandCompanyId ?: throw InvalidInputApiException(
            "The specified company is unknown to Dataland.",
            "The company with identifier: $companyIdentifier is unknown to Dataland.",
        )
    }

    private fun sendSingleDataRequestEmailMessage(
        preprocessedRequest: PreprocessedRequest,
        reportingPeriodsOfStoredDataRequests: List<String>?,
    ) {
        if (reportingPeriodsOfStoredDataRequests.isNullOrEmpty()) {
            return
        } else {
            val messageInformation = SingleDataRequestEmailMessageSender.MessageInformation(
                userAuthentication = DatalandAuthentication.fromContext() as DatalandJwtAuthentication,
                datalandCompanyId = preprocessedRequest.companyId,
                dataType = preprocessedRequest.dataType,
                reportingPeriods = reportingPeriodsOfStoredDataRequests.toSet(),
            )

            if (preprocessedRequest.contacts.isNullOrEmpty()) {
                singleDataRequestEmailMessageSender.sendSingleDataRequestInternalMessage(
                    messageInformation, preprocessedRequest.correlationId,
                )
            } else {
                singleDataRequestEmailMessageSender.sendSingleDataRequestExternalMessage(
                    messageInformation = messageInformation,
                    receiverSet = preprocessedRequest.contacts,
                    contactMessage = preprocessedRequest.message,
                    correlationId = preprocessedRequest.correlationId,
                )
            }
        }
    }

    private fun sendDataAccessRequestEmailMessage(
        preprocessedRequest: PreprocessedRequest,
        reportingPeriodsOfStoredAccessRequests: List<String>?,
    ) {
        if (reportingPeriodsOfStoredAccessRequests.isNullOrEmpty()) {
            return
        } else {
            accessRequestEmailSender.notifyCompanyOwnerAboutNewRequest(
                AccessRequestEmailSender.RequestEmailInformation(
                    preprocessedRequest.userId, preprocessedRequest.message,
                    preprocessedRequest.companyId, preprocessedRequest.dataType.toString(),
                    reportingPeriodsOfStoredAccessRequests.toSet(),
                    preprocessedRequest.contacts ?: setOf(),
                ),
                preprocessedRequest.correlationId,
            )
        }
    }

    private fun buildResponseForSingleDataRequest(
        singleDataRequest: SingleDataRequest,
        reportingPeriodsOfStoredDataRequests: List<String>,
        reportingPeriodsOfDuplicateDataRequests: List<String>,
        reportingPeriodOfStoredAccessRequests: List<String>,
    ): SingleDataRequestResponse {
        return SingleDataRequestResponse(
            buildResponseMessageForSingleDataRequest(
                totalNumberOfReportingPeriods = singleDataRequest.reportingPeriods.size,
                numberOfReportingPeriodsCorrespondingToDuplicates = reportingPeriodsOfDuplicateDataRequests.size,
            ),
            reportingPeriodsOfStoredDataRequests,
            reportingPeriodsOfDuplicateDataRequests,
            reportingPeriodOfStoredAccessRequests,
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

package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.QuotaExceededException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.services.CommonDataRequestProcessingUtils
import org.dataland.datalandbackendutils.utils.ReportingPeriodKeys
import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequestResponse
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.services.messaging.AccessRequestEmailBuilder
import org.dataland.datalandcommunitymanager.services.messaging.SingleDataRequestEmailMessageBuilder
import org.dataland.datalandcommunitymanager.utils.CommunityManagerDataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.readableFrameworkNameMapping
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.utils.KeycloakAdapterRequestProcessingUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Implementation of a request manager service for all operations concerning the processing of single data requests
 */
@Service("SingleDataRequestManager")
class SingleDataRequestManager
    @Suppress("LongParameterList")
    @Autowired
    constructor(
        private val dataRequestLogger: DataRequestLogger,
        private val dataRequestRepository: DataRequestRepository,
        private val singleDataRequestEmailMessageBuilder: SingleDataRequestEmailMessageBuilder,
        private val communityManagerDataRequestProcessingUtils: CommunityManagerDataRequestProcessingUtils,
        private val commonDataRequestProcessingUtils: CommonDataRequestProcessingUtils,
        private val keycloakAdapterRequestProcessingUtils: KeycloakAdapterRequestProcessingUtils,
        private val dataAccessManager: DataAccessManager,
        private val accessRequestEmailBuilder: AccessRequestEmailBuilder,
        private val securityUtilsService: SecurityUtilsService,
        private val companyRolesManager: CompanyRolesManager,
        @Value("\${dataland.community-manager.max-number-of-data-requests-per-day-for-role-user}") val maxRequestsForUser: Int,
    ) {
        /**
         * Data structure holding the process request information
         */
        data class PreprocessedRequest(
            val companyId: String,
            val userId: String,
            val dataType: DataTypeEnum,
            val notifyMeImmediately: Boolean,
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
        fun processSingleDataRequest(
            singleDataRequest: SingleDataRequest,
            userId: String? = null,
        ): SingleDataRequestResponse {
            val userIdToUse = userId ?: DatalandAuthentication.fromContext().userId
            val preprocessedRequest = preprocessSingleDataRequest(singleDataRequest, userIdToUse)

            dataRequestLogger.logMessageForReceivingSingleDataRequest(
                preprocessedRequest,
            )

            val reportingPeriodsMap = mutableMapOf<String, MutableList<String>>()

            singleDataRequest.reportingPeriods.forEach { reportingPeriod ->
                val processedReportingPeriod =
                    processReportingPeriod(
                        reportingPeriod, preprocessedRequest,
                    )
                processedReportingPeriod.forEach { (key, value) ->
                    reportingPeriodsMap.getOrPut(key) { mutableListOf() }.add(value)
                }
            }

            sendSingleDataRequestEmailMessage(
                preprocessedRequest,
                reportingPeriodsMap[ReportingPeriodKeys.REPORTING_PERIODS_OF_STORED_DATA_REQUESTS],
            )
            sendDataAccessRequestEmailMessage(
                preprocessedRequest,
                reportingPeriodsMap[ReportingPeriodKeys.REPORTING_PERIODS_OF_DATA_ACCESS_REQUESTS],
            )

            return buildResponseForSingleDataRequest(
                singleDataRequest,
                reportingPeriodsMap[ReportingPeriodKeys.REPORTING_PERIODS_OF_STORED_DATA_REQUESTS]?.toList() ?: listOf(),
                reportingPeriodsMap[ReportingPeriodKeys.REPORTING_PERIODS_OF_DUPLICATE_DATA_REQUESTS]?.toList() ?: listOf(),
                reportingPeriodsMap[ReportingPeriodKeys.REPORTING_PERIODS_OF_DATA_ACCESS_REQUESTS]?.toList() ?: listOf(),
            )
        }

        /**
         * This method preprocesses a singleDataProcess
         * @param singleDataRequest is the single data process which should be preprocessed
         * @return the processed single request
         */
        fun preprocessSingleDataRequest(
            singleDataRequest: SingleDataRequest,
            userIdToUse: String,
        ): PreprocessedRequest {
            keycloakAdapterRequestProcessingUtils.throwExceptionIfNotJwtAuth()

            val (acceptedIdentifiersToCompanyIdAndName, rejectedIdentifiers) =
                communityManagerDataRequestProcessingUtils.performIdentifierValidation(listOf(singleDataRequest.companyIdentifier))
            if (rejectedIdentifiers.isNotEmpty()) {
                throw ResourceNotFoundApiException(
                    "The company identifier is unknown.",
                    "No company is associated to the identifier ${rejectedIdentifiers.first()}.",
                )
            }
            val companyId = acceptedIdentifiersToCompanyIdAndName.getValue(singleDataRequest.companyIdentifier).companyId

            validateSingleDataRequestContent(singleDataRequest)
            performQuotaCheckForNonPremiumUser(
                userIdToUse,
                singleDataRequest.reportingPeriods.size,
                companyId,
            )

            return PreprocessedRequest(
                companyId = companyId,
                userId = userIdToUse,
                dataType = singleDataRequest.dataType,
                notifyMeImmediately = singleDataRequest.notifyMeImmediately,
                contacts = singleDataRequest.contacts.takeIf { !it.isNullOrEmpty() },
                message = singleDataRequest.message.takeIf { !it.isNullOrBlank() },
                correlationId = UUID.randomUUID().toString(),
            )
        }

        private fun processReportingPeriod(
            reportingPeriod: String,
            preprocessedRequest: PreprocessedRequest,
        ): Map<String, String> =
            if (shouldCreateAccessRequestToPrivateDataset(
                    dataType = preprocessedRequest.dataType, companyId = preprocessedRequest.companyId,
                    reportingPeriod = reportingPeriod, userId = preprocessedRequest.userId,
                )
            ) {
                dataAccessManager.createAccessRequestToPrivateDataset(
                    userId = preprocessedRequest.userId, companyId = preprocessedRequest.companyId,
                    dataType = preprocessedRequest.dataType, reportingPeriod = reportingPeriod,
                    contacts = preprocessedRequest.contacts, message = preprocessedRequest.message,
                )
                mutableMapOf(ReportingPeriodKeys.REPORTING_PERIODS_OF_DATA_ACCESS_REQUESTS to reportingPeriod)
            } else if (communityManagerDataRequestProcessingUtils.existsDataRequestWithNonFinalStatus(
                    companyId = preprocessedRequest.companyId, framework = preprocessedRequest.dataType,
                    reportingPeriod = reportingPeriod, userId = preprocessedRequest.userId,
                ) ||
                dataAccessManager.existsAccessRequestWithNonPendingStatus(
                    companyId = preprocessedRequest.companyId, framework = preprocessedRequest.dataType,
                    reportingPeriod = reportingPeriod, userId = preprocessedRequest.userId,
                )
            ) {
                mutableMapOf(ReportingPeriodKeys.REPORTING_PERIODS_OF_DUPLICATE_DATA_REQUESTS to reportingPeriod)
            } else {
                communityManagerDataRequestProcessingUtils.storeDataRequestEntityAsOpen(
                    userId = preprocessedRequest.userId,
                    datalandCompanyId = preprocessedRequest.companyId,
                    dataType = preprocessedRequest.dataType,
                    notifyMeImmediately = preprocessedRequest.notifyMeImmediately,
                    reportingPeriod = reportingPeriod,
                    contacts = preprocessedRequest.contacts,
                    message = preprocessedRequest.message,
                )
                mutableMapOf(ReportingPeriodKeys.REPORTING_PERIODS_OF_STORED_DATA_REQUESTS to reportingPeriod)
            }

        private fun shouldCreateAccessRequestToPrivateDataset(
            dataType: DataTypeEnum,
            companyId: String,
            reportingPeriod: String,
            userId: String,
        ): Boolean {
            val matchingDatasetExists =
                communityManagerDataRequestProcessingUtils.matchingDatasetExists(
                    companyId = companyId, reportingPeriod = reportingPeriod,
                    dataType = dataType,
                )
            val hasAccessToPrivateDataset =
                dataAccessManager.hasAccessToPrivateDataset(
                    companyId = companyId,
                    reportingPeriod = reportingPeriod, dataType = dataType, userId = userId,
                )
            val accessRequestAlreadyInPendingStatus =
                dataAccessManager.existsAccessRequestWithNonPendingStatus(
                    companyId = companyId, framework = dataType,
                    reportingPeriod = reportingPeriod, userId = userId,
                )
            return (
                dataType == DataTypeEnum.vsme &&
                    matchingDatasetExists &&
                    !hasAccessToPrivateDataset &&
                    !accessRequestAlreadyInPendingStatus
            )
        }

        private fun performQuotaCheckForNonPremiumUser(
            userId: String,
            numberOfReportingPeriods: Int,
            companyId: String,
        ) {
            if (!keycloakAdapterRequestProcessingUtils.userIsPremiumUser(userId) &&
                !securityUtilsService.isUserMemberOfTheCompany(UUID.fromString(companyId))
            ) {
                val numberOfDataRequestsPerformedByUserFromTimestamp =
                    dataRequestRepository.getNumberOfDataRequestsPerformedByUserFromTimestamp(
                        userId, commonDataRequestProcessingUtils.getEpochTimeStartOfDay(),
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

        private fun sendSingleDataRequestEmailMessage(
            preprocessedRequest: PreprocessedRequest,
            reportingPeriodsOfStoredDataRequests: List<String>?,
        ) {
            if (reportingPeriodsOfStoredDataRequests.isNullOrEmpty()) {
                return
            } else {
                val messageInformation =
                    SingleDataRequestEmailMessageBuilder.MessageInformation(
                        userAuthentication = DatalandAuthentication.fromContext() as DatalandJwtAuthentication,
                        datalandCompanyId = preprocessedRequest.companyId,
                        dataType = preprocessedRequest.dataType,
                        reportingPeriods = reportingPeriodsOfStoredDataRequests.toSet(),
                    )

                if (preprocessedRequest.contacts.isNullOrEmpty()) {
                    singleDataRequestEmailMessageBuilder.buildSingleDataRequestInternalMessageAndSendCEMessage(
                        messageInformation, preprocessedRequest.correlationId,
                    )
                } else {
                    singleDataRequestEmailMessageBuilder.buildSingleDataRequestExternalMessageAndSendCEMessage(
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
                val dataTypeDescription =
                    readableFrameworkNameMapping[preprocessedRequest.dataType] ?: preprocessedRequest.dataType.toString()
                accessRequestEmailBuilder.notifyCompanyOwnerAboutNewRequest(
                    AccessRequestEmailBuilder.RequestEmailInformation(
                        preprocessedRequest.userId, preprocessedRequest.message,
                        preprocessedRequest.companyId, dataTypeDescription,
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
        ): SingleDataRequestResponse =
            SingleDataRequestResponse(
                commonDataRequestProcessingUtils.buildResponseMessageForSingleDataRequest(
                    totalNumberOfReportingPeriods = singleDataRequest.reportingPeriods.size,
                    numberOfReportingPeriodsCorrespondingToDuplicates = reportingPeriodsOfDuplicateDataRequests.size,
                ),
                reportingPeriodsOfStoredDataRequests,
                reportingPeriodsOfDuplicateDataRequests,
                reportingPeriodOfStoredAccessRequests,
            )
    }

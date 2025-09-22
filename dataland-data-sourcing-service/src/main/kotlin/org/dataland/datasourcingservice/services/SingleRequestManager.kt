package org.dataland.datasourcingservice.services

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.QuotaExceededException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.services.CommonDataRequestProcessingUtils
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandbackendutils.utils.ReportingPeriodKeys
import org.dataland.datasourcingservice.model.request.PreprocessedRequest
import org.dataland.datasourcingservice.model.request.SingleRequest
import org.dataland.datasourcingservice.model.request.SingleRequestResponse
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.datasourcingservice.utils.DataSourcingServiceDataRequestProcessingUtils
import org.dataland.datasourcingservice.utils.RequestLogger
import org.dataland.datasourcingservice.utils.SecurityUtilsService
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.utils.KeycloakAdapterRequestProcessingUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * Service responsible for managing data requests in the sense of the data sourcing service.
 */
@Service("SingleRequestManager")
class SingleRequestManager
    @Autowired
    constructor(
        private val requestLogger: RequestLogger,
        private val commonDataRequestProcessingUtils: CommonDataRequestProcessingUtils,
        private val keycloakAdapterRequestProcessingUtils: KeycloakAdapterRequestProcessingUtils,
        private val dataSourcingServiceDataRequestProcessingUtils: DataSourcingServiceDataRequestProcessingUtils,
        private val requestRepository: RequestRepository,
        private val securityUtilsService: SecurityUtilsService,
        private val keycloakUserService: KeycloakUserService,
        @Value("\${dataland.data-sourcing-service.max-number-of-data-requests-per-day-for-role-user}")
        private val maxRequestsForUser: Int,
    ) {
        private fun validateSingleDataRequestContent(singleRequest: SingleRequest) {
            if (singleRequest.reportingPeriods.isEmpty()) {
                throw InvalidInputApiException(
                    "The list of reporting periods must not be empty.",
                    "At least one reporting period must be provided. Without, no meaningful request can be created.",
                )
            }
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
                    requestRepository.getNumberOfDataRequestsPerformedByUserFromTimestamp(
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

        private fun preprocessDataRequest(
            singleRequest: SingleRequest,
            userIdToUse: UUID,
        ): PreprocessedRequest {
            keycloakAdapterRequestProcessingUtils.throwExceptionIfNotJwtAuth()

            val (acceptedIdentifiersToCompanyIdAndName, rejectedIdentifiers) =
                dataSourcingServiceDataRequestProcessingUtils
                    .performIdentifierValidation(
                        listOf(singleRequest.companyIdentifier),
                    )
            if (rejectedIdentifiers.isNotEmpty()) {
                throw ResourceNotFoundApiException(
                    "The company identifier is unknown.",
                    "No company is associated to the identifier ${rejectedIdentifiers.first()}.",
                )
            }
            val companyId = acceptedIdentifiersToCompanyIdAndName.getValue(singleRequest.companyIdentifier).companyId

            validateSingleDataRequestContent(singleRequest)
            performQuotaCheckForNonPremiumUser(
                userIdToUse.toString(),
                singleRequest.reportingPeriods.size,
                companyId,
            )

            return PreprocessedRequest(
                companyId = companyId,
                userId = userIdToUse,
                dataType = singleRequest.dataType,
                notifyMeImmediately = singleRequest.notifyMeImmediately,
                correlationId = UUID.randomUUID().toString(),
            )
        }

        private fun processReportingPeriod(
            reportingPeriod: String,
            preprocessedRequest: PreprocessedRequest,
        ): Map<String, String> =
            if (dataSourcingServiceDataRequestProcessingUtils.existsDataRequestWithNonFinalState(
                    companyId = preprocessedRequest.companyId, framework = preprocessedRequest.dataType,
                    reportingPeriod = reportingPeriod, userId = preprocessedRequest.userId,
                )
            ) {
                mutableMapOf(ReportingPeriodKeys.REPORTING_PERIODS_OF_DUPLICATE_DATA_REQUESTS to reportingPeriod)
            } else {
                dataSourcingServiceDataRequestProcessingUtils.storeRequestEntityAsOpen(
                    userId = preprocessedRequest.userId,
                    companyId = preprocessedRequest.companyId,
                    dataType = preprocessedRequest.dataType,
                    notifyMeImmediately = preprocessedRequest.notifyMeImmediately,
                    reportingPeriod = reportingPeriod,
                )
                mutableMapOf(ReportingPeriodKeys.REPORTING_PERIODS_OF_STORED_DATA_REQUESTS to reportingPeriod)
            }

        private fun buildResponseForSingleDataRequest(
            dataRequest: SingleRequest,
            reportingPeriodsOfStoredDataRequests: List<String>,
            reportingPeriodsOfDuplicateDataRequests: List<String>,
        ): SingleRequestResponse =
            SingleRequestResponse(
                commonDataRequestProcessingUtils.buildResponseMessageForSingleDataRequest(
                    totalNumberOfReportingPeriods = dataRequest.reportingPeriods.size,
                    numberOfReportingPeriodsCorrespondingToDuplicates = reportingPeriodsOfDuplicateDataRequests.size,
                ),
                reportingPeriodsOfStoredDataRequests,
                reportingPeriodsOfDuplicateDataRequests,
            )

        @Transactional
        fun createRequest(
            singleRequest: SingleRequest,
            userId: UUID?,
        ): SingleRequestResponse {
            val userIdToUse = userId ?: UUID.fromString(DatalandAuthentication.fromContext().userId)

            val preprocessedRequest = preprocessDataRequest(singleRequest, userIdToUse)

            requestLogger.logMessageForReceivingSingleDataRequest(
                preprocessedRequest,
            )

            val reportingPeriodsMap = mutableMapOf<String, MutableList<String>>()

            singleRequest.reportingPeriods.forEach { reportingPeriod ->
                val processedReportingPeriod =
                    processReportingPeriod(
                        reportingPeriod, preprocessedRequest,
                    )
                processedReportingPeriod.forEach { (key, value) ->
                    reportingPeriodsMap.getOrPut(key) { mutableListOf() }.add(value)
                }
            }

            return buildResponseForSingleDataRequest(
                singleRequest,
                reportingPeriodsMap[ReportingPeriodKeys.REPORTING_PERIODS_OF_STORED_DATA_REQUESTS]?.toList() ?: listOf(),
                reportingPeriodsMap[ReportingPeriodKeys.REPORTING_PERIODS_OF_DUPLICATE_DATA_REQUESTS]?.toList() ?: listOf(),
            )
        }
    }

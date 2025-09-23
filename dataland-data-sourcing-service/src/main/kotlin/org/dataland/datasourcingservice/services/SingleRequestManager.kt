package org.dataland.datasourcingservice.services

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.QuotaExceededException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.utils.CommonDataRequestProcessingUtils
import org.dataland.datalandbackendutils.utils.ReportingPeriodKeys
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.exceptions.RequestNotFoundApiException
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.PreprocessedRequest
import org.dataland.datasourcingservice.model.request.SingleRequest
import org.dataland.datasourcingservice.model.request.SingleRequestResponse
import org.dataland.datasourcingservice.model.request.StoredRequest
import org.dataland.datasourcingservice.repositories.DataRevisionRepository
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
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
import kotlin.jvm.optionals.getOrNull

/**
 * Service responsible for managing data requests in the sense of the data sourcing service.
 */
@Suppress("LongParameterList")
@Service("SingleRequestManager")
class SingleRequestManager
    @Autowired
    constructor(
        private val requestLogger: RequestLogger,
        private val keycloakAdapterRequestProcessingUtils: KeycloakAdapterRequestProcessingUtils,
        private val dataSourcingServiceDataRequestProcessingUtils: DataSourcingServiceDataRequestProcessingUtils,
        private val requestRepository: RequestRepository,
        private val dataSourcingRepository: DataSourcingRepository,
        private val dataRevisionRepository: DataRevisionRepository,
        private val securityUtilsService: SecurityUtilsService,
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
                        userId, CommonDataRequestProcessingUtils.getEpochTimeStartOfDay(),
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
                correlationId = UUID.randomUUID().toString(),
            )
        }

        private fun processReportingPeriod(
            reportingPeriod: String,
            preprocessedRequest: PreprocessedRequest,
        ): Map<String, Pair<String, UUID>> {
            val existingDataRequestsWithNonFinalState =
                dataSourcingServiceDataRequestProcessingUtils.getExistingDataRequestsWithNonFinalState(
                    companyId = preprocessedRequest.companyId, framework = preprocessedRequest.dataType,
                    reportingPeriod = reportingPeriod, userId = preprocessedRequest.userId,
                )
            return if (existingDataRequestsWithNonFinalState.isNotEmpty()) {
                mutableMapOf(
                    ReportingPeriodKeys.REPORTING_PERIODS_OF_DUPLICATE_DATA_REQUESTS to
                        Pair(reportingPeriod, existingDataRequestsWithNonFinalState.first().id),
                )
            } else {
                val requestEntity =
                    dataSourcingServiceDataRequestProcessingUtils.storeRequestEntityAsOpen(
                        userId = preprocessedRequest.userId,
                        companyId = preprocessedRequest.companyId,
                        dataType = preprocessedRequest.dataType,
                        reportingPeriod = reportingPeriod,
                    )
                mutableMapOf(
                    ReportingPeriodKeys.REPORTING_PERIODS_OF_STORED_DATA_REQUESTS to
                        Pair(
                            reportingPeriod,
                            requestEntity.id,
                        ),
                )
            }
        }

        private fun buildResponseForSingleDataRequest(
            dataRequest: SingleRequest,
            reportingPeriodsAndIdsOfStoredDataRequests: List<Pair<String, UUID>>,
            reportingPeriodsAndNullIdsOfDuplicateDataRequests: List<Pair<String, UUID>>,
        ): SingleRequestResponse =
            SingleRequestResponse(
                CommonDataRequestProcessingUtils.buildResponseMessageForSingleDataRequest(
                    totalNumberOfReportingPeriods = dataRequest.reportingPeriods.size,
                    numberOfReportingPeriodsCorrespondingToDuplicates = reportingPeriodsAndNullIdsOfDuplicateDataRequests.size,
                ),
                reportingPeriodsAndIdsOfStoredDataRequests.map { it.first },
                reportingPeriodsAndIdsOfStoredDataRequests.map { it.second.toString() },
                reportingPeriodsAndNullIdsOfDuplicateDataRequests.map { it.first },
                reportingPeriodsAndNullIdsOfDuplicateDataRequests.map { it.second.toString() },
            )

        /**
         * Creates a new data request based on the provided SingleRequest object.
         * In case a request for the same company, data type, reporting period, and user already exists
         * with a non-final state (i.e., Open or Processing), it will not create a new request for that reporting period.
         * @param singleRequest The SingleRequest object containing the details of the data request.
         * @param userId The UUID of the user making the request. If null, it will be extracted from the security context.
         * @return A SingleRequestResponse object containing details about the created request.
         * @throws InvalidInputApiException If the input data is invalid.
         * @throws QuotaExceededException If the user has exceeded their quota for requests.
         * @throws ResourceNotFoundApiException If the specified company identifier does not exist.
         */
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

            val reportingPeriodsAndIdsMap = mutableMapOf<String, MutableList<Pair<String, UUID>>>()

            singleRequest.reportingPeriods.forEach { reportingPeriod ->
                val processedReportingPeriod =
                    processReportingPeriod(
                        reportingPeriod, preprocessedRequest,
                    )
                processedReportingPeriod.forEach { (key, value) ->
                    reportingPeriodsAndIdsMap.getOrPut(key) { mutableListOf() }.add(value)
                }
            }

            return buildResponseForSingleDataRequest(
                singleRequest,
                reportingPeriodsAndIdsMap[ReportingPeriodKeys.REPORTING_PERIODS_OF_STORED_DATA_REQUESTS]?.toList()
                    ?: listOf(),
                reportingPeriodsAndIdsMap[ReportingPeriodKeys.REPORTING_PERIODS_OF_DUPLICATE_DATA_REQUESTS]?.toList()
                    ?: listOf(),
            )
        }

        /**
         Retrieves a stored data request by its ID.
         * @param dataRequestId The UUID of the data request to retrieve.
         * @return The StoredRequest object corresponding to the given ID.
         * @throws RequestNotFoundApiException If no data request with the given ID exists.
         */
        @Transactional(readOnly = true)
        fun getRequest(dataRequestId: UUID): StoredRequest =
            requestRepository.findById(dataRequestId).getOrNull()?.toStoredDataRequest()
                ?: throw RequestNotFoundApiException(
                    dataRequestId,
                )

        /**
         * Updates the state of a data request identified by its ID.
         * If the state is changed from Open to Processing, it ensures that a corresponding DataSourcingEntity exists.
         * @param dataRequestId The UUID of the data request to update.
         * @param newRequestState The new state to set for the data request.
         * @return The updated StoredRequest object.
         * @throws RequestNotFoundApiException If no data request with the given ID exists.
         */
        @Transactional
        fun patchRequestState(
            dataRequestId: UUID,
            newRequestState: RequestState,
        ): StoredRequest {
            val requestEntity =
                requestRepository.findById(dataRequestId).getOrNull() ?: throw RequestNotFoundApiException(
                    dataRequestId,
                )
            val oldRequestState = requestEntity.state
            requestEntity.state = newRequestState

            if (oldRequestState != RequestState.Open || newRequestState != RequestState.Processing) {
                return requestEntity.toStoredDataRequest()
            }
            val companyId = requestEntity.companyId
            val dataType = requestEntity.dataType
            val reportingPeriod = requestEntity.reportingPeriod
            val dataSourcingEntity =
                dataSourcingRepository.findByCompanyIdAndDataTypeAndReportingPeriod(
                    companyId = companyId,
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                )
            if (dataSourcingEntity == null) {
                val newDataSourcingEntity =
                    DataSourcingEntity(
                        companyId = companyId,
                        reportingPeriod = reportingPeriod,
                        dataType = dataType,
                    )
                newDataSourcingEntity.associatedRequests.add(requestEntity)
                dataSourcingRepository.save(newDataSourcingEntity)
            } else {
                dataSourcingEntity.associatedRequests.add(requestEntity)
            }
            return requestEntity.toStoredDataRequest()
        }

        /**
         * Retrieves the history of revisions for a specific data request identified by its ID.
         * @param id The UUID string of the data request whose history is to be retrieved.
         * @return A list of StoredRequest objects representing the revision history of the specified data request.
         * @throws InvalidInputApiException If the provided ID is not a valid UUID format.
         */
        @Transactional(readOnly = true)
        fun retrieveRequestHistory(id: String): List<StoredRequest> {
            val uuid =
                try {
                    UUID.fromString(id)
                } catch (_: IllegalArgumentException) {
                    throw InvalidInputApiException(
                        "Invalid UUID format for id: $id",
                        message = "Invalid UUID format for id: $id, please provide a valid UUID string.",
                    )
                }
            return dataRevisionRepository
                .listDataRequestRevisionsById(uuid)
                .map { it.toStoredDataRequest() }
        }
    }

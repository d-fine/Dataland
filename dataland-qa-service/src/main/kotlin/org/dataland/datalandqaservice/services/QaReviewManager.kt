@file:Suppress("TooManyFunctions")

package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.dataSourcingService.openApiClient.api.DataSourcingControllerApi
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.QaBypass
import org.dataland.datalandbackendutils.utils.ValidationUtils.convertToUUID
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.QaReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.QaReviewUtils
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.QaSearchFilter
import org.dataland.datalandqaservice.repositories.QaReviewRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID
import org.dataland.dataSourcingService.openApiClient.model.BasicDataDimensions as DsBasicDataDimensions

/**
 * A service class for managing QA report meta-information
 */
@Suppress("LongParameterList")
@Service
class QaReviewManager
    @Autowired
    constructor(
        val qaReviewRepository: QaReviewRepository,
        val companyDataControllerApi: CompanyDataControllerApi,
        val metaDataControllerApi: MetaDataControllerApi,
        var cloudEventMessageHandler: CloudEventMessageHandler,
        var objectMapper: ObjectMapper,
        val datalandBackendAccessor: DatalandBackendAccessor,
        val dataPointQaReportManager: DataPointQaReportManager,
        val datasetJudgementService: DatasetJudgementService,
        val dataSourcingControllerApi: DataSourcingControllerApi,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        companion object {
            private const val NANOSECONDS_IN_A_MILLISECOND = 1_000_000
        }

        private data class ToQaReviewResponseTiming(
            var mappedCount: Int = 0,
            var getContainedDataPointsDurationNanos: Long = 0,
            var countQaReportsForDataPointIdsDurationNanos: Long = 0,
            var datasetJudgementLookupDurationNanos: Long = 0,
        )

        private data class QaReportLookupResult(
            val numberQaReports: Long,
            val getContainedDataPointsDurationNanos: Long,
            val countQaReportsForDataPointIdsDurationNanos: Long,
        )

        private inline fun <T> measureExecutionNanos(block: () -> T): Pair<T, Long> {
            val start = System.nanoTime()
            val result = block()
            return Pair(result, System.nanoTime() - start)
        }

        private fun logToQaReviewResponseAverages(
            context: String,
            timing: ToQaReviewResponseTiming,
        ) {
            if (timing.mappedCount == 0) {
                logger.info("No datasets mapped in toQaReviewResponse for $context.")
                return
            }
            val getContainedDataPointsDurationMs =
                timing.getContainedDataPointsDurationNanos.toDouble() / timing.mappedCount / NANOSECONDS_IN_A_MILLISECOND
            val countQaReportsForDataPointIdsDurationMs =
                timing.countQaReportsForDataPointIdsDurationNanos.toDouble() / timing.mappedCount / NANOSECONDS_IN_A_MILLISECOND
            val datasetJudgementLookupDurationMs =
                timing.datasetJudgementLookupDurationNanos.toDouble() / timing.mappedCount / NANOSECONDS_IN_A_MILLISECOND
            logger.info(
                "Average toQaReviewResponse call durations for {} over {} datasets: " +
                    "getContainedDataPoints={} ms, countQaReportsForDataPointIds={} ms, " +
                    "getDatasetJudgementsByDatasetId={} ms.",
                context,
                timing.mappedCount,
                getContainedDataPointsDurationMs,
                countQaReportsForDataPointIdsDurationMs,
                datasetJudgementLookupDurationMs,
            )
        }

        /**
         * Add a new qa review entry corresponding to a dataset event (upload, qa status change, etc.) to the qa review
         * history
         * @param dataId identifier of the dataset
         * @param bypassQa whether to bypass the qa process or not; if true, qa status of dataset is automatically set to
         * Accepted
         * @param correlationId
         */
        @Transactional
        fun addDatasetToQaReviewRepository(
            dataId: String,
            bypassQa: Boolean,
            correlationId: String,
        ) {
            logger.info("Received data with dataId $dataId and bypassQA $bypassQa on QA message queue (correlation Id: $correlationId)")
            val triggeringUserId = requireNotNull(metaDataControllerApi.getDataMetaInfo(dataId).uploaderUserId)
            val (qaStatus, comment) = QaBypass.getCommentAndStatusForBypass(bypassQa)

            handleQaChange(
                dataId = dataId,
                qaStatus = qaStatus,
                triggeringUserId = triggeringUserId,
                comment = comment,
                correlationId = correlationId,
            )
        }

        /**
         * The method returns a list of unreviewed datasets with corresponding information for the specified input params
         * @param dataTypes the datatype of the dataset
         * @param reportingPeriods the reportingPeriod of the dataset
         * @param companyName the company name connected to the dataset
         * @param chunkIndex the chunkIndex of the request
         * @param chunkSize the chunkSize of the request
         */
        @Transactional(readOnly = true)
        fun getInfoOnDatasets(
            dataTypes: Set<DataTypeEnum>?,
            reportingPeriods: Set<String>?,
            companyName: String?,
            qaStatus: QaStatus = QaStatus.Pending,
            chunkSize: Int,
            chunkIndex: Int,
        ): List<QaReviewResponse> {
            val userIsAdmin = DatalandAuthentication.fromContext().roles.contains(DatalandRealmRole.ROLE_ADMIN)
            val toQaReviewResponseTiming = ToQaReviewResponseTiming()
            val qaReviewResponses =
                qaReviewRepository
                    .getSortedAndFilteredQaReviewMetadataset(
                        QaSearchFilter(
                            dataTypes = dataTypes,
                            reportingPeriods = reportingPeriods,
                            companyIds = datalandBackendAccessor.getCompanyIdsForCompanyName(companyName),
                            companyName = companyName,
                            qaStatuses = setOf(qaStatus),
                        ),
                        resultOffset = chunkIndex * chunkSize,
                        resultLimit = chunkSize,
                    ).map { it.toQaReviewResponse(userIsAdmin, toQaReviewResponseTiming) }
            logToQaReviewResponseAverages("getInfoOnDatasets", toQaReviewResponseTiming)
            return qaReviewResponses
        }

        /**
         * The method returns a list of unreviewed datasets with corresponding information for the specified company name,
         * which are still pending review (qaStatus = Pending).
         */
        @Transactional(readOnly = true)
        fun getInfoOnPendingDatasets(companyName: String?): List<QaReviewResponse> {
            val userIsAdmin = DatalandAuthentication.fromContext().roles.contains(DatalandRealmRole.ROLE_ADMIN)
            logger.info("Retrieving information about pending datasets for companyName $companyName.")
            val toQaReviewResponseTiming = ToQaReviewResponseTiming()
            val qaReviewResponses =
                qaReviewRepository
                    .getPendingQaReviewMetadatasetsByCompany(
                        QaSearchFilter(
                            dataTypes = null,
                            reportingPeriods = null,
                            companyIds = datalandBackendAccessor.getCompanyIdsForCompanyName(companyName),
                            companyName = companyName,
                            qaStatuses = setOf(QaStatus.Pending),
                        ),
                    ).map { it.toQaReviewResponse(userIsAdmin, toQaReviewResponseTiming) }
            logger.info("Retrieved information about pending datasets for companyName $companyName.")
            logToQaReviewResponseAverages("getInfoOnPendingDatasets", toQaReviewResponseTiming)

            return addPrioritiesToResponse(qaReviewResponses)
        }

        /**
         * Adds data sourcing priorities to the given QA review responses if available.
         *
         * If the data sourcing service returns 404, priorities are assumed missing and set to null. Other errors are rethrown.
         * Since the service may return priorities in a different order, a map from dimensions to priority is used.
         *
         * @param qaReviewResponses list of QA review responses to enrich with priority
         * @return list of QA review responses with priorities added or null if unavailable
         */
        private fun addPrioritiesToResponse(qaReviewResponses: List<QaReviewResponse>): List<QaReviewResponse> {
            logger.info("Fetching data sourcing priorities to ${qaReviewResponses.size} QA review responses.")
            val dsDimensions =
                qaReviewResponses.map {
                    DsBasicDataDimensions(it.companyId, it.framework, it.reportingPeriod)
                }

            val prioritiesOfAssociatedDataSourcing =
                try {
                    dataSourcingControllerApi.getDataSourcingPriorities(dsDimensions)
                } catch (ex: ClientException) {
                    if ((ex.response as? ClientError<*>)?.statusCode == HttpStatus.NOT_FOUND.value()) null else throw ex
                }
            logger.info("Adding data sourcing priorities to ${qaReviewResponses.size} QA review responses.")
            val responsesWithPriorities = QaReviewUtils.assignPriorities(qaReviewResponses, prioritiesOfAssociatedDataSourcing)
            logger.info("Successfully added data sourcing priorities to ${qaReviewResponses.size} QA review responses.")
            return responsesWithPriorities
        }

        /**
         * This method returns the number of unreviewed datasets for a specific set of filters
         * @param dataTypes the set of datatypes for which should be filtered
         * @param reportingPeriods the set of reportingPeriods for which should be filtered
         * @param companyName the companyName for which should be filtered
         */
        @Transactional
        fun getNumberOfPendingDatasets(
            dataTypes: Set<DataTypeEnum>?,
            reportingPeriods: Set<String>?,
            companyName: String?,
        ): Int =
            qaReviewRepository.getNumberOfFilteredQaReviews(
                QaSearchFilter(
                    dataTypes = dataTypes, companyName = companyName, reportingPeriods = reportingPeriods,
                    companyIds = datalandBackendAccessor.getCompanyIdsForCompanyName(companyName), qaStatuses = setOf(QaStatus.Pending),
                ),
            )

        /**
         * Return the most recent Qa review entity for a particular data ID
         * @param dataId the data ID for which the information is retrieved
         */
        @Transactional
        fun getMostRecentQaReviewEntity(dataId: String): QaReviewEntity? = qaReviewRepository.findFirstByDataIdOrderByTimestampDesc(dataId)

        /**
         * Retrieves from database a QaReviewEntity by its dataId
         * @param dataId: dataID
         */
        @Transactional(readOnly = true)
        fun getQaReviewResponseByDataId(dataId: UUID): QaReviewResponse? {
            val userIsAdmin = DatalandAuthentication.fromContextOrNull()?.roles?.contains(DatalandRealmRole.ROLE_ADMIN)
            return getMostRecentQaReviewEntity(dataId.toString())?.toQaReviewResponse(userIsAdmin ?: false)
        }

        /**
         * Method called to update triggeringUserId for first Pending entry of a dataset.
         * This effectively updates the uploaderUserId of the dataset. Method is only triggered via messageQueue.
         * Due to Spring JPA, the object is saved when the transaction is committed without explicitly calling save().
         * @param dataId identifier of dataset
         * @param uploaderUserId the new uploaderUserId aka the triggeringUserId of the upload event
         * @param correlationId
         */
        @Transactional
        fun patchUploaderUserIdInQaReviewEntry(
            dataId: String,
            uploaderUserId: String,
            correlationId: String,
        ) {
            logger.info("Received message to patch uploaderUserId for dataset with dataId $dataId (correlationId: $correlationId).")
            val qaReviewEntity = qaReviewRepository.findFirstByDataIdOrderByTimestampAsc(dataId)

            requireNotNull(qaReviewEntity) { "QaReviewEntity must not be null." }

            logger.info(
                "Updating triggeringUserId for first qa review entry for dataset with dataId $dataId$. " +
                    "Old triggeringUserId was ${qaReviewEntity.triggeringUserId}, new triggeringUserId is $uploaderUserId " +
                    "(correlationId: $correlationId).",
            )

            qaReviewEntity.triggeringUserId = uploaderUserId
        }

        /**
         * Handles Qa changes by creating and saving QaReviewEntity, and sending messages.
         * @param dataId dataId of dataset of which to change qaStatus
         * @param qaStatus new qaStatus to be set
         * @param triggeringUserId keycloakId of user triggering QA Status change or upload event
         * @param correlationId the ID for the process triggering the change
         */
        @Transactional
        fun handleQaChange(
            dataId: String,
            qaStatus: QaStatus,
            triggeringUserId: String,
            comment: String?,
            correlationId: String,
        ) {
            val dataMetaInfo = metaDataControllerApi.getDataMetaInfo(dataId)
            val companyName = companyDataControllerApi.getCompanyById(dataMetaInfo.companyId).companyInformation.companyName

            logger.info("Assigning quality status ${qaStatus.name} to dataset with ID $dataId (correlationID: $correlationId)")

            val qaReviewEntity =
                QaReviewEntity(
                    dataId = dataId,
                    companyId = dataMetaInfo.companyId,
                    companyName = companyName,
                    framework = dataMetaInfo.dataType.value,
                    reportingPeriod = dataMetaInfo.reportingPeriod,
                    timestamp = Instant.now().toEpochMilli(),
                    qaStatus = qaStatus,
                    triggeringUserId = triggeringUserId,
                    comment = comment,
                )
            qaReviewRepository.save(qaReviewEntity)
            getAcceptedReviewMetadataSorted(
                qaReviewEntity.companyId,
                qaReviewEntity.framework,
                qaReviewEntity.reportingPeriod,
            ).also {
                this.sendQaStatusUpdateMessage(
                    qaReviewEntity = qaReviewEntity,
                    correlationId = correlationId,
                    isUpdate = it.size > 1,
                    newActiveDataId = it.firstOrNull()?.dataId,
                )
            }
        }

        /**
         * Checks if the QA service knows the dataId
         */
        @Transactional
        fun checkIfQaServiceKnowsDataId(dataId: String): Boolean = getMostRecentQaReviewEntity(dataId) != null

        /**
         * Asserts that the QA service knows the dataId
         */
        @Transactional
        fun assertQaServiceKnowsDataId(dataId: String) {
            if (!checkIfQaServiceKnowsDataId(dataId)) {
                throw ResourceNotFoundApiException(
                    "Data ID not known to QA service",
                    "Dataland does not know the data id $dataId",
                )
            }
        }

        /**
         * Send the information that the QA status was updated to the message queue
         * @param qaReviewEntity qaReviewEntity for which to send the QaStatusChangeMessage
         * @param correlationId the ID for the process triggering the change
         */
        fun sendQaStatusUpdateMessage(
            qaReviewEntity: QaReviewEntity,
            correlationId: String,
            isUpdate: Boolean,
            newActiveDataId: String?,
        ) {
            val qaStatusChangeMessage =
                QaStatusChangeMessage(
                    dataId = qaReviewEntity.dataId,
                    updatedQaStatus = qaReviewEntity.qaStatus,
                    currentlyActiveDataId = newActiveDataId,
                    basicDataDimensions =
                        BasicDataDimensions(
                            companyId = qaReviewEntity.companyId,
                            dataType = qaReviewEntity.framework,
                            reportingPeriod = qaReviewEntity.reportingPeriod,
                        ),
                    isUpdate = isUpdate,
                )

            logger.info("Send QA status update message for dataId ${qaStatusChangeMessage.dataId} to messageQueue.")
            val messageBody = objectMapper.writeValueAsString(qaStatusChangeMessage)

            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                messageBody, MessageType.QA_STATUS_UPDATED, correlationId, ExchangeName.QA_SERVICE_DATA_QUALITY_EVENTS,
                RoutingKeyNames.DATA,
            )
        }

        /**
         * Delete all QA Review information from repository
         * @param dataId All QA review information with dataId is deleted
         * @param correlationId correlation Id
         */
        @Transactional
        fun deleteAllByDataId(
            dataId: String,
            correlationId: String,
        ) {
            logger.info("Deleting all QA information associated with dataId $dataId (correlationId: $correlationId)")
            this.qaReviewRepository.deleteAllByDataId(dataId)
        }

        /**
         * Retrieves all QA review entities with status Accepted for a given ([companyId], [dataType], [reportingPeriod])
         * triple, sorted by timestamp in descending order.
         *
         * @param companyId the ID of the company
         * @param dataType the dataType of the dataset
         * @param reportingPeriod the reportingPeriod of the dataset
         * @return a list of accepted [QaReviewEntity] objects sorted by timestamp descending
         */
        fun getAcceptedReviewMetadataSorted(
            companyId: String,
            dataType: String,
            reportingPeriod: String,
        ): List<QaReviewEntity> {
            logger.info(
                "Retrieving accepted QA review entities sorted by timestamp for companyId $companyId, " +
                    "dataType $dataType, reportingPeriod $reportingPeriod.",
            )
            val searchFilter =
                QaSearchFilter(
                    dataTypes = DataTypeEnum.decode(dataType)?.let { setOf(it) },
                    companyIds = setOf(companyId),
                    reportingPeriods = setOf(reportingPeriod),
                    qaStatuses = setOf(QaStatus.Accepted),
                    companyName = null,
                )
            return qaReviewRepository
                .getSortedAndFilteredQaReviewMetadataset(searchFilter)
        }

        /**
         * Retrieve dataId of currently active dataset for some triple ([companyId], [dataType], [reportingPeriod])
         *
         * @param companyId the ID of the company
         * @param dataType the dataType of the dataset
         * @param reportingPeriod the reportingPeriod of the dataset
         * @return Returns the dataId of the active dataset, or an empty string if no active dataset can be found
         */
        fun getDataIdOfCurrentlyActiveDataset(
            companyId: String,
            dataType: String,
            reportingPeriod: String,
        ): String? = getAcceptedReviewMetadataSorted(companyId, dataType, reportingPeriod).firstOrNull()?.dataId

        /**
         * Returns the number of QA reports for all data points contained in the given dataId
         */
        private fun getNumberOfQaReportsForDataId(dataId: String): QaReportLookupResult =
            try {
                val (dataPointIdsByDimension, getContainedDataPointsDurationNanos) =
                    measureExecutionNanos { metaDataControllerApi.getContainedDataPoints(dataId) }
                val dataPointIds = dataPointIdsByDimension.values.toSet()
                val (numberQaReports, countQaReportsForDataPointIdsDurationNanos) =
                    measureExecutionNanos { dataPointQaReportManager.countQaReportsForDataPointIds(dataPointIds) }
                QaReportLookupResult(
                    numberQaReports = numberQaReports,
                    getContainedDataPointsDurationNanos = getContainedDataPointsDurationNanos,
                    countQaReportsForDataPointIdsDurationNanos = countQaReportsForDataPointIdsDurationNanos,
                )
            } catch (clientException: ClientException) {
                if (clientException.statusCode == HttpStatus.NOT_FOUND.value()) {
                    logger.warn("Could not find data points for dataset $dataId, returning 0 QA reports")
                    QaReportLookupResult(
                        numberQaReports = 0L,
                        getContainedDataPointsDurationNanos = 0L,
                        countQaReportsForDataPointIdsDurationNanos = 0L,
                    )
                } else {
                    throw clientException
                }
            }

        /**
         * Converts the QaReviewEntity into a QaReviewResponse which is used in a response for a GET request.
         * The QaReviewResponse can optionally hide the triggeringUserId by setting showTriggeringUserId to false.
         */
        private fun QaReviewEntity.toQaReviewResponse(
            showTriggeringUserId: Boolean = false,
            toQaReviewResponseTiming: ToQaReviewResponseTiming? = null,
        ): QaReviewResponse {
            val qaReportLookupResult = getNumberOfQaReportsForDataId(dataId)
            val (datasetJudgements, datasetJudgementLookupDurationNanosForDataset) =
                measureExecutionNanos { datasetJudgementService.getDatasetJudgementsByDatasetId(convertToUUID(dataId)) }
            toQaReviewResponseTiming?.apply {
                mappedCount++
                getContainedDataPointsDurationNanos += qaReportLookupResult.getContainedDataPointsDurationNanos
                countQaReportsForDataPointIdsDurationNanos += qaReportLookupResult.countQaReportsForDataPointIdsDurationNanos
                datasetJudgementLookupDurationNanos += datasetJudgementLookupDurationNanosForDataset
            }
            val latestDatasetJudgement = datasetJudgements.firstOrNull()
            return QaReviewResponse(
                dataId = this.dataId,
                companyId = this.companyId,
                companyName = this.companyName,
                framework = this.framework,
                reportingPeriod = this.reportingPeriod,
                timestamp = this.timestamp,
                qaStatus = this.qaStatus,
                qaJudgeUserId = latestDatasetJudgement?.qaJudgeUserId,
                qaJudgeUserName = latestDatasetJudgement?.qaJudgeUserName,
                datasetReviewId = latestDatasetJudgement?.dataSetJudgementId,
                numberQaReports = qaReportLookupResult.numberQaReports,
                comment = this.comment,
                triggeringUserId = if (showTriggeringUserId) this.triggeringUserId else null,
                priorityOfAssociatedDataSourcing = null,
            )
        }
    }

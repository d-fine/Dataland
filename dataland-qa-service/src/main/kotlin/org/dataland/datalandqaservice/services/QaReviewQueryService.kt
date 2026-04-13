package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import jakarta.persistence.PersistenceException
import org.dataland.dataSourcingService.openApiClient.api.DataSourcingControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.ValidationUtils.convertToUUID
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.QaReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetJudgementRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.QaReviewUtils
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.QaSearchFilter
import org.dataland.datalandqaservice.repositories.QaReviewRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import org.dataland.dataSourcingService.openApiClient.model.BasicDataDimensions as DsBasicDataDimensions

const val NS_IN_MS = 1_000_000

/**
 * Query-only service for dataset-level QA review metadata and projections.
 */
@Suppress("LongParameterList")
@Service
class QaReviewQueryService
    @Autowired
    constructor(
        private val qaReviewRepository: QaReviewRepository,
        private val datalandBackendAccessor: DatalandBackendAccessor,
        private val dataPointQaReportManager: DataPointQaReportManager,
        private val dataSourcingControllerApi: DataSourcingControllerApi,
        private val datasetJudgementRepository: DatasetJudgementRepository,
        private val metaDataControllerApi: MetaDataControllerApi,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        // Lightweight container used to avoid building full DatasetJudgementResponse
        // which accesses collections (and can trigger extra queries).
        private data class MinimalDatasetJudgement(
            val dataSetJudgementId: String,
            val qaJudgeUserId: String?,
            val qaJudgeUserName: String?,
        )

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
            return qaReviewRepository
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
                ).map { it.toQaReviewResponse(userIsAdmin) }
        }

        /**
         * The method returns a list of unreviewed datasets with corresponding information for the specified company name,
         * which are still pending review (qaStatus = Pending).
         */
        @Transactional(readOnly = true)
        fun getInfoOnPendingDatasets(
            companyName: String?,
        ): List<QaReviewResponse> {
            val getInfoOnPendingDatasetsStartNs = System.nanoTime()
            val userIsAdmin = DatalandAuthentication.fromContext().roles.contains(DatalandRealmRole.ROLE_ADMIN)

            // Add timing around fetching pending metadata
            val getPendingStartNs = System.nanoTime()
            val entities =
                qaReviewRepository
                    .getPendingQaReviewMetadatasetsByCompany(
                        QaSearchFilter(
                            dataTypes = null,
                            reportingPeriods = null,
                            companyIds = datalandBackendAccessor.getCompanyIdsForCompanyName(companyName),
                            companyName = companyName,
                            qaStatuses = setOf(QaStatus.Pending),
                        ),
                    )
            logger.info(
                "perf|getInfoOnPendingDatasets|getPendingQaReviewMetadatasetsByCompany|datasetCount={} elapsedMs={}",
                entities.size,
                (System.nanoTime() - getPendingStartNs) / NS_IN_MS,
            )

            val datasetIds = entities.map { it.dataId }.distinct()

            val getNumberOfQaReportsForDatasetIdsStartNs = System.nanoTime()
            val numberQaReportsByDatasetId = getNumberOfQaReportsForDatasetIds(datasetIds)
            logger.info(
                "perf|getInfoOnPendingDatasets|getNumberOfQaReportsForDatasetIds|datasetCount={} elapsedMs={}",
                datasetIds.size,
                (System.nanoTime() - getNumberOfQaReportsForDatasetIdsStartNs) / NS_IN_MS,
            )

            val datasetUUIDs = datasetIds.map { convertToUUID(it) }
            // Time the dataset judgement fetch
            val getJudgementsStartANs = System.nanoTime()
            // Use a fetch-join query to load dataPoints together and avoid N+1
            val judgementEntities =
                try {
                    datasetJudgementRepository.findAllByDatasetIdInWithDataPoints(datasetUUIDs)
                } catch (ex: PersistenceException) {
                    logger.warn("Could not use fetch-join query for dataset judgements, falling back to default. Error [{}]: {}", ex::class.simpleName, ex.message)
                    datasetJudgementRepository.findAllByDatasetIdIn(datasetUUIDs)
                } catch (ex: DataAccessException) {
                    logger.warn("Could not use fetch-join query for dataset judgements, falling back to default. Error [{}]: {}", ex::class.simpleName, ex.message)
                    datasetJudgementRepository.findAllByDatasetIdIn(datasetUUIDs)
                }

            logger.info(
                "perf|getInfoOnPendingDatasets|findAllByDatasetIdInA|datasetCount={} elapsedMs={}",
                datasetUUIDs.size,
                (System.nanoTime() - getJudgementsStartANs) / NS_IN_MS,
            )

            val getJudgementsStartBNs = System.nanoTime()

            val latestJudgementByDatasetId =
                judgementEntities
                    .groupBy { it.datasetId }
                    .mapValues { (_, judgements) ->
                        val j = judgements.first()
                        MinimalDatasetJudgement(
                            dataSetJudgementId = j.dataSetJudgementId.toString(),
                            qaJudgeUserId = j.qaJudgeUserId.toString(),
                            qaJudgeUserName = j.qaJudgeUserName,
                        )
                    }
            logger.info(
                "perf|getInfoOnPendingDatasets|findAllByDatasetIdInB|datasetCount={} judgementCount={} elapsedMs={}",
                datasetUUIDs.size,
                latestJudgementByDatasetId.size,
                (System.nanoTime() - getJudgementsStartBNs) / NS_IN_MS,
            )

            // Time the creation of QaReviewResponse objects (mapping)
            val mapResponsesStartNs = System.nanoTime()
            val qaReviewResponses =
                entities
                    .map {
                        it.toQaReviewResponseWithPrecomputedData(
                            showTriggeringUserId = userIsAdmin,
                            numberQaReports = numberQaReportsByDatasetId[it.dataId] ?: 0L,
                            latestJudgement = latestJudgementByDatasetId[convertToUUID(it.dataId)],
                        )
                    }
            logger.info(
                "perf|getInfoOnPendingDatasets|mapQaReviewResponses|datasetCount={} responseCount={} elapsedMs={}",
                datasetIds.size,
                qaReviewResponses.size,
                (System.nanoTime() - mapResponsesStartNs) / NS_IN_MS,
            )
            logger.info(
                "perf|getInfoOnPendingDatasets|datasetCount={} responseCount={} elapsedMs={}",
                datasetIds.size,
                qaReviewResponses.size,
                (System.nanoTime() - getInfoOnPendingDatasetsStartNs) / NS_IN_MS,
            )
            // Time adding priorities (calls external data sourcing service)
            val addPrioritiesStartNs = System.nanoTime()
            val qaReviewResponsesWithPriorities = addPrioritiesToResponse(qaReviewResponses)
            logger.info(
                "perf|getInfoOnPendingDatasets|addPrioritiesToResponse|datasetCount={} responseCount={} elapsedMs={}",
                datasetIds.size,
                qaReviewResponsesWithPriorities.size,
                (System.nanoTime() - addPrioritiesStartNs) / NS_IN_MS,
            )
            return qaReviewResponsesWithPriorities
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
            val dsDimensions =
                qaReviewResponses.map {
                    DsBasicDataDimensions(it.companyId, it.framework, it.reportingPeriod)
                }

            // Time the external data sourcing call
            val dataSourcingStartNs = System.nanoTime()
            val prioritiesOfAssociatedDataSourcing =
                try {
                    val res = dataSourcingControllerApi.getDataSourcingPriorities(dsDimensions)
                    logger.info(
                        "perf|addPrioritiesToResponse|getDataSourcingPriorities|dsCount={} notFound=false elapsedMs={}",
                        dsDimensions.size,
                        (System.nanoTime() - dataSourcingStartNs) / NS_IN_MS,
                    )
                    res
                } catch (ex: ClientException) {
                    val elapsed = (System.nanoTime() - dataSourcingStartNs) / NS_IN_MS
                    if ((ex.response as? ClientError<*>)?.statusCode == HttpStatus.NOT_FOUND.value()) {
                        logger.info(
                            "perf|addPrioritiesToResponse|getDataSourcingPriorities|dsCount={} notFound=true elapsedMs={}",
                            dsDimensions.size,
                            elapsed,
                        )
                        null
                    } else {
                        logger.info(
                            "perf|addPrioritiesToResponse|getDataSourcingPriorities|dsCount={} error=true elapsedMs={}",
                            dsDimensions.size,
                            elapsed,
                        )
                        throw ex
                    }
                }

            return QaReviewUtils.assignPriorities(qaReviewResponses, prioritiesOfAssociatedDataSourcing)
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
                    dataTypes = dataTypes,
                    companyName = companyName,
                    reportingPeriods = reportingPeriods,
                    companyIds = datalandBackendAccessor.getCompanyIdsForCompanyName(companyName),
                    qaStatuses = setOf(QaStatus.Pending),
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
         * Returns the number of QA reports for all data points contained in the given datasetId
         */
        private fun getNumberOfQaReportsForDatasetId(datasetId: String): Long =
            try {
                val dataPointIds = metaDataControllerApi.getContainedDataPoints(datasetId).values.toSet()
                dataPointQaReportManager.countQaReportsForDataPointIds(dataPointIds)
            } catch (clientException: ClientException) {
                if (clientException.statusCode == HttpStatus.NOT_FOUND.value()) {
                    logger.warn("Could not find data points for dataset $datasetId, returning 0 QA reports")
                    0L
                } else {
                    throw clientException
                }
            }

        /**
         * Returns a map from datasetId to the number of QA reports for all data points contained in that dataset.
         * Fetches metadata for all datasetIds in bulk (one call per datasetId to the metadata API) and counts
         * QA reports for all collected data point IDs in a single DB query.
         */
        private fun getNumberOfQaReportsForDatasetIds(datasetIds: List<String>): Map<String, Long> {
            val getNumberOfQaReportsForDatasetIdsStartNs = System.nanoTime()
            val dataPointIdsByDatasetId = mutableMapOf<String, Set<String>>()
            for (datasetId in datasetIds) {
                val getContainedDataPointsStartNs = System.nanoTime()
                try {
                    val containedDataPoints = metaDataControllerApi.getContainedDataPoints(datasetId).values.toSet()
                    dataPointIdsByDatasetId[datasetId] = containedDataPoints
                    logger.info(
                        "perf|getInfoOnPendingDatasets|getContainedDataPoints|datasetId={} dataPointCount={} elapsedMs={}",
                        datasetId,
                        containedDataPoints.size,
                        (System.nanoTime() - getContainedDataPointsStartNs) / NS_IN_MS,
                    )
                } catch (clientException: ClientException) {
                    if (clientException.statusCode == HttpStatus.NOT_FOUND.value()) {
                        logger.warn("Could not find data points for dataset $datasetId, returning 0 QA reports")
                        logger.info(
                            "perf|getInfoOnPendingDatasets|getContainedDataPoints|datasetId={} dataPointCount=0 notFound=true elapsedMs={}",
                            datasetId,
                            (System.nanoTime() - getContainedDataPointsStartNs) / NS_IN_MS,
                        )
                        dataPointIdsByDatasetId[datasetId] = emptySet()
                    } else {
                        throw clientException
                    }
                }
            }
            val allDataPointIds = dataPointIdsByDatasetId.values.flatten().toSet()
            val countQaReportsForDataPointIdsBulkStartNs = System.nanoTime()
            val totalCountByDataPointId =
                if (allDataPointIds.isEmpty()) {
                    emptyMap()
                } else {
                    dataPointQaReportManager.countQaReportsForDataPointIdsBulk(allDataPointIds)
                }
            logger.info(
                "perf|getInfoOnPendingDatasets|countQaReportsForDataPointIdsBulk|dataPointCount={} elapsedMs={}",
                allDataPointIds.size,
                (System.nanoTime() - countQaReportsForDataPointIdsBulkStartNs) / NS_IN_MS,
            )
            val qaReportsByDatasetId =
                dataPointIdsByDatasetId.mapValues { (_, dpIds) ->
                    dpIds.sumOf { totalCountByDataPointId[it] ?: 0L }
                }
            logger.info(
                "perf|getInfoOnPendingDatasets|getNumberOfQaReportsForDatasetIds|datasetCount={} uniqueDataPointCount={} elapsedMs={}",
                datasetIds.size,
                allDataPointIds.size,
                (System.nanoTime() - getNumberOfQaReportsForDatasetIdsStartNs) / NS_IN_MS,
            )
            return qaReportsByDatasetId
        }

        /**
         * Converts the QaReviewEntity into a QaReviewResponse using pre-fetched data to avoid per-item I/O.
         */
        private fun QaReviewEntity.toQaReviewResponseWithPrecomputedData(
            showTriggeringUserId: Boolean = false,
            numberQaReports: Long,
            latestJudgement: MinimalDatasetJudgement?,
        ): QaReviewResponse =
            QaReviewResponse(
                dataId = this.dataId,
                companyId = this.companyId,
                companyName = this.companyName,
                framework = this.framework,
                reportingPeriod = this.reportingPeriod,
                timestamp = this.timestamp,
                qaStatus = this.qaStatus,
                qaJudgeUserId = latestJudgement?.qaJudgeUserId,
                qaJudgeUserName = latestJudgement?.qaJudgeUserName,
                datasetReviewId = latestJudgement?.dataSetJudgementId,
                numberQaReports = numberQaReports,
                comment = this.comment,
                triggeringUserId = if (showTriggeringUserId) this.triggeringUserId else null,
                priorityOfAssociatedDataSourcing = null,
            )

        /**
         * Converts the QaReviewEntity into a QaReviewResponse which is used in a response for a GET request.
         * The QaReviewResponse can optionally hide the triggeringUserId by setting showTriggeringUserId to false.
         */
        private fun QaReviewEntity.toQaReviewResponse(showTriggeringUserId: Boolean = false): QaReviewResponse {
            val numberQaReports = getNumberOfQaReportsForDatasetId(dataId)
            val datasetJudgements =
                datasetJudgementRepository.findAllByDatasetId(convertToUUID(dataId)).map {
                    it.toDatasetJudgementResponse()
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
                numberQaReports = numberQaReports,
                comment = this.comment,
                triggeringUserId = if (showTriggeringUserId) this.triggeringUserId else null,
                priorityOfAssociatedDataSourcing = null,
            )
        }
    }

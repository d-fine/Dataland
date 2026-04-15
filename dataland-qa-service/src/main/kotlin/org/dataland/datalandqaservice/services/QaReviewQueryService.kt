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

        // --- Internal DTOs -------------------------------------------------------

        private data class MinimalDatasetJudgement(
            val dataSetJudgementId: String,
            val qaJudgeUserId: String,
            val qaJudgeUserName: String,
        )

        // --- Public query APIs: overview endpoints --------------------------------

        /**
         * Returns a paged list of datasets (for the given filters) with QA metadata.
         * Used for the “Datasets” tab.
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

            val entities =
                qaReviewRepository.getSortedAndFilteredQaReviewMetadataset(
                    QaSearchFilter(
                        dataTypes = dataTypes,
                        reportingPeriods = reportingPeriods,
                        companyIds = datalandBackendAccessor.getCompanyIdsForCompanyName(companyName),
                        companyName = companyName,
                        qaStatuses = setOf(qaStatus),
                    ),
                    resultOffset = chunkIndex * chunkSize,
                    resultLimit = chunkSize,
                )

            return buildResponsesWithAggregates(entities, userIsAdmin)
        }

        /**
         * Returns a list of pending datasets (qaStatus = Pending) for the given company.
         * Used for the QA “queue” view.
         */
        @Transactional(readOnly = true)
        fun getInfoOnPendingDatasets(companyName: String?): List<QaReviewResponse> {
            val userIsAdmin = DatalandAuthentication.fromContext().roles.contains(DatalandRealmRole.ROLE_ADMIN)

            val entities = fetchPendingEntities(companyName)
            val enriched = buildResponsesWithAggregates(entities, userIsAdmin)

            return addPrioritiesToResponse(enriched)
        }

        /**
         * Returns the number of unreviewed datasets for the given filters.
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

        // --- Public query APIs: single dataset / accepted metadata ---------------

        /**
         * Returns the most recent QA review entity for the given dataId, or null.
         */
        @Transactional
        fun getMostRecentQaReviewEntity(dataId: String): QaReviewEntity? = qaReviewRepository.findFirstByDataIdOrderByTimestampDesc(dataId)

        /**
         * Retrieves a QA review response by dataId (single dataset view).
         */
        @Transactional(readOnly = true)
        fun getQaReviewResponseByDataId(dataId: UUID): QaReviewResponse? {
            val userIsAdmin = DatalandAuthentication.fromContextOrNull()?.roles?.contains(DatalandRealmRole.ROLE_ADMIN)
            return getMostRecentQaReviewEntity(dataId.toString())?.toQaReviewResponse(userIsAdmin ?: false)
        }

        /**
         * Returns true if any QA review is known for the given dataId.
         */
        @Transactional
        fun checkIfQaServiceKnowsDataId(dataId: String): Boolean = getMostRecentQaReviewEntity(dataId) != null

        /**
         * Asserts that at least one QA review is known for the given dataId.
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
         * Retrieves all QA review entities with status Accepted for a given triple
         * ([companyId], [dataType], [reportingPeriod]), sorted by timestamp descending.
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
            return qaReviewRepository.getSortedAndFilteredQaReviewMetadataset(searchFilter)
        }

        /**
         * Returns the dataId of the currently active dataset for ([companyId], [dataType], [reportingPeriod]),
         * or null if none exists.
         */
        fun getDataIdOfCurrentlyActiveDataset(
            companyId: String,
            dataType: String,
            reportingPeriod: String,
        ): String? = getAcceptedReviewMetadataSorted(companyId, dataType, reportingPeriod).firstOrNull()?.dataId

        // --- Internal helpers: fetching entities & priorities --------------------

        private fun fetchPendingEntities(companyName: String?): List<QaReviewEntity> {
            val filter =
                QaSearchFilter(
                    dataTypes = null,
                    reportingPeriods = null,
                    companyIds = datalandBackendAccessor.getCompanyIdsForCompanyName(companyName),
                    companyName = companyName,
                    qaStatuses = setOf(QaStatus.Pending),
                )

            return qaReviewRepository.getPendingQaReviewMetadatasetsByCompany(filter)
        }

        /**
         * Adds data sourcing priorities to the given QA review responses if available.
         *
         * If the data sourcing service returns 404, priorities are assumed missing and set to null.
         * Other errors are rethrown.
         */
        private fun addPrioritiesToResponse(qaReviewResponses: List<QaReviewResponse>): List<QaReviewResponse> {
            val dsDimensions =
                qaReviewResponses.map {
                    DsBasicDataDimensions(it.companyId, it.framework, it.reportingPeriod)
                }

            val prioritiesOfAssociatedDataSourcing =
                try {
                    dataSourcingControllerApi.getDataSourcingPriorities(dsDimensions)
                } catch (ex: ClientException) {
                    if ((ex.response as? ClientError<*>)?.statusCode == HttpStatus.NOT_FOUND.value()) {
                        null
                    } else {
                        throw ex
                    }
                }

            return QaReviewUtils.assignPriorities(qaReviewResponses, prioritiesOfAssociatedDataSourcing)
        }

        private fun fallbackToNonFetch(
            datasetUUIDs: Collection<UUID>,
            ex: Throwable,
        ) = run {
            logger.warn(
                "Could not use fetch-join query for dataset judgements, falling back to default. Error [{}]: {}",
                ex::class.simpleName,
                ex.message,
            )
            datasetJudgementRepository.findAllByDatasetIdIn(datasetUUIDs)
        }

        // --- Internal helpers: QA report counting --------------------------------

        /**
         * Returns the number of QA reports for all data points in a single dataset.
         * Used only in the single-dataset path (getQaReviewResponseByDataId).
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
         * Returns a map from datasetId to the number of QA reports for all data points in that dataset,
         * using a single bulk query for all data point IDs.
         */
        private fun getNumberOfQaReportsForDatasetIds(datasetIds: List<String>): Map<String, Long> {
            val dataPointIdsByDatasetId = mutableMapOf<String, Set<String>>()
            for (datasetId in datasetIds) {
                try {
                    val containedDataPoints = metaDataControllerApi.getContainedDataPoints(datasetId).values.toSet()
                    dataPointIdsByDatasetId[datasetId] = containedDataPoints
                } catch (clientException: ClientException) {
                    if (clientException.statusCode == HttpStatus.NOT_FOUND.value()) {
                        logger.warn("Could not find data points for dataset $datasetId, returning 0 QA reports")
                        dataPointIdsByDatasetId[datasetId] = emptySet()
                    } else {
                        throw clientException
                    }
                }
            }

            val allDataPointIds = dataPointIdsByDatasetId.values.flatten().toSet()
            val totalCountByDataPointId =
                if (allDataPointIds.isEmpty()) {
                    emptyMap()
                } else {
                    dataPointQaReportManager.countQaReportsForDataPointIdsBulk(allDataPointIds)
                }

            return dataPointIdsByDatasetId.mapValues { (_, dpIds) ->
                dpIds.sumOf { totalCountByDataPointId[it] ?: 0L }
            }
        }

        // --- Internal helpers: mapping ------------------------------------------

        /**
         * Builds enriched QA review responses for a list of QA review metadata entities.
         *
         * For the given [entities], this method:
         *  - Collects all distinct dataset IDs.
         *  - Fetches the contained data points for each dataset and counts active QA reports
         *    in bulk via [DataPointQaReportManager.countQaReportsForDataPointIdsBulk].
         *  - Loads all DatasetJudgementEntity rows for those datasets in a single query
         *    (using a fetch-join if possible, or falling back to a non-fetch query).
         *  - Maps at most one [MinimalDatasetJudgement] per dataset (enforcing the
         *    “one judgement per dataset” invariant).
         *  - Builds [QaReviewResponse] objects using the precomputed QA report counts and
         *    optional judgement information.
         *
         * This centralizes the performance-sensitive aggregation logic and is used by both
         * [getInfoOnDatasets] and [getInfoOnPendingDatasets] to avoid N+1 queries.
         *
         * If a dataset has no judgement yet, the corresponding judge fields in the response
         * ([QaReviewResponse.qaJudgeUserId], [QaReviewResponse.qaJudgeUserName],
         * [QaReviewResponse.datasetReviewId]) will be null.
         *
         * @param entities The list of [QaReviewEntity] rows to enrich.
         * @param userIsAdmin Whether the current user has the admin role. Controls whether
         *        the [QaReviewResponse.triggeringUserId] is included or hidden.
         * @return A list of [QaReviewResponse] in the same order as [entities].
         */
        private fun buildResponsesWithAggregates(
            entities: List<QaReviewEntity>,
            userIsAdmin: Boolean,
        ): List<QaReviewResponse> {
            val datasetIds = entities.map { it.dataId }.distinct()
            val numberQaReportsByDatasetId = getNumberOfQaReportsForDatasetIds(datasetIds)
            val datasetUUIDs = datasetIds.map { convertToUUID(it) }

            val judgementEntities =
                try {
                    datasetJudgementRepository.findAllWithDataPointsByDatasetIdIn(datasetUUIDs)
                } catch (ex: PersistenceException) {
                    fallbackToNonFetch(datasetUUIDs, ex)
                } catch (ex: DataAccessException) {
                    fallbackToNonFetch(datasetUUIDs, ex)
                }

            val latestJudgementByDatasetId =
                judgementEntities
                    .groupBy { it.datasetId }
                    .mapValues { (_, judgements) ->
                        check(judgements.size == 1) {
                            "Expected exactly one DatasetJudgement for" +
                                " datasetId=${judgements.first().datasetId} but found ${judgements.size}"
                        }
                        val j = judgements.first()
                        MinimalDatasetJudgement(
                            dataSetJudgementId = j.dataSetJudgementId.toString(),
                            qaJudgeUserId = j.qaJudgeUserId.toString(),
                            qaJudgeUserName = j.qaJudgeUserName,
                        )
                    }

            return entities.map {
                it.toQaReviewResponseWithPrecomputedData(
                    showTriggeringUserId = userIsAdmin,
                    numberQaReports = numberQaReportsByDatasetId[it.dataId] ?: 0L,
                    latestJudgement = latestJudgementByDatasetId[convertToUUID(it.dataId)],
                )
            }
        }

        /**
         * Mapping for overview endpoints using precomputed aggregates.
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
         * Legacy mapping for the single-dataset detail endpoint.
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

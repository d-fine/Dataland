package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReporter
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.collections.iterator

/**
 * Utility class to support the creation of dataset reviews.
 *
 * Contains helper methods to create a dataset review entity from the given qa reports, dataset metadata
 * and data points in the dataset.
 */
@Service
class DatasetReviewCreationService
    @Autowired
    constructor(
        private val datasetJudgementSupportService: DatasetJudgementSupportService,
        private val keycloakUserService: KeycloakUserService,
    ) {
        /**
         * Helper method to create a dataset review entity from the given QA reports, dataset metadata, and data points.
         *
         * The resulting entity includes the latest QA report per company and data point type, the list of QA reporter
         * companies, and one review-details entry per data point in the dataset.
         *
         * @param datasetMetaData Metadata of the dataset used to populate company, data type, and reporting period.
         * @param datasetId Unique identifier of the dataset for which the review is created.
         * @param datatypeToDatapointIds Mapping of data point type to data point id contained in the dataset.
         * @return A fully initialized DatasetReviewEntity ready for persistence.
         */
        fun createDatasetReviewEntity(
            datasetMetaData: DataMetaInformation,
            datasetId: UUID,
            datatypeToDatapointIds: Map<String, String>,
        ): DatasetJudgementEntity {
            val qaReports =
                datasetJudgementSupportService
                    .findQaReports(datatypeToDatapointIds.values.toList())

            val dataPointTypeToQaReports = getLatestQaReportsByDataPointTypeAndReporter(qaReports)

            val datasetReviewEntity =
                DatasetJudgementEntity(
                    dataSetJudgementId = UUID.randomUUID(),
                    datasetId = datasetId,
                    companyId = ValidationUtils.convertToUUID(datasetMetaData.companyId),
                    dataType = datasetMetaData.dataType.toString(),
                    reportingPeriod = datasetMetaData.reportingPeriod,
                    qaJudgeUserId = ValidationUtils.convertToUUID(DatalandAuthentication.fromContext().userId),
                    qaJudgeUserName = DatalandAuthentication.fromContext().name,
                    qaReporters = getQaReporters(dataPointTypeToQaReports).toMutableList(),
                    dataPoints = mutableListOf(),
                )

            val datasetReviewEntityWithDataPoints =
                setDataPointsForReview(
                    datasetReviewEntity,
                    datatypeToDatapointIds,
                    dataPointTypeToQaReports,
                )
            return datasetReviewEntityWithDataPoints
        }

        /**
         * Helper Method to group QA reports by data point type and keep only the latest upload per reporter.
         *
         * For each data point type, the returned list contains at most one report per reporter user id,
         * selected by the greatest upload time.
         *
         * @param activeQaReports QA reports to evaluate.
         * @return Map keyed by data point type with the latest reports per reporter.
         */
        private fun getLatestQaReportsByDataPointTypeAndReporter(
            activeQaReports: Collection<DataPointQaReportEntity>,
        ): Map<String, Collection<DataPointQaReportEntity>> =
            activeQaReports.groupBy { it.dataPointType }.mapValues { (_, reports) ->
                reports.groupBy { it.reporterUserId }.map { (_, reportsByReporter) ->
                    reportsByReporter.maxBy { it.uploadTime }
                }
            }

        /**
         * Builds QA reporter entries from the latest QA reports grouped by data point type.
         *
         * Extracts distinct reporter user ids, fetches user details from Keycloak, and returns
         * a list of QA reporters containing user id, display name, and email.
         *
         * @param dataPointTypeToQaReports QA reports grouped by data point type.
         * @return List of QA reporters with resolved user details.
         */
        private fun getQaReporters(dataPointTypeToQaReports: Map<String, Collection<DataPointQaReportEntity>>): List<QaReporter> {
            val reporterUserIds =
                dataPointTypeToQaReports
                    .values
                    .flatten()
                    .map { it.reporterUserId }
                    .distinct()

            return reporterUserIds.map { reporterUserId ->
                val userInfo = keycloakUserService.getUser(reporterUserId)
                QaReporter(
                    reporterUserId = UUID.fromString(reporterUserId),
                    reporterUserName =
                        listOfNotNull(userInfo.firstName, userInfo.lastName)
                            .joinToString(" ")
                            .ifBlank { null },
                    reporterEmailAddress = userInfo.email,
                )
            }
        }

        /**
         * Helper method to populate the dataset review with data point review details.
         *
         * For each data point type in the dataset, this method creates a review-details entry and attaches
         * the latest QA reports for that data point type.
         *
         * @param datasetJudgementEntity The dataset review entity to enrich with data point review details.
         * @param datatypeToDatapointIds Mapping of data point type to data point id contained in the dataset.
         * @param latestQaReportsByDataPointTypeAndReporter Latest QA reports grouped by data point type.
         * @return The same DatasetReviewEntity instance with populated data points.
         */
        private fun setDataPointsForReview(
            datasetJudgementEntity: DatasetJudgementEntity,
            datatypeToDatapointIds: Map<String, String>,
            latestQaReportsByDataPointTypeAndReporter: Map<String, Collection<DataPointQaReportEntity>>,
        ): DatasetJudgementEntity {
            for ((dataPointType, dataPointId) in datatypeToDatapointIds) {
                val currentDataPointReviewDetails =
                    DataPointJudgementEntity(
                        dataPointType = dataPointType,
                        dataPointId = ValidationUtils.convertToUUID(dataPointId),
                        qaReports = latestQaReportsByDataPointTypeAndReporter[dataPointType]?.toMutableList() ?: mutableListOf(),
                        acceptedSource = null,
                        reporterUserIdOfAcceptedQaReport = null,
                        customValue = null,
                    )

                datasetJudgementEntity.addAssociatedDataPoints(currentDataPointReviewDetails)
            }
            return datasetJudgementEntity
        }
    }

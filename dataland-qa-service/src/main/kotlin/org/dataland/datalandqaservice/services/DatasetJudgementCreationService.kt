package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
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
 * Utility class to support the creation of dataset judgements.
 *
 * Contains helper methods to create a dataset judgement entity from the given qa reports, dataset metadata
 * and data points in the dataset.
 */
@Service
class DatasetJudgementCreationService
    @Autowired
    constructor(
        private val datasetJudgementSupportService: DatasetJudgementSupportService,
        private val keycloakUserService: KeycloakUserService,
    ) {
        /**
         * Helper method to create a dataset judgement entity from the given QA reports, dataset metadata, and data points.
         *
         * The resulting entity includes the latest QA report per reporter and data point type, the list of QA reporter
         * companies, and one judgement-details entry per data point in the dataset.
         *
         * @param datasetMetaData Metadata of the dataset used to populate company, data type, and reporting period.
         * @param datasetId Unique identifier of the dataset for which the judgement is created.
         * @param datatypeToDatapointIds Mapping of data point type to data point id contained in the dataset.
         * @return A fully initialized DatasetJudgementEntity ready for persistence.
         */
        fun createDatasetJudgementEntity(
            datasetMetaData: DataMetaInformation,
            datasetId: UUID,
            datatypeToDatapointIds: Map<String, String>,
        ): DatasetJudgementEntity {
            val qaReports =
                datasetJudgementSupportService
                    .findQaReports(datatypeToDatapointIds.values.toList())

            val dataPointTypeToQaReports = getLatestQaReportsByDataPointTypeAndReporter(qaReports)

            val judgeUserId = DatalandAuthentication.fromContext().userId

            val datasetJudgementEntity =
                DatasetJudgementEntity(
                    dataSetJudgementId = UUID.randomUUID(),
                    datasetId = datasetId,
                    companyId = UUID.fromString(datasetMetaData.companyId),
                    dataType = datasetMetaData.dataType,
                    reportingPeriod = datasetMetaData.reportingPeriod,
                    qaJudgeUserId = UUID.fromString(judgeUserId),
                    qaJudgeUserName = getUserName(keycloakUserService.getUser(judgeUserId)) ?: judgeUserId,
                    qaReporters = getQaReporters(dataPointTypeToQaReports).toMutableList(),
                    dataPoints = mutableListOf(),
                )

            val datasetJudgementEntityWithDataPoints =
                setDataPointsForJudgement(
                    datasetJudgementEntity,
                    datatypeToDatapointIds,
                    dataPointTypeToQaReports,
                )
            return datasetJudgementEntityWithDataPoints
        }

        /**
         * Resolve a human-readable display name for the given userInfo.
         *
         * Returns "First Last" when available; otherwise returns null.
         *
         * @param userInfo The user info containing the name
         * @return The display name or null if no name parts are set.
         */
        private fun getUserName(userInfo: KeycloakUserInfo): String? =
            listOfNotNull(userInfo.firstName, userInfo.lastName)
                .joinToString(" ")
                .ifBlank { null }

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
        ): Map<String, List<DataPointQaReportEntity>> =
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
        private fun getQaReporters(
            dataPointTypeToQaReports: Map<
                String,
                Collection<DataPointQaReportEntity>,
            >,
        ): List<QaReporter> {
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
                    reporterUserName = getUserName(userInfo),
                    reporterEmailAddress = userInfo.email,
                )
            }
        }

        /**
         * Helper method to populate the dataset judgement with data point judgement details.
         *
         * For each data point type in the dataset, this method creates a judgement-details entry and attaches
         * the latest QA reports for that data point type.
         *
         * @param datasetJudgementEntity The dataset judgement entity to enrich with data point judgement details.
         * @param datatypeToDatapointIds Mapping of data point type to data point id contained in the dataset.
         * @param latestQaReportsByDataPointTypeAndReporter Latest QA reports grouped by data point type.
         * @return The same DatasetJudgementEntity instance with populated data points.
         */
        private fun setDataPointsForJudgement(
            datasetJudgementEntity: DatasetJudgementEntity,
            datatypeToDatapointIds: Map<String, String>,
            latestQaReportsByDataPointTypeAndReporter: Map<String, Collection<DataPointQaReportEntity>>,
        ): DatasetJudgementEntity {
            for ((dataPointType, dataPointId) in datatypeToDatapointIds) {
                val currentDataPointJudgementDetails =
                    DataPointJudgementEntity(
                        dataPointType = dataPointType,
                        dataPointId = dataPointId,
                        qaReports = latestQaReportsByDataPointTypeAndReporter[dataPointType]?.toMutableList() ?: mutableListOf(),
                        acceptedSource = null,
                        reporterUserIdOfAcceptedQaReport = null,
                        customValue = null,
                    )

                datasetJudgementEntity.addAssociatedDataPoints(currentDataPointJudgementDetails)
            }
            return datasetJudgementEntity
        }
    }

package org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandbackendutils.utils.ValidationUtils.convertToUUID
import org.dataland.datalandcommunitymanager.openApiClient.api.InheritedRolesControllerApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointReviewDetailsEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportDataPointWithReporterDetailsEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReporter
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetReviewSupportService
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Utility class to support the creation of dataset reviews.
 *
 * Contains helper methods to create a dataset review entity from the given qa reports, dataset metadata
 * and data points in the dataset.
 */
@Service
class DatasetReviewCreationUtils
    @Autowired
    constructor(
        private val inheritedRolesControllerApi: InheritedRolesControllerApi,
        private val companyDataControllerApi: CompanyDataControllerApi,
        private val datasetReviewSupportService: DatasetReviewSupportService,
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
        ): DatasetReviewEntity {
            val activeQaReports =
                datasetReviewSupportService
                    .findQaReportsWithDetails(datatypeToDatapointIds.values.toList())

            val mapDataPointTypeToQaReports = getLatestQaReportsByDataPointTypeAndReporter(activeQaReports)

            val reporterUserIds =
                mapDataPointTypeToQaReports
                    .values
                    .flatten()
                    .map { it.reporterUserId }
                    .distinct()

            val reporterIdToCompanyId = getCompanyIdsFromUserIds(reporterUserIds)

            val qaReporters =
                getQaReporters(
                    reporterUserIds,
                    reporterIdToCompanyId,
                )

            val datasetReviewEntity =
                DatasetReviewEntity(
                    dataSetReviewId = UUID.randomUUID(),
                    datasetId = datasetId,
                    companyId = convertToUUID(datasetMetaData.companyId),
                    dataType = datasetMetaData.dataType.toString(),
                    reportingPeriod = datasetMetaData.reportingPeriod,
                    reviewerUserId = convertToUUID(DatalandAuthentication.fromContext().userId),
                    reviewerUserName = DatalandAuthentication.fromContext().name,
                    qaReporters = qaReporters,
                    dataPoints = mutableListOf(),
                )

            val datasetReviewEntityWithDataPoints =
                setDataPointsForReview(
                    datasetReviewEntity,
                    datatypeToDatapointIds,
                    mapDataPointTypeToQaReports,
                    reporterIdToCompanyId,
                )
            return datasetReviewEntityWithDataPoints
        }

        /**
         * Helper Method to group QA reports by data point type and keeps only the latest upload per reporter.
         *
         * For each data point type, the returned list contains at most one report per reporter user id,
         * selected by the greatest upload time.
         *
         * @param activeQaReports QA reports to evaluate.
         * @return Map keyed by data point type with the latest reports per reporter.
         */
        private fun getLatestQaReportsByDataPointTypeAndReporter(
            activeQaReports: List<DataPointQaReportEntity>,
        ): Map<String, List<DataPointQaReportEntity>> {
            val latestByType = mutableMapOf<String, MutableList<DataPointQaReportEntity>>()
            for (qaReport in activeQaReports) {
                val reportsForType = latestByType.getOrPut(qaReport.dataPointType) { mutableListOf() }
                val existingIndex = reportsForType.indexOfFirst { it.reporterUserId == qaReport.reporterUserId }
                if (existingIndex >= 0) {
                    if (qaReport.uploadTime > reportsForType[existingIndex].uploadTime) {
                        reportsForType[existingIndex] = qaReport
                    }
                } else {
                    reportsForType.add(qaReport)
                }
            }
            return latestByType
        }

        /**
         * Helper method to build a list of QA reporters for the provided reporter user ids.
         *
         * Resolves company names from the provided reporter-to-company mapping and enriches each reporter
         * with user details from Keycloak.
         *
         * @param reporterUserIds Distinct reporter user ids to process.
         * @param reporterIdToCompanyId Mapping of reporter user id to company id, if available.
         * @return List of QA reporters with user and company information when available.
         */
        private fun getQaReporters(
            reporterUserIds: List<String>,
            reporterIdToCompanyId: Map<String, String>,
        ): MutableList<QaReporter> {
            val companyIdToName = getCompanyNameByIdMap(reporterIdToCompanyId.values.distinct())
            val qaReportersFinalList = mutableListOf<QaReporter>()
            for (reporterUserId in reporterUserIds) {
                val companyId = reporterIdToCompanyId[reporterUserId]
                val userInfo = keycloakUserService.getUser(reporterUserId)
                qaReportersFinalList.add(
                    QaReporter(
                        reporterUserId = convertToUUID(reporterUserId),
                        reporterUserName =
                            listOfNotNull(userInfo.firstName, userInfo.lastName)
                                .joinToString(" ")
                                .ifBlank { null },
                        reporterEmailAddress = userInfo.email,
                        reportCompanyName = companyId?.let { companyIdToName[it] },
                        reporterCompanyId = companyId?.let { convertToUUID(it) },
                    ),
                )
            }

            return qaReportersFinalList
        }

        /**
         * Helper Method to resolve the reporter company id for each reporter user id.
         *
         * Uses the inherited roles API to look up the company id for every reporter user id and returns
         * a map keyed by reporter user id with the corresponding company id as value.
         *
         * @param reporterUserIds Reporter user ids to resolve.
         * @return Map of reporter user id to company id for all resolvable reporters.
         */
        private fun getCompanyIdsFromUserIds(reporterUserIds: List<String>): Map<String, String> {
            val reporterIdToCompanyId = mutableMapOf<String, String>()
            for (reporterUserId in reporterUserIds) {
                val companyId =
                    inheritedRolesControllerApi
                        .getInheritedRoles(reporterUserId)
                        .keys
                        .firstOrNull()
                if (companyId != null) {
                    reporterIdToCompanyId[reporterUserId] = companyId
                }
            }
            return reporterIdToCompanyId
        }

        /**
         * Helper method to get a map of company names by company id.
         *
         * Resolves the provided company ids to display names via the company validation API.
         *
         * @param uniqueCompanyIds List of unique company ids to resolve.
         * @return Map of company id to company name for the provided ids.
         */
        private fun getCompanyNameByIdMap(uniqueCompanyIds: List<String>): Map<String, String> {
            val reporterCompanyNames =
                companyDataControllerApi
                    .postCompanyValidation(uniqueCompanyIds)
                    .map { it.companyInformation?.companyName ?: it.identifier }

            return uniqueCompanyIds.zip(reporterCompanyNames).toMap()
        }

        /**
         * Helper method to populate the dataset review with data point review details.
         *
         * For each data point type in the dataset, this method creates a review-details entry and attaches
         * the latest QA reports for that data point type. Each attached report is enriched with the
         * reporter company id resolved from the provided mapping.
         *
         * @param reviewEntity The dataset review entity to enrich with data point review details.
         * @param datatypeToDatapointIds Mapping of data point type to data point id contained in the dataset.
         * @param latestQaReportsByDataPointTypeAndReporter Latest QA reports grouped by data point type.
         * @param reporterIdToCompanyId Map of reporter user id to reporter company id.
         * @return The same DatasetReviewEntity instance with populated data points.
         */
        private fun setDataPointsForReview(
            reviewEntity: DatasetReviewEntity,
            datatypeToDatapointIds: Map<String, String>,
            latestQaReportsByDataPointTypeAndReporter: Map<String, List<DataPointQaReportEntity>>,
            reporterIdToCompanyId: Map<String, String>,
        ): DatasetReviewEntity {
            for ((dataPointType, dataPointId) in datatypeToDatapointIds) {
                val currentDataPointReviewDetails =
                    DataPointReviewDetailsEntity(
                        dataPointType = dataPointType,
                        dataPointId = convertToUUID(dataPointId),
                        qaReports = mutableListOf(),
                        acceptedSource = null,
                        companyIdOfAcceptedQaReport = null,
                        customValue = null,
                    )

                latestQaReportsByDataPointTypeAndReporter[dataPointType]
                    .orEmpty()
                    .forEach { qaReport ->
                        currentDataPointReviewDetails
                            .addAssociatedQaReports(
                                QaReportDataPointWithReporterDetailsEntity(
                                    qaReportId = convertToUUID(qaReport.qaReportId),
                                    verdict = qaReport.verdict,
                                    correctedData = qaReport.correctedData,
                                    reporterUserId = convertToUUID(qaReport.reporterUserId),
                                    reporterCompanyId =
                                        reporterIdToCompanyId[qaReport.reporterUserId]
                                            ?.let { convertToUUID(it) },
                                ),
                            )
                    }

                reviewEntity.addAssociatedDataPoints(currentDataPointReviewDetails)
            }
            return reviewEntity
        }
    }

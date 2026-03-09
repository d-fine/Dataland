package org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
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
            val qaReportEntities =
                datasetReviewSupportService
                    .findQaReportsWithDetails(datatypeToDatapointIds.values.toList())

            val latestQaReportForCompanyAndType = getLatestQaReportForEachCompanyAndDataPointType(qaReportEntities)
            val qaReporterCompanies = getQaReporterCompanies(latestQaReportForCompanyAndType)

            val datasetReviewEntity =
                DatasetReviewEntity(
                    dataSetReviewId = UUID.randomUUID(),
                    datasetId = datasetId,
                    companyId = convertToUUID(datasetMetaData.companyId),
                    dataType = datasetMetaData.dataType.toString(),
                    reportingPeriod = datasetMetaData.reportingPeriod,
                    reviewerUserId = convertToUUID(DatalandAuthentication.fromContext().userId),
                    reviewerUserName = DatalandAuthentication.fromContext().name,
                    qaReporterCompanies = qaReporterCompanies.toMutableList(),
                    dataPoints = mutableListOf(),
                )

            val datasetReviewEntityWithDataPoints =
                setDataPointsForReview(
                    datasetReviewEntity,
                    datatypeToDatapointIds,
                    latestQaReportForCompanyAndType,
                )
            return datasetReviewEntityWithDataPoints
        }

        /**
         * Helper method to populate the dataset review with data point review details.
         *
         * For each data point type in the dataset, this method creates a review-details entry and attaches the
         * latest QA reports for that data point type, then adds it to the given review entity.
         *
         * @param reviewEntity The dataset review entity to enrich with data point review details.
         * @param datatypeToDatapointIds Mapping of data point type to data point id contained in the dataset.
         * @param latestQaReportForCompanyAndType Latest QA report per company and data point type.
         * @return The same DatasetReviewEntity instance with populated data points.
         */
        private fun setDataPointsForReview(
            reviewEntity: DatasetReviewEntity,
            datatypeToDatapointIds: Map<String, String>,
            latestQaReportForCompanyAndType: LinkedHashMap<String, DataPointQaReportEntity>,
        ): DatasetReviewEntity {
            for ((dataPointType, dataPointId) in datatypeToDatapointIds) {
                val qaReportsForThisDataPointType =
                    latestQaReportForCompanyAndType
                        .values
                        .filter { it.dataPointType == dataPointType }

                val currentDataPointReviewDetails =
                    DataPointReviewDetailsEntity(
                        dataPointType = dataPointType,
                        dataPointId = convertToUUID(dataPointId),
                        qaReports = mutableListOf(),
                        acceptedSource = null,
                        companyIdOfAcceptedQaReport = null,
                        customValue = null,
                    )

                qaReportsForThisDataPointType.forEach { qaReport ->
                    currentDataPointReviewDetails.addAssociatedQaReports(
                        QaReportDataPointWithReporterDetailsEntity(
                            qaReportId = convertToUUID(qaReport.qaReportId),
                            verdict = qaReport.verdict,
                            correctedData = qaReport.correctedData,
                            reporterUserId = convertToUUID(qaReport.reporterUserId),
                            reporterCompanyId =
                                latestQaReportForCompanyAndType
                                    .entries
                                    .first { it.value == qaReport }
                                    .key
                                    .substringBefore("|")
                                    .let { convertToUUID(it) },
                        ),
                    )
                }

                reviewEntity.addAssociatedDataPoints(currentDataPointReviewDetails)
            }
            return reviewEntity
        }

        /**
         * Helper method to get the companies of the reporters of QA reports.
         *
         * Only considers the latest QA report for each company and data point type combination to determine
         * the reporter companies.
         *
         * @param latestQaReportForCompanyAndType Latest QA report per company and data point type.
         * @return List of reporter companies derived from the latest QA reports.
         */
        private fun getQaReporterCompanies(
            latestQaReportForCompanyAndType: LinkedHashMap<String, DataPointQaReportEntity>,
        ): List<QaReporter> {
            val uniqueCompanyIds =
                latestQaReportForCompanyAndType.keys
                    .map { key -> key.split("|")[0] }
                    .distinct()

            val companyNameById = getCompanyNameByIdMap(uniqueCompanyIds)

            return uniqueCompanyIds.map { companyId ->
                QaReporter(
                    companyNameById.getValue(companyId),
                    convertToUUID(companyId),
                )
            }
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
         * Helper method to get the latest QA report for each combination of company and data point type.
         *
         * Resolves the reporter's company for each QA report, then keeps only the most recent report per
         * company and data point type combination.
         *
         * @param qaReportEntities List of QA report entities to evaluate.
         * @return Map keyed by "companyId|dataPointType" containing the latest QA report for each key.
         */
        private fun getLatestQaReportForEachCompanyAndDataPointType(
            qaReportEntities: List<DataPointQaReportEntity>,
        ): LinkedHashMap<String, DataPointQaReportEntity> {
            val map = linkedMapOf<String, DataPointQaReportEntity>()
            for (entry in qaReportEntities) {
                val companyId =
                    inheritedRolesControllerApi
                        .getInheritedRoles(entry.reporterUserId)
                        .keys
                        .firstOrNull()
                        ?: throw ResourceNotFoundApiException(
                            "Company of QA report reporter not found.",
                            "Could not find a company for the user ${entry.reporterUserId} who " +
                                "reported a QA report with id ${entry.qaReportId}.",
                        )

                val compositeKey = "$companyId|${entry.dataPointType}"
                val existing = map[compositeKey]
                if (existing == null || entry.uploadTime > existing.uploadTime) {
                    map[compositeKey] = entry
                }
            }
            return map
        }
    }

package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.utils.ValidationUtils.convertToUUID
import org.dataland.datalandcommunitymanager.openApiClient.api.InheritedRolesControllerApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointReviewDetailsEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportDataPointWithReporterDetailsEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReporterCompany
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetReviewRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException as BackendClientException

/**
 * Service class for dataset review objects.
 */
@Service
class DatasetReviewService
    @Autowired
    constructor(
        private val datasetReviewRepository: DatasetReviewRepository,
        private val datasetReviewSupportService: DatasetReviewSupportService,
        private val inheritedRolesControllerApi: InheritedRolesControllerApi,
        private val companyDataControllerApi: CompanyDataControllerApi,
    ) {
        /**
         * Create a dataset review object associated to the given dataset.
         */
        @Transactional
        fun postDatasetReview(datasetId: UUID): DatasetReviewResponse {
            lateinit var datatypeToDatapointIds: Map<String, String>
            try {
                datatypeToDatapointIds = datasetReviewSupportService.getContainedDataPoints(datasetId.toString())
            } catch (_: BackendClientException) {
                throw ResourceNotFoundApiException(
                    "Dataset not found",
                    "Dataset with the id: $datasetId could not be found.",
                )
            }
            if (datasetReviewRepository.findAllByDatasetIdAndReviewState(datasetId, DatasetReviewState.Pending).isNotEmpty()) {
                throw ConflictApiException(
                    summary = "Pending dataset review entity already exists.",
                    message = "There is already a dataset review entity for this dataset which is pending.",
                )
            }
            val qaReportsWithDetails =
                datasetReviewSupportService
                    .findQaReportsWithDetails(datatypeToDatapointIds.values.toList())

            val latestQaReportForCompanyAndType = getLatestQaReportForEachCompanyAndDataPointType(qaReportsWithDetails)
            val qaReporterCompanies = getQaReporterCompanies(latestQaReportForCompanyAndType)
            val dataPoints = getDataPointsForReview(datatypeToDatapointIds, latestQaReportForCompanyAndType)
            val datasetMetaData = datasetReviewSupportService.getDataMetaInfo(datasetId.toString())

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
                    dataPoints = dataPoints,
                )
            return datasetReviewRepository.save(datasetReviewEntity).toDatasetReviewResponse()
        }

        /**
         * Helper method to get the data points with details for the review process.
         */
        private fun getDataPointsForReview(
            datatypeToDatapointIds: Map<String, String>,
            latestQaReportForCompanyAndType: LinkedHashMap<String, DataPointQaReportEntity>,
        ): MutableList<DataPointReviewDetailsEntity> {
            val dataPoints = mutableListOf<DataPointReviewDetailsEntity>()

            for ((dataPointType, dataPointId) in datatypeToDatapointIds) {
                val qaReportsForThisDataPointType =
                    latestQaReportForCompanyAndType
                        .values
                        .filter { it.dataPointType == dataPointType }

                dataPoints.add(
                    DataPointReviewDetailsEntity(
                        dataPointType = dataPointType,
                        dataPointId = convertToUUID(dataPointId),
                        qaReports =
                            qaReportsForThisDataPointType
                                .map { qaReport ->
                                    QaReportDataPointWithReporterDetailsEntity(
                                        dataPointReviewDetails = null,
                                        qaReportId = convertToUUID(qaReport.qaReportId),
                                        verdict = qaReport.verdict,
                                        correctedData = qaReport.correctedData,
                                        reporterUserId = convertToUUID(qaReport.reporterUserId),
                                        reporterCompanyId =
                                            latestQaReportForCompanyAndType
                                                .entries
                                                .firstOrNull { entry -> entry.value == qaReport }
                                                ?.key
                                                ?.split("|")
                                                ?.get(0)
                                                ?.let { x -> kotlin.runCatching { convertToUUID(x) }.getOrNull() },
                                    )
                                }.toMutableList(),
                        acceptedSource = null,
                        companyIdOfAcceptedQaReport = null,
                        customValue = null,
                        datasetReview = null,
                    ),
                )
            }
            return dataPoints
        }

        /**
         * Helper method to get the companies of the reporters of qa reports.
         *
         * Only considers the latest qa report for each company and data point type combination
         * to determine the reporter companies.
         */
        private fun getQaReporterCompanies(
            latestQaReportForCompanyAndType: LinkedHashMap<String, DataPointQaReportEntity>,
        ): List<QaReporterCompany> {
            val uniqueCompanyIds =
                latestQaReportForCompanyAndType.keys
                    .map { key -> key.split("|")[0] }
                    .distinct()

            val companyNameById = getCompanyNameByIdMap(uniqueCompanyIds)

            return uniqueCompanyIds.map { companyId ->
                QaReporterCompany(
                    companyNameById[companyId] ?: "Unknown Company",
                    kotlin.runCatching { convertToUUID(companyId) }.getOrNull(),
                )
            }
        }

        /**
         * Helper method to get a map of company names by company id.
         */
        private fun getCompanyNameByIdMap(uniqueCompanyIds: List<String>): Map<String, String> {
            val reporterCompanyNames =
                companyDataControllerApi
                    .postCompanyValidation(uniqueCompanyIds)
                    .map { it.companyInformation?.companyName ?: it.identifier }

            return uniqueCompanyIds.zip(reporterCompanyNames).toMap()
        }

        /**
         * Helper method to get the latest qa report for each combination of company and data point type.
         */
        private fun getLatestQaReportForEachCompanyAndDataPointType(
            qaReportsWithDetails: List<DataPointQaReportEntity>,
        ): LinkedHashMap<String, DataPointQaReportEntity> {
            val map = linkedMapOf<String, DataPointQaReportEntity>()
            for (entry in qaReportsWithDetails) {
                var companyId =
                    inheritedRolesControllerApi
                        .getInheritedRoles(entry.reporterUserId)
                        .keys
                        .firstOrNull()
                if (companyId == null) {
                    companyId = "Unknown Company of user " + entry.reporterUserId
                }

                val compositeKey = "$companyId|${entry.dataPointType}"
                val existing = map[compositeKey]
                if (existing == null || entry.uploadTime > existing.uploadTime) {
                    map[compositeKey] = entry
                }
            }
            return map
        }

        /**
         * Method to set reviewer to current user.
         */
        @Transactional
        fun setReviewer(datasetReviewId: UUID): DatasetReviewResponse {
            val datasetReview = getDatasetReview(datasetReviewId)
            datasetReview.reviewerUserId = convertToUUID(DatalandAuthentication.fromContext().userId)
            datasetReview.reviewerUserName = DatalandAuthentication.fromContext().name
            return datasetReviewRepository.save(datasetReview).toDatasetReviewResponse()
        }

        /**
         * Method to set state of dataset review object.
         */
        @Transactional
        fun setReviewState(
            datasetReviewId: UUID,
            state: DatasetReviewState,
        ): DatasetReviewResponse {
            val datasetReview = getDatasetReview(datasetReviewId)
            isUserReviewer(datasetReview.reviewerUserId)
            datasetReview.reviewState = state
            return datasetReviewRepository.save(datasetReview).toDatasetReviewResponse()
        }

        /**
         * Method to accept the original data point as the accepted value for a data point in the dataset review.
         */
        @Transactional
        fun acceptOriginalDataPoint(
            datasetReview: DatasetReviewEntity,
            dataPointIndex: Int,
        ): DatasetReviewResponse {
            datasetReview.dataPoints[dataPointIndex].companyIdOfAcceptedQaReport = null
            datasetReview.dataPoints[dataPointIndex].customValue = null
            return datasetReviewRepository.save(datasetReview).toDatasetReviewResponse()
        }

        /**
         * Method to accept a QA report data point as the accepted value for a data point in the dataset review.
         */
        @Transactional
        fun acceptQaReportDataPoint(
            datasetReview: DatasetReviewEntity,
            dataPointIndex: Int,
            companyIdOfAcceptedQaReport: UUID,
        ): DatasetReviewResponse {
            datasetReview.dataPoints[dataPointIndex].companyIdOfAcceptedQaReport = companyIdOfAcceptedQaReport
            datasetReview.dataPoints[dataPointIndex].customValue = null
            return datasetReviewRepository.save(datasetReview).toDatasetReviewResponse()
        }

        /**
         * Method to accept a custom value for a data point, including validation against the data point type spec.
         */
        @Transactional
        fun acceptCustomDataPoint(
            datasetReview: DatasetReviewEntity,
            dataPointIndex: Int,
            dataPointType: String,
            customValue: String,
        ): DatasetReviewResponse {
            try {
                datasetReviewSupportService.validateCustomDataPoint(customValue, dataPointType)
            } catch (e: BackendClientException) {
                throw InvalidInputApiException(
                    "Datapoint not valid.",
                    "Datapoint given does not match the specification of $dataPointType.",
                    e,
                )
            }
            datasetReview.dataPoints[dataPointIndex].companyIdOfAcceptedQaReport = null
            datasetReview.dataPoints[dataPointIndex].customValue = customValue

            return datasetReviewRepository.save(datasetReview).toDatasetReviewResponse()
        }

        /**
         * Method to set the accepted source for a data point in a dataset review.
         */
        @Transactional
        fun setAcceptedSource(
            datasetReviewId: UUID,
            dataPointType: String,
            acceptedSource: AcceptedDataPointSource,
            companyIdOfAcceptedQaReport: String?,
            customValue: String?,
        ): DatasetReviewResponse {
            val datasetReview = getDatasetReview(datasetReviewId)
            isUserReviewer(datasetReview.reviewerUserId)
            val dataPointIndex = getIndexOfDataPointByDataPointType(datasetReview, dataPointType)
            datasetReview.dataPoints[dataPointIndex].acceptedSource = acceptedSource

            return when (acceptedSource) {
                AcceptedDataPointSource.Original -> {
                    acceptOriginalDataPoint(datasetReview, dataPointIndex)
                }

                AcceptedDataPointSource.Qa -> {
                    if (companyIdOfAcceptedQaReport == null) {
                        throw InvalidInputApiException(
                            "Missing companyIdOfAcceptedQaReport.",
                            "companyIdOfAcceptedQaReport must be provided when acceptedSource is Qa.",
                        )
                    }
                    acceptQaReportDataPoint(datasetReview, dataPointIndex, convertToUUID(companyIdOfAcceptedQaReport))
                }

                AcceptedDataPointSource.Custom -> {
                    if (customValue == null) {
                        throw InvalidInputApiException(
                            "Missing customValue.",
                            "customValue must be provided when acceptedSource is Custom.",
                        )
                    }
                    acceptCustomDataPoint(datasetReview, dataPointIndex, dataPointType, customValue)
                }
            }
        }

        /**
         * Method to get a dataset review entity by id and convert to response.
         */
        @Transactional(readOnly = true)
        fun getDatasetReviewById(datasetReviewId: UUID): DatasetReviewResponse = getDatasetReview(datasetReviewId).toDatasetReviewResponse()

        /**
         * Method to get dataset review objects by dataset id.
         */
        @Transactional(readOnly = true)
        fun getDatasetReviewsByDatasetId(datasetId: UUID): List<DatasetReviewResponse> =
            datasetReviewRepository.findAllByDatasetId(datasetId).map {
                it.toDatasetReviewResponse()
            }

        /**
         * Helper method to get a dataset review entity by id including exception handling.
         */
        @Transactional(readOnly = true)
        fun getDatasetReview(datasetReviewId: UUID): DatasetReviewEntity =
            datasetReviewRepository.findById(datasetReviewId).orElseThrow {
                ResourceNotFoundApiException(
                    "Dataset review object not found",
                    "No Dataset review object with the id: $datasetReviewId could be found.",
                )
            }

        /**
         * Method to find a data point by its type, throws ResourceNotFoundApiException if not found.
         */
        private fun getIndexOfDataPointByDataPointType(
            datasetReview: DatasetReviewEntity,
            dataPointType: String,
        ): Int {
            val index =
                datasetReview.dataPoints.indexOfFirst {
                    it.dataPointType == dataPointType
                }
            if (index == -1) {
                throw ResourceNotFoundApiException(
                    "Datapoint not found.",
                    "No datapoint with type $dataPointType in dataset review.",
                )
            }
            return index
        }

        /**
         * Throws InsufficientRightsApiException if user is not reviewer.
         */
        private fun isUserReviewer(reviewerUserId: UUID) {
            if (DatalandAuthentication.fromContext().userId != reviewerUserId.toString()) {
                throw InsufficientRightsApiException(
                    summary = "Only the reviewer is allowed to patch this dataset review object.",
                    message = "Please patch yourself as the reviewer before patching this object.",
                ) as Throwable
            }
        }
    }

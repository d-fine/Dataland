package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandbackendutils.utils.ValidationUtils.convertToUUID
import org.dataland.datalandcommunitymanager.openApiClient.api.InheritedRolesControllerApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportIdWithUploaderCompanyId
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetReviewRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException as BackendClientException
import org.dataland.datalandspecificationservice.openApiClient.infrastructure.ClientException as SpecificationClientException

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
        private val keycloakUserService: KeycloakUserService,
    ) {
        /**
         * Create a dataset review object associated to the given dataset.
         */
        @Transactional
        fun postDatasetReview(datasetId: UUID): DatasetReviewResponse<Any> {
            lateinit var datatypeToDatapointIds: Map<String, String>
            try {
                datatypeToDatapointIds = datasetReviewSupportService.getContainedDataPoints(datasetId.toString())
            } catch (_: BackendClientException) {
                throw ResourceNotFoundApiException(
                    "Dataset not found",
                    "Dataset with the id: $datasetId could not be found.",
                )
            }
            val dataPointQaReportIds =
                datasetReviewSupportService
                    .findQaReportIdsForDataPoints(datatypeToDatapointIds.values.toList())

            val qaReportIdWithUploaderCompanyIds =
                dataPointQaReportIds.map {
                    val uploaderCompanyId =
                        inheritedRolesControllerApi
                            .getInheritedRoles(it)
                            .keys
                            .firstOrNull()
                            ?.let { companyId -> convertToUUID(companyId) }
                    QaReportIdWithUploaderCompanyId(
                        convertToUUID(it),
                        uploaderCompanyId,
                    )
                }

            val datasetMetaData = datasetReviewSupportService.getDataMetaInfo(datasetId.toString())

            val datasetReviewEntity =
                DatasetReviewEntity(
                    dataSetReviewId = UUID.randomUUID(),
                    datasetId = datasetId,
                    companyId = convertToUUID(datasetMetaData.companyId),
                    dataType = datasetMetaData.dataType.toString(),
                    reportingPeriod = datasetMetaData.reportingPeriod,
                    reviewerUserId = convertToUUID(DatalandAuthentication.fromContext().userId),
                    qaReports = qaReportIdWithUploaderCompanyIds.toSet(),
                )
            return datasetReviewRepository.save(datasetReviewEntity).toDatasetReviewResponseWithReviewerUserName()
        }

        /**
         * Method to get dataset review objects by dataset id.
         */
        @Transactional(readOnly = true)
        fun getDatasetReviewsByDatasetId(datasetId: UUID): List<DatasetReviewResponse<Any>> {
            val entities = datasetReviewRepository.findAllByDatasetId(datasetId)
            return entities.toDatasetReviewResponsesWithReviewerUserNames()
        }

        /**
         * Method to set reviewer to current user.
         */
        @Transactional
        fun setReviewer(datasetReviewId: UUID): DatasetReviewResponse<Any> {
            val datasetReview = getDatasetReview(datasetReviewId)
            datasetReview.reviewerUserId = convertToUUID(DatalandAuthentication.fromContext().userId)

            return datasetReviewRepository.save(datasetReview).toDatasetReviewResponseWithReviewerUserName()
        }

        /**
         * Method to set state of dataset review object.
         */
        @Transactional
        fun setReviewState(
            datasetReviewId: UUID,
            state: DatasetReviewState,
        ): DatasetReviewResponse<Any> {
            val datasetReview = getDatasetReview(datasetReviewId)
            isUserReviewer(datasetReview.reviewerUserId)
            datasetReview.reviewState = state

            return datasetReviewRepository.save(datasetReview).toDatasetReviewResponseWithReviewerUserName()
        }

        /**
         * Method to approve a datapoint from dataset. Also removes approved qa reports and custom datapoints accordingly.
         */
        @Transactional
        fun acceptOriginalDatapoint(
            datasetReviewId: UUID,
            dataPointId: UUID,
        ): DatasetReviewResponse<Any> {
            val datasetReview = getDatasetReview(datasetReviewId)
            isUserReviewer(datasetReview.reviewerUserId)
            val datatypeToDatapointIds = datasetReviewSupportService.getContainedDataPoints(datasetReview.datasetId.toString())
            if (dataPointId.toString() !in datatypeToDatapointIds.values) {
                throw ResourceNotFoundApiException(
                    "Datapoint not found.",
                    "Datapoint id $dataPointId not part of dataset ${datasetReview.datasetId}.",
                )
            }
            val dataPointType = datasetReviewSupportService.getDataPointType(dataPointId)
            datasetReview.approvedDataPointIds[dataPointType] = dataPointId
            datasetReview.approvedQaReportIds.remove(dataPointType)
            datasetReview.approvedCustomDataPointIds.remove(dataPointType)
            return datasetReviewRepository.save(datasetReview).toDatasetReviewResponseWithReviewerUserName()
        }

        /**
         * Method to approve a qa report. Also removes approved data points and custom datapoints accordingly.
         */
        @Transactional
        fun acceptQaReport(
            datasetReviewId: UUID,
            qaReportId: UUID,
        ): DatasetReviewResponse<Any> {
            val datasetReview = getDatasetReview(datasetReviewId)
            isUserReviewer(datasetReview.reviewerUserId)
            datasetReview.qaReports.firstOrNull { it.qaReportId == qaReportId }
                ?: throw ResourceNotFoundApiException(
                    "QA report not found.",
                    "QA report id $qaReportId not part of collected qa reports of " +
                        "dataset review ${datasetReview.dataSetReviewId}.",
                )

            val dataPointType =
                datasetReviewSupportService.findDataPointTypeUsingQaReportId(qaReportId)

            datasetReview.approvedQaReportIds[dataPointType] = qaReportId
            datasetReview.approvedDataPointIds.remove(dataPointType)
            datasetReview.approvedCustomDataPointIds.remove(dataPointType)
            return datasetReviewRepository.save(datasetReview).toDatasetReviewResponseWithReviewerUserName()
        }

        /**
         * Method to approve a custom data point. Also removes approved data points and qa reports accordingly.
         */
        @Suppress("ThrowsCount")
        @Transactional
        fun acceptCustomDataPoint(
            datasetReviewId: UUID,
            dataPoint: String,
            dataPointType: String,
        ): DatasetReviewResponse<Any> {
            val datasetReview = getDatasetReview(datasetReviewId)
            isUserReviewer(datasetReview.reviewerUserId)
            lateinit var frameworksOfDataPointType: List<String>
            try {
                frameworksOfDataPointType =
                    datasetReviewSupportService.getFrameworksForDataPointType(dataPointType)
            } catch (_: SpecificationClientException) {
                throw InvalidInputApiException(
                    "DataPoint type not found.",
                    "Cannot find DataPoint type $dataPointType on Dataland.",
                )
            }
            if (datasetReview.dataType !in frameworksOfDataPointType) {
                throw InvalidInputApiException(
                    "Datapoint type not valid.",
                    "Datapoint type is not part of the framework ${datasetReview.dataType}.",
                )
            }
            try {
                datasetReviewSupportService.validateCustomDataPoint(dataPoint, dataPointType)
            } catch (e: BackendClientException) {
                throw InvalidInputApiException(
                    "Datapoint not valid.",
                    "Datapoint given does not match the specification of $dataPointType.",
                    e,
                )
            }

            datasetReview.approvedCustomDataPointIds[dataPointType] = dataPoint
            datasetReview.approvedDataPointIds.remove(dataPointType)
            datasetReview.approvedQaReportIds.remove(dataPointType)
            return datasetReviewRepository.save(datasetReview).toDatasetReviewResponseWithReviewerUserName()
        }

        /**
         * Method to get a dataset review entity by id and convert to response
         */
        @Transactional(readOnly = true)
        fun getDatasetReviewById(datasetReviewId: UUID): DatasetReviewResponse<Any> =
            getDatasetReview(datasetReviewId).toDatasetReviewResponse()

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

        private fun DatasetReviewEntity.toDatasetReviewResponseWithReviewerUserName(): DatasetReviewResponse<Any> {
            val response = this.toDatasetReviewResponse()
            response.reviewerUserName = resolveReviewerUserName(this.reviewerUserId)
            return response
        }

        private fun List<DatasetReviewEntity>.toDatasetReviewResponsesWithReviewerUserNames(): List<DatasetReviewResponse<Any>> {
            val reviewerUserIds = this.mapNotNull { it.reviewerUserId }.distinct()
            val reviewerIdToName = reviewerUserIds.associateWith { resolveReviewerUserName(it) }

            return this.map {
                val response = it.toDatasetReviewResponse()
                response.reviewerUserName = it.reviewerUserId?.let { reviewerUserId -> reviewerIdToName[reviewerUserId] }
                response
            }
        }

        private fun resolveReviewerUserName(reviewerUserId: UUID?): String? {
            val reviewerUserIdString = reviewerUserId?.toString() ?: return null
            val userInfo = keycloakUserService.getUser(reviewerUserIdString)

            val firstName = userInfo.firstName?.trim().orEmpty()
            val lastName = userInfo.lastName?.trim().orEmpty()
            val fullName = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")

            return when {
                fullName.isNotBlank() -> fullName
                !userInfo.email.isNullOrBlank() -> userInfo.email
                else -> reviewerUserIdString
            }
        }

        /**
         * Throws InsufficientRightsApiException if user is not reviewer.
         */
        private fun isUserReviewer(reviewerUserId: UUID?) {
            if (DatalandAuthentication.fromContext().userId != reviewerUserId.toString()) {
                throw InsufficientRightsApiException(
                    summary = "Only the reviewer is allowed to patch this dataset review object.",
                    message = "Please patch yourself as the reviewer before patching this object.",
                ) as Throwable
            }
        }
    }

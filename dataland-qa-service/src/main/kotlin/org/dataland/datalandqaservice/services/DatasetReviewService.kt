package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataPointToValidate
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetReviewRepository
import org.dataland.datalandspecificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpClientErrorException
import java.util.UUID

/**
 * Service class for dataset review objects.
 */
@Service
class DatasetReviewService(
    @Autowired var datasetReviewRepository: DatasetReviewRepository,
    @Autowired var dataPointControllerApi: DataPointControllerApi,
    @Autowired var specificationControllerApi: SpecificationControllerApi,
    @Autowired val metaDataControllerApi: MetaDataControllerApi,
) {
    /**
     * Method to set reviewer to current user.
     */
    @Transactional
    fun setReviewer(datasetReviewId: UUID): DatasetReviewResponse {
        val datasetReview = getDatasetReviewById(datasetReviewId)
        datasetReview.reviewerUserId = UUID.fromString(DatalandAuthentication.fromContext().userId)

        return datasetReviewRepository.save(datasetReview).toDatasetReviewResponse()
    }

    /**
     * Method to set state of dataset review object.
     */
    @Transactional
    fun setState(
        datasetReviewId: UUID,
        state: DatasetReviewState,
    ): DatasetReviewResponse {
        val datasetReview = getDatasetReviewById(datasetReviewId)
        isUserReviewer(datasetReview.reviewerUserId)
        datasetReview.status = state

        return datasetReviewRepository.save(datasetReview).toDatasetReviewResponse()
    }

    /**
     * Method to approve a datapoint from dataset. Also removes approved qa reports and custom datapoints accordingly.
     */
    @Transactional
    fun acceptOriginalDatapoint(
        datasetReviewId: UUID,
        dataPointId: UUID,
    ): DatasetReviewResponse {
        val datasetReview = getDatasetReviewById(datasetReviewId)
        isUserReviewer(datasetReview.reviewerUserId)
        val datatypeToDatapointIds =
            metaDataControllerApi.getContainedDataPoints(datasetReview.datasetId.toString())
        if (dataPointId.toString() !in datatypeToDatapointIds.values) {
            throw ResourceNotFoundApiException(
                "Datapoint not found.",
                "Datapoint id $dataPointId not part of dataset ${datasetReview.datasetId}.",
            )
        }
        val dataPointType = dataPointControllerApi.getDataPointMetaInfo(dataPointId.toString()).dataPointType
        datasetReview.approvedDataPointIds[dataPointType] = dataPointId
        datasetReview.approvedQaReportIds.remove(dataPointType)
        datasetReview.approvedCustomDataPointIds.remove(dataPointType)
        return datasetReviewRepository.save(datasetReview).toDatasetReviewResponse()
    }

    /**
     * Method to approve a qa report. Also removes approved data points and custom datapoints accordingly.
     */
    @Transactional
    fun acceptQaReport(
        datasetReviewId: UUID,
        qaReportId: UUID,
    ): DatasetReviewResponse {
        val datasetReview = getDatasetReviewById(datasetReviewId)
        isUserReviewer(datasetReview.reviewerUserId)
        val qaReport =
            datasetReview.qaReports.firstOrNull { it.qaReportId == qaReportId.toString() }
                ?: throw ResourceNotFoundApiException(
                    "QA report not found.",
                    "QA report id $qaReportId not part of collected qa reports of " +
                        "dataset review ${datasetReview.dataSetReviewId}.",
                )
        val dataPointType = qaReport.dataPointType
        datasetReview.approvedQaReportIds[dataPointType] = qaReportId
        datasetReview.approvedDataPointIds.remove(dataPointType)
        datasetReview.approvedCustomDataPointIds.remove(dataPointType)
        return datasetReviewRepository.save(datasetReview).toDatasetReviewResponse()
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
    ): DatasetReviewResponse {
        val datasetReview = getDatasetReviewById(datasetReviewId)
        isUserReviewer(datasetReview.reviewerUserId)
        lateinit var frameworksOfDataPointType: List<String>
        try {
            frameworksOfDataPointType = specificationControllerApi.getDataPointTypeSpecification(dataPointType).usedBy.map { it.id }
        } catch (_: HttpClientErrorException) {
            throw InvalidInputApiException(
                "DataPoint type not found.",
                "Cannot find DataPoint type $dataPointType on Dataland.",
            )
        }
        if (datasetReview.dataType !in frameworksOfDataPointType) {
            throw InvalidInputApiException(
                "Datapoint type not valid.",
                "Datapoint type either does not exist or is not part of the framework ${datasetReview.dataType}.",
            )
        }
        try {
            dataPointControllerApi.validateDataPoint(DataPointToValidate(dataPoint, dataPointType))
        } catch (e: HttpClientErrorException) {
            throw InvalidInputApiException(
                "Datapoint not valid.",
                "Datapoint given does not match the specification of $dataPointType. ${e.message}",
                e,
            )
        }

        datasetReview.approvedCustomDataPointIds[dataPointType] = dataPoint
        datasetReview.approvedDataPointIds.remove(dataPointType)
        datasetReview.approvedQaReportIds.remove(dataPointType)
        return datasetReviewRepository.save(datasetReview).toDatasetReviewResponse()
    }

    private fun getDatasetReviewById(datasetReviewId: UUID): DatasetReviewEntity =
        datasetReviewRepository.findById(datasetReviewId).orElseThrow {
            ResourceNotFoundApiException(
                "Dataset review object not found",
                "NoDataset review object with the id: $datasetReviewId could be found.",
            )
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

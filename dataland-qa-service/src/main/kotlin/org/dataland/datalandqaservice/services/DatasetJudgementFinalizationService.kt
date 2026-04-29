package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackend.openApiClient.model.UploadedDataPoint
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DataPointQaReviewManager.ReviewDataPointTask
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.DatasetJudgementValidationHelper
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Service responsible for handling the finalization of a dataset judgement,
 * which includes handling the acceptance or rejection of the dataset.
 */
@Service
class DatasetJudgementFinalizationService
    @Autowired
    constructor(
        private val dataPointControllerApi: DataPointControllerApi,
        private val dataPointQaReviewManager: DataPointQaReviewManager,
        private val qaReviewManager: QaReviewManager,
    ) {
        /**
         * Handles the rejection of a dataset
         */
        @Transactional
        fun handleRejection(datasetJudgement: DatasetJudgementEntity) {
            qaReviewManager.changeQaStatus(
                dataId = datasetJudgement.datasetId.toString(),
                qaStatus = QaStatus.Rejected,
                comment = null,
                overwriteDataPointQaStatus = true,
            )
        }

        /**
         * Handles the acceptance of a dataset
         */
        @Transactional
        fun handleAcceptance(datasetJudgement: DatasetJudgementEntity) {
            DatasetJudgementValidationHelper.validateAllDataPointsHaveAcceptedSource(datasetJudgement.dataPoints)
            updateDataPointQaStatus(datasetJudgement.dataPoints, datasetJudgement.companyId, datasetJudgement.reportingPeriod)
            qaReviewManager.changeQaStatus(
                dataId = datasetJudgement.datasetId.toString(),
                qaStatus = QaStatus.Accepted,
                comment = null,
                overwriteDataPointQaStatus = false,
            )
        }

        /**
         * Updates the QA status of each data point in the judgement according to its accepted source.
         *
         * @param dataPoints The data point judgement entities to process.
         * @param companyId The company ID to use when uploading new data points.
         * @param reportingPeriod The reporting period to use when uploading new data points.
         */
        private fun updateDataPointQaStatus(
            dataPoints: Collection<DataPointJudgementEntity>,
            companyId: UUID,
            reportingPeriod: String,
        ) {
            val triggeringUserId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()
            val timestamp = Instant.now().toEpochMilli()

            val reviewTasks =
                dataPoints.map { dataPoint ->
                    buildReviewTask(dataPoint, triggeringUserId, correlationId, timestamp)
                }

            dataPointQaReviewManager.reviewDataPoints(reviewTasks)

            dataPoints.forEach { uploadReplacementDataPointIfNeeded(it, companyId, reportingPeriod) }
        }

        /**
         * Uploads a replacement data point to the backend with bypassQa=true for [AcceptedDataPointSource.Qa]
         * and [AcceptedDataPointSource.Custom] sources. Does nothing for [AcceptedDataPointSource.Original].
         *
         * @param dataPoint The data point judgement entity to process.
         * @param companyId The company ID to use when uploading the replacement data point.
         * @param reportingPeriod The reporting period to use when uploading the replacement data point.
         */
        private fun uploadReplacementDataPointIfNeeded(
            dataPoint: DataPointJudgementEntity,
            companyId: UUID,
            reportingPeriod: String,
        ) {
            val replacementValue =
                when (dataPoint.acceptedSource) {
                    AcceptedDataPointSource.Qa -> getReplacementValueFromQaReport(dataPoint)
                    AcceptedDataPointSource.Custom ->
                        dataPoint.customValue ?: throw InvalidInputApiException(
                            summary = "Custom value is missing.",
                            message =
                                "Data point ${dataPoint.dataPointId} has acceptedSource=Custom " +
                                    "but no custom value is set.",
                        )
                    else -> return
                }

            dataPointControllerApi.postDataPoint(
                uploadedDataPoint =
                    UploadedDataPoint(
                        dataPoint = replacementValue,
                        dataPointType = dataPoint.dataPointType,
                        companyId = companyId.toString(),
                        reportingPeriod = reportingPeriod,
                    ),
                bypassQa = true,
            )
        }

        /**
         * Retrieves the corrected data value from the accepted QA report of the given data point.
         *
         * @param dataPoint The data point judgement entity whose accepted QA report to look up.
         * @return The corrected data string from the accepted QA report.
         * @throws InvalidInputApiException If no matching QA report is found or it has no corrected data.
         */
        private fun getReplacementValueFromQaReport(dataPoint: DataPointJudgementEntity): String {
            val acceptedReport =
                dataPoint.qaReports.find {
                    it.reporterUserId == dataPoint.reporterUserIdOfAcceptedQaReport?.toString()
                } ?: throw InvalidInputApiException(
                    summary = "Accepted QA report not found.",
                    message =
                        "No QA report from user ${dataPoint.reporterUserIdOfAcceptedQaReport} " +
                            "found for data point ${dataPoint.dataPointId}.",
                )
            return acceptedReport.correctedData ?: throw InvalidInputApiException(
                summary = "QA report has no corrected data.",
                message =
                    "The accepted QA report for data point ${dataPoint.dataPointId} " +
                        "contains no corrected data to upload.",
            )
        }

        /**
         * Builds a [ReviewDataPointTask] for the given data point, mapping its [AcceptedDataPointSource]
         * to the appropriate [QaStatus].
         *
         * @param dataPoint The data point judgement entity to build the task for.
         * @param triggeringUserId The user ID triggering the QA status change.
         * @param correlationId The correlation ID for tracing.
         * @param timestamp The timestamp of the operation.
         * @return A [ReviewDataPointTask] ready to be submitted.
         * @throws InvalidInputApiException If the data point has no accepted source set.
         */
        private fun buildReviewTask(
            dataPoint: DataPointJudgementEntity,
            triggeringUserId: String,
            correlationId: String,
            timestamp: Long,
        ): ReviewDataPointTask {
            val qaStatus =
                when (dataPoint.acceptedSource) {
                    AcceptedDataPointSource.Original -> QaStatus.Accepted
                    AcceptedDataPointSource.Qa,
                    AcceptedDataPointSource.Custom,
                    -> QaStatus.Rejected
                    null ->
                        throw InvalidInputApiException(
                            summary = "Data point has no accepted source.",
                            message =
                                "Data point ${dataPoint.dataPointId} of type ${dataPoint.dataPointType} " +
                                    "has no accepted source set.",
                        )
                }
            return ReviewDataPointTask(
                dataPointId = dataPoint.dataPointId,
                qaStatus = qaStatus,
                triggeringUserId = triggeringUserId,
                comment = null,
                correlationId = correlationId,
                timestamp = timestamp,
            )
        }
    }

package org.dataland.datalandqaservice.controller

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.api.QaApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataPointQaReviewInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.QaReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DataPointQaReviewManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DataPointQaReviewManager.ReviewDataPointTask
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReviewManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.DataPointQaReviewItemFilter
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID
import java.util.UUID.randomUUID

/**
 * Controller for the QA service API
 */
@RestController
class QaController(
    @Autowired var qaReviewManager: QaReviewManager,
    @Autowired var dataPointQaReviewManager: DataPointQaReviewManager,
) : QaApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getInfoOnDatasets(
        dataTypes: Set<DataTypeEnum>?,
        reportingPeriods: Set<String>?,
        companyName: String?,
        qaStatus: QaStatus,
        chunkSize: Int,
        chunkIndex: Int,
    ): ResponseEntity<List<QaReviewResponse>> {
        logger.info("Received request to respond with information about pending datasets")
        return ResponseEntity.ok(
            qaReviewManager
                .getInfoOnDatasets(
                    dataTypes = dataTypes,
                    reportingPeriods = reportingPeriods,
                    companyName = companyName,
                    qaStatus = qaStatus,
                    chunkSize = chunkSize,
                    chunkIndex = chunkIndex,
                ),
        )
    }

    override fun getQaReviewResponseByDataId(dataId: UUID): ResponseEntity<QaReviewResponse> {
        logger.info(
            "Received request to respond with the review information " +
                "of the dataset with identifier $dataId",
        )

        val datasetQaReviewResponse =
            qaReviewManager.getQaReviewResponseByDataId(dataId)
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(datasetQaReviewResponse)
    }

    override fun changeQaStatus(
        dataId: String,
        qaStatus: QaStatus,
        comment: String?,
        overwriteDataPointQaStatus: Boolean,
    ) {
        qaReviewManager.assertQaServiceKnowsDataId(dataId)

        val correlationId = randomUUID().toString()
        val reviewerId = DatalandAuthentication.fromContext().userId
        logger.info(
            "Received request from user $reviewerId to change the quality status of dataset with ID $dataId " +
                "(correlationId: $correlationId)",
        )

        val qaReviewEntity =
            qaReviewManager.saveQaReviewEntity(
                dataId = dataId,
                qaStatus = qaStatus,
                triggeringUserId = reviewerId,
                comment = comment,
                correlationId = correlationId,
            )
        dataPointQaReviewManager.reviewAssembledDataset(
            dataId = dataId,
            qaStatus = qaStatus,
            triggeringUserId = reviewerId,
            comment = comment,
            correlationId = correlationId,
            overwriteDataPointQaStatus = overwriteDataPointQaStatus,
        )
        qaReviewManager.sendQaStatusUpdateMessage(
            qaReviewEntity = qaReviewEntity, correlationId = correlationId,
        )
    }

    /**
     * Retrieves the number of unreviewed datasets specified by certain query parameter
     * @param dataTypes the set of datatypes by which to filter
     * @param reportingPeriods the set of reportingPeriods by which to filter
     * @param companyName the companyName by which to filter
     */
    override fun getNumberOfPendingDatasets(
        dataTypes: Set<DataTypeEnum>?,
        reportingPeriods: Set<String>?,
        companyName: String?,
    ): ResponseEntity<Int> {
        logger.info("Received request to respond with number of pending datasets")

        return ResponseEntity.ok(
            qaReviewManager.getNumberOfPendingDatasets(dataTypes, reportingPeriods, companyName),
        )
    }

    override fun getDataPointQaReviewInformationByDataId(dataPointId: String): ResponseEntity<List<DataPointQaReviewInformation>> {
        logger.info("Received request to retrieve the review information of the dataset with identifier $dataPointId")
        return ResponseEntity.ok(dataPointQaReviewManager.getDataPointQaReviewInformationByDataId(dataPointId))
    }

    override fun getDataPointReviewQueue(): ResponseEntity<List<DataPointQaReviewInformation>> {
        logger.info("Received request to retrieve the review queue")
        return ResponseEntity.ok(dataPointQaReviewManager.getDataPointQaReviewQueue())
    }

    override fun changeDataPointQaStatus(
        dataPointId: String,
        qaStatus: QaStatus,
        comment: String?,
    ) {
        dataPointQaReviewManager.assertQaServiceKnowsDataPointId(dataPointId)
        val correlationId = randomUUID().toString()
        val reviewerId = DatalandAuthentication.fromContext().userId
        logger.info(
            "Received request to change the QA status of the data point $dataPointId to $qaStatus " +
                "from user $reviewerId (correlationId: $correlationId)",
        )
        val qaTask =
            ReviewDataPointTask(
                dataPointId = dataPointId,
                qaStatus = qaStatus,
                triggeringUserId = reviewerId,
                comment = comment,
                correlationId = correlationId,
                timestamp = Instant.now().toEpochMilli(),
            )
        dataPointQaReviewManager.reviewDataPoints(listOf(qaTask))
    }

    override fun getDataPointQaReviewInformation(
        companyId: String?,
        dataType: String?,
        reportingPeriod: String?,
        qaStatus: QaStatus?,
        showOnlyActive: Boolean,
        chunkSize: Int,
        chunkIndex: Int,
    ): ResponseEntity<List<DataPointQaReviewInformation>> {
        logger.info("Received request to retrieve the review information of the data point with identifier $companyId")
        val offset = chunkSize * chunkIndex
        val searchFilter =
            DataPointQaReviewItemFilter(
                companyId = companyId,
                dataTypeList = dataType?.let { listOf(it) },
                reportingPeriod = reportingPeriod,
                qaStatus = qaStatus,
            )

        return ResponseEntity.ok(
            dataPointQaReviewManager.getFilteredDataPointQaReviewInformation(
                searchFilter = searchFilter,
                showOnlyActive = showOnlyActive,
                chunkSize = chunkSize,
                offset = offset,
            ),
        )
    }
}

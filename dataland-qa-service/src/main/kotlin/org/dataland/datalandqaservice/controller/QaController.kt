package org.dataland.datalandqaservice.controller

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.api.QaApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataPointQaReviewInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.QaReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DataPointQaReviewManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReviewManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.DataPointQaReviewItemFilter
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
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

    override fun getInfoOnPendingDatasets(
        dataTypes: Set<DataTypeEnum>?,
        reportingPeriods: Set<String>?,
        companyName: String?,
        chunkSize: Int,
        chunkIndex: Int,
    ): ResponseEntity<List<QaReviewResponse>> {
        logger.info("Received request to respond with information about pending datasets")
        return ResponseEntity.ok(
            qaReviewManager
                .getInfoOnPendingDatasets(
                    dataTypes = dataTypes,
                    reportingPeriods = reportingPeriods,
                    companyName = companyName,
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
    ) {
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

        qaReviewManager.sendQaStatusChangeMessage(
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

    override fun getDataPointQaReviewInformationByDataId(dataId: UUID): ResponseEntity<List<DataPointQaReviewInformation>> {
        logger.info("Received request to retrieve the review information of the dataset with identifier $dataId")
        return ResponseEntity.ok(dataPointQaReviewManager.getDataPointQaReviewInformationByDataId(dataId.toString()))
    }

    override fun getDataPointReviewQueue(): ResponseEntity<List<DataPointQaReviewInformation>> {
        logger.info("Received request to retrieve the review queue")
        return ResponseEntity.ok(dataPointQaReviewManager.getDataPointQaReviewQueue())
    }

    override fun changeDataPointQaStatus(
        dataId: String,
        qaStatus: QaStatus,
        comment: String?,
    ) {
        val correlationId = randomUUID().toString()
        val reviewerId = DatalandAuthentication.fromContext().userId
        logger.info(
            "Received request to change the QA status of the data point $dataId to $qaStatus " +
                "from user $reviewerId (correlationId: $correlationId)",
        )
        val dataPointQaReviewEntity =
            dataPointQaReviewManager.saveDataPointQaReviewEntity(
                dataId, qaStatus, reviewerId, comment, correlationId,
            )
        dataPointQaReviewManager.sendDataPointQaStatusChangeMessage(dataPointQaReviewEntity, correlationId)
    }

    override fun getDataPointQaReviewInformation(
        companyId: String?,
        dataPointIdentifier: String?,
        reportingPeriod: String?,
        qaStatus: QaStatus?,
        onlyLatest: Boolean?,
        chunkSize: Int?,
        chunkIndex: Int?,
    ): ResponseEntity<List<DataPointQaReviewInformation>> {
        logger.info("Received request to retrieve the review information of the data point with identifier $companyId")
        val searchFilter =
            DataPointQaReviewItemFilter(
                companyId = companyId,
                dataPointIdentifier = dataPointIdentifier,
                reportingPeriod = reportingPeriod,
                qaStatus = qaStatus?.toString(),
            )
        return ResponseEntity.ok(
            dataPointQaReviewManager.getFilteredDataPointQaReviewInformation(
                searchFilter = searchFilter,
                onlyLatest = onlyLatest,
                chunkSize = chunkSize,
                chunkIndex = chunkIndex,
            ),
        )
    }
}

package org.dataland.datalandqaservice.controller

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.api.QaApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.QaReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReviewManager
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import java.util.UUID.randomUUID

/**
 * Controller for the QA service API
 */
@RestController
class QaController(
    @Autowired var qaReviewManager: QaReviewManager,
) : QaApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun getInfoOnPendingDatasets(
        dataTypes: Set<DataTypeEnum>?,
        reportingPeriods: Set<String>?,
        companyName: String?,
        chunkSize: Int,
        chunkIndex: Int,
    ): ResponseEntity<List<QaReviewResponse>> {
        logger.info("Received request to respond with information about pending datasets")
        return ResponseEntity.ok(
            qaReviewManager.getInfoOnPendingDatasets(
                dataTypes = dataTypes, reportingPeriods = reportingPeriods,
                companyName = companyName, chunkSize = chunkSize, chunkIndex = chunkIndex,
            ),
        )
    }

    @Transactional
    override fun getQaReviewsByDataId(dataId: UUID): ResponseEntity<QaReviewResponse> {
        logger.info(
            "Received request to respond with the review information " +
                "of the dataset with identifier $dataId",
        )

        val userIsAdmin = DatalandAuthentication.fromContext().roles.contains(DatalandRealmRole.ROLE_ADMIN)
        val datasetQaReviewResponse =
            qaReviewManager.getQaReviewResponseByDataId(dataId, userIsAdmin)
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(datasetQaReviewResponse)
    }

    @Transactional
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

        qaReviewManager.saveQaReviewEntityAndSendQaStatusChangeMessage(
            dataId = dataId,
            qaStatus = qaStatus,
            reviewerId = reviewerId,
            comment = comment,
            correlationId = correlationId,
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
            qaReviewManager.getNumberOfPendingDatasets(
                dataTypes = dataTypes,
                reportingPeriods = reportingPeriods, companyName = companyName,
            ),
        )
    }
}

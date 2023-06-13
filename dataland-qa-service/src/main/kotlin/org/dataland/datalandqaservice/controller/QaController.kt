package org.dataland.datalandqaservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandqaservice.api.QaApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.ReviewInformationEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.ReviewQueueEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.ReviewHistoryRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.ReviewQueueRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RestController
import java.util.UUID.randomUUID
import kotlin.jvm.optionals.getOrElse

/**
 * Controller for the QA service API
 */
@RestController
class QaController(
    @Autowired val reviewQueueRepository: ReviewQueueRepository,
    @Autowired val reviewHistoryRepository: ReviewHistoryRepository,
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var objectMapper: ObjectMapper,
) : QaApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun getUnreviewedDatasetsIds(): ResponseEntity<List<String>> {
        logger.info("Received request to respond with IDs of unreviewed datasets")
        return ResponseEntity.ok(reviewQueueRepository.getSortedPendingDataIds())
    }

    @Transactional
    override fun assignQualityStatus(dataId: String, qualityStatus: QaStatus) {
        val correlationId = randomUUID().toString()
        logger.info(
            "Received request to change the quality status of dataset with ID $dataId " +
                "(correlationId: $correlationId)",
        )
        if (qualityStatus == QaStatus.Pending) {
            throw InvalidInputApiException(
                "Quality \"Pending\" cannot be assigned to a reviewed dataset",
                "Quality \"Pending\" cannot be assigned to a reviewed dataset",
            )
        }
        val dataReviewStatusToUpdate = validateDataIdAndGetDataReviewStatus(dataId)
        logger.info("Assigning quality status ${qualityStatus.name} to dataset with ID $dataId")
        reviewHistoryRepository.save(
            ReviewInformationEntity(
                dataId = dataId,
                receptionTime = dataReviewStatusToUpdate.receptionTime,
                qaStatus = qualityStatus,
                reviewerKeycloakId = DatalandAuthentication.fromContext().userId,
            ),
        )
        reviewQueueRepository.deleteById(dataId)
        sendQaCompletedMessage(dataId, qualityStatus, correlationId)
    }

    /**
     * Validates that a dataset corresponding to a data ID needs to be reviewed
     * @param dataId the ID of the data to validate
     * @returns the ReviewQueueEntity corresponding the dataId
     */
    fun validateDataIdAndGetDataReviewStatus(dataId: String): ReviewQueueEntity {
        return reviewQueueRepository.findById(dataId).getOrElse {
            throw InvalidInputApiException(
                "There is no reviewable dataset with ID $dataId.",
                "There is no reviewable dataset with ID $dataId.",
            )
        }
    }

    /**
     * Sends the QA completed message
     * @param dataId the ID of the QAed dataset
     * @param qualityStatus the assigned quality status
     * @param correlationId the ID of the process
     */
    fun sendQaCompletedMessage(dataId: String, qualityStatus: QaStatus, correlationId: String) {
        val message = objectMapper.writeValueAsString(
            QaCompletedMessage(dataId, qualityStatus),
        )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            message, MessageType.QaCompleted, correlationId, ExchangeName.DataQualityAssured,
            RoutingKeyNames.data,
        )
    }
}

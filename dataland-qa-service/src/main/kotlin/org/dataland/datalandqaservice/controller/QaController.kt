package org.dataland.datalandqaservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.QAStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeNames
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandqaservice.api.QaApi
import org.dataland.datalandqaservice.entities.DatasetReviewStatusEntity
import org.dataland.datalandqaservice.repositories.DatasetReviewStatusRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RestController
import kotlin.jvm.optionals.getOrElse

/**
 * Controller for the QA service API
 */
@RestController
class QaController(
    @Autowired val datasetReviewStatusRepository: DatasetReviewStatusRepository,
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var objectMapper: ObjectMapper,
) : QaApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun getUnreviewedDatasets(): ResponseEntity<List<String>> {
        logger.info("Received request to respond with IDs of unreviewed datasets")
        return ResponseEntity.ok(datasetReviewStatusRepository.getSortedPendingDataIds())
    }

    @Transactional
    override fun assignQualityStatus(dataId: String, qualityStatus: QAStatus):
        ResponseEntity<Void> {
        logger.info("Assigning quality status ${qualityStatus.name} to dataset with ID $dataId")
        if (qualityStatus == QAStatus.Pending) {
            throw InvalidInputApiException(
                "Quality \"Pending\" cannot be assigned to a reviewed dataset",
                "Quality \"Pending\" cannot be assigned to a reviewed dataset",
            )
        }
        val dataReviewStatusToUpdate = validateDataIdAndGetDataReviewStatus(dataId)
        datasetReviewStatusRepository.save(
            DatasetReviewStatusEntity(
                dataId = dataId,
                correlationId = dataReviewStatusToUpdate.correlationId,
                qaStatus = qualityStatus,
                receptionTime = dataReviewStatusToUpdate.receptionTime,
                reviewerKeycloakId = DatalandAuthentication.fromContext().userId,
            ),
        )
        sendQaCompletedMessage(dataId, qualityStatus, dataReviewStatusToUpdate.correlationId)

        return ResponseEntity.ok(null)
    }

    /**
     * Validates that a dataset corresponding to a data ID needs to be reviewed
     * @param dataId the ID of the data to validate
     * @returns the DatasetReviewQuality corresponding
     */
    fun validateDataIdAndGetDataReviewStatus(dataId: String): DatasetReviewStatusEntity {
        return datasetReviewStatusRepository.findById(dataId).getOrElse {
            throw InvalidInputApiException(
                "There is no dataset with ID $dataId.",
                "There is no dataset with ID $dataId.",
            )
        }.also {
            if (it.qaStatus != QAStatus.Pending) {
                throw InvalidInputApiException(
                    "The dataset has already been reviewed.",
                    "The dataset with ID $dataId already has the " +
                        "quality status ${it.qaStatus.name}.",
                )
            }
        }
    }

    /**
     * Sends the QA completed message
     * @param dataId the ID of the QAed dataset
     * @param qualityStatus the assigned quality status
     * @param correlationId the ID of the process
     */
    fun sendQaCompletedMessage(dataId: String, qualityStatus: QAStatus, correlationId: String) {
        val message = objectMapper.writeValueAsString(
            QaCompletedMessage(dataId, qualityStatus),
        )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            message, MessageType.QACompleted, correlationId, ExchangeNames.dataQualityAssured,
            RoutingKeyNames.data,
        )
    }
}

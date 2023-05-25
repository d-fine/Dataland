package org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.QAStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeNames
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.api.QaApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewStatusEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetReviewStatusRepository
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
        val allDatasetReviewStati = datasetReviewStatusRepository.findAll()
        val pendingDatasetReviewStati = allDatasetReviewStati.filter { it.qaStatus == QAStatus.Pending }
        return ResponseEntity.ok(
            pendingDatasetReviewStati
                .sortedBy { it.receptionTime }
                .map { it.dataId },
        )
        // TODO move this stuff into a database query
    }

    @Transactional
    override fun assignQualityStatus(dataId: String, qualityStatus: QAStatus): ResponseEntity<Nothing> {
        logger.info("Assigning quality status ${qualityStatus.name} to dataset with ID $dataId")
        if (qualityStatus == QAStatus.Pending) {
            throw InvalidInputApiException(
                "Quality \"Pending\" cannot be assigned to a reviewed dataset",
                "Quality \"Pending\" cannot be assigned to a reviewed dataset",
            )
        }
        val dataReviewStatusToUpdate = datasetReviewStatusRepository.findById(dataId).getOrElse {
            throw InvalidInputApiException(
                "There is no dataset is no dataset with ID $dataId.",
                "There is no dataset is no dataset with ID $dataId to be reviewed.",
            )
        }
        if (dataReviewStatusToUpdate.qaStatus != QAStatus.Pending) {
            throw InvalidInputApiException(
                "The dataset has already been reviewed.",
                "The dataset with ID $dataId already has the " +
                    "quality status ${dataReviewStatusToUpdate.qaStatus.name}.",
            )
        }
        // TODO move all of the above into a database query
        datasetReviewStatusRepository.save(
            DatasetReviewStatusEntity(
                dataId = dataId,
                correlationId = dataReviewStatusToUpdate.correlationId,
                qaStatus = qualityStatus,
                receptionTime = dataReviewStatusToUpdate.receptionTime,
                reviewerKeycloakId = DatalandAuthentication.fromContext().userId,
            ),
        )

        val message = objectMapper.writeValueAsString(
            QaCompletedMessage(dataId, qualityStatus),
        )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            message, MessageType.QACompleted, dataReviewStatusToUpdate.correlationId, ExchangeNames.dataQualityAssured,
            RoutingKeyNames.data,
        )

        return ResponseEntity.ok(null)
    }
}

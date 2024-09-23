package org.dataland.datalandqaservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
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
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.ReviewInformationResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.ReviewQueueResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.ReviewHistoryRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.ReviewQueueRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReviewManager
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RestController
import java.util.*
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
    @Autowired var qaReviewManager: QaReviewManager,
) : QaApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun getInfoOnUnreviewedDatasets(
        dataTypes: Set<DataTypeEnum>?,
        reportingPeriods: Set<String>?,
        companyName: String?,
        chunkSize: Int,
        chunkIndex: Int,
    ): ResponseEntity<List<ReviewQueueResponse>> {
        logger.info("Received request to respond with information about unreviewed datasets")
        return ResponseEntity.ok(
            qaReviewManager.getInfoOnUnreviewedDatasets(
                dataTypes = dataTypes, reportingPeriods = reportingPeriods,
                companyName = companyName, chunkSize = chunkSize, chunkIndex = chunkIndex,
            ),
        )
    }

    @Transactional
    override fun getDatasetById(dataId: UUID): ResponseEntity<ReviewInformationResponse> {
        val identifier = dataId.toString()
        logger.info(
            "Received request to respond with the review information " +
                "of the dataset with identifier $identifier",
        )

        val reviewHistoryEntity = reviewHistoryRepository.findById(identifier).orElse(null)

        return if (reviewHistoryEntity != null) {
            val userIsAdmin = DatalandAuthentication.fromContext().roles.contains(DatalandRealmRole.ROLE_ADMIN)
            val response = reviewHistoryEntity.toReviewInformationResponse(userIsAdmin)
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @Transactional
    override fun assignQaStatus(dataId: String, qaStatus: QaStatus, message: String?) {
        val correlationId = randomUUID().toString()
        logger.info(
            "Received request to change the quality status of dataset with ID $dataId " +
                "(correlationId: $correlationId)",
        )
        if (qaStatus == QaStatus.Pending) {
            throw InvalidInputApiException(
                "Quality \"Pending\" cannot be assigned to a reviewed dataset",
                "Quality \"Pending\" cannot be assigned to a reviewed dataset",
            )
        }
        val dataReviewStatusToUpdate = validateDataIdAndGetDataReviewStatus(dataId)
        logger.info("Assigning quality status ${qaStatus.name} to dataset with ID $dataId")
        reviewHistoryRepository.save(
            ReviewInformationEntity(
                dataId = dataId,
                receptionTime = dataReviewStatusToUpdate.receptionTime,
                qaStatus = qaStatus,
                reviewerKeycloakId = DatalandAuthentication.fromContext().userId,
                message = message,
            ),
        )
        reviewQueueRepository.deleteById(dataId)
        sendQaCompletedMessage(dataId, qaStatus, correlationId, message)
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
     * @param qaStatus the assigned quality status
     * @param correlationId the ID of the process
     * @param message optional message attached to the QA completion
     */
    fun sendQaCompletedMessage(dataId: String, qaStatus: QaStatus, correlationId: String, message: String?) {
        val reviewerId = SecurityContextHolder.getContext().authentication.name
        val messageBody = objectMapper.writeValueAsString(
            QaCompletedMessage(dataId, qaStatus, reviewerId, message),
        )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            messageBody, MessageType.QaCompleted, correlationId, ExchangeName.DataQualityAssured,
            RoutingKeyNames.data,
        )
    }

    /**
     * Retrieves the number of unreviewed datasets specified by certain query parameter
     * @param dataType the set of datatypes for which should be filtered
     * @param reportingPeriod the set of reportingPeriods for which should be filtered
     * @param companyName the companyName for which should be filtered
     */
    override fun getNumberOfUnreviewedDatasets(
        dataTypes: Set<DataTypeEnum>?,
        reportingPeriods: Set<String>?,
        companyName: String?,
    ): ResponseEntity<Int> {
        logger.info("Received request to respond with number of unreviewed datasets")

        return ResponseEntity.ok(
            qaReviewManager.getNumberOfUnreviewedDatasets(
                dataTypes = dataTypes,
                reportingPeriods = reportingPeriods, companyName = companyName,
            ),
        )
    }
}

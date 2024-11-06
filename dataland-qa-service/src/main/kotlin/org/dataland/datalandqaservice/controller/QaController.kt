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
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetQaReviewLogEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetQaReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.toDatasetQaReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReviewManager
import org.dataland.datalandqaservice.repositories.DatasetQaReviewRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import java.util.UUID.randomUUID

/**
 * Controller for the QA service API
 */
@RestController
class QaController(
    @Autowired var datasetQaReviewRepository: DatasetQaReviewRepository,
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
    ): ResponseEntity<List<DatasetQaReviewResponse>> {
        logger.info("Received request to respond with information about unreviewed datasets")
        return ResponseEntity.ok(
            qaReviewManager.getInfoOnUnreviewedDatasets(
                dataTypes = dataTypes, reportingPeriods = reportingPeriods,
                companyName = companyName, chunkSize = chunkSize, chunkIndex = chunkIndex,
            ),
        )
    }

    @Transactional
    override fun getQaReviewEventsByDataId(dataId: UUID): ResponseEntity<DatasetQaReviewResponse> {
        logger.info(
            "Received request to respond with the review information " +
                "of the dataset with identifier $dataId",
        )

        val datasetQaReviewLogEntity = datasetQaReviewRepository.findByDataId(dataId.toString())

        return if (datasetQaReviewLogEntity != null) {
            val userIsAdmin = DatalandAuthentication.fromContext().roles.contains(DatalandRealmRole.ROLE_ADMIN)
            val response = datasetQaReviewLogEntity.toDatasetQaReviewResponse(userIsAdmin)
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @Transactional
    override fun assignQaStatus(
        dataId: String,
        qaStatus: QaStatus,
        comment: String?,
    ) {
        val correlationId = randomUUID().toString()
        logger.info(
            "Received request to change the quality status of dataset with ID $dataId " +
                "(correlationId: $correlationId)",
        )
        val datasetQaReviewLogEntry = validateDataIdAndGetDataReviewStatus(dataId)
        logger.info("Assigning quality status ${qaStatus.name} to dataset with ID $dataId")
        datasetQaReviewRepository.save(
            DatasetQaReviewLogEntity(
                dataId = dataId,
                companyId = datasetQaReviewLogEntry.companyId,
                companyName = datasetQaReviewLogEntry.companyName,
                dataType = datasetQaReviewLogEntry.dataType,
                reportingPeriod = datasetQaReviewLogEntry.reportingPeriod,
                timestamp = datasetQaReviewLogEntry.timestamp,
                qaStatus = qaStatus,
                reviewerId = DatalandAuthentication.fromContext().userId,
                comment = comment,
            ),
        )
        sendQaUpdateMessage(
            changedDataId = dataId,
            newQaStatus = qaStatus,
            newActiveDataId = activeDataId,
            correlationId = correlationId,
            comment = comment,
        )
    }

    /**
     * Validates that a dataset corresponding to a data ID needs to be reviewed
     * @param dataId the ID of the data to validate
     * @returns the ReviewQueueEntity corresponding the dataId
     */
    fun validateDataIdAndGetDataReviewStatus(dataId: String): DatasetQaReviewLogEntity =
        datasetQaReviewRepository.findByDataId(dataId)
            ?: throw InvalidInputApiException(
                "There is no reviewable dataset with ID $dataId.",
                "There is no reviewable dataset with ID $dataId.",
            )

    /**
     * Sends the QA completed message
     * @param dataId the ID of the QAed dataset
     * @param qaStatus the assigned quality status
     * @param correlationId the ID of the process
     * @param message optional message attached to the QA completion
     */
    fun sendQaUpdateMessage(
        dataId: String,
        qaStatus: QaStatus,
        correlationId: String,
        message: String?,
    ) {
        val reviewerId = SecurityContextHolder.getContext().authentication.name
        val messageBody =
            objectMapper.writeValueAsString(
                QaCompletedMessage(dataId, qaStatus, reviewerId, message),
            )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            messageBody, MessageType.QA_COMPLETED, correlationId, ExchangeName.DATA_QUALITY_ASSURED,
            RoutingKeyNames.DATA,
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

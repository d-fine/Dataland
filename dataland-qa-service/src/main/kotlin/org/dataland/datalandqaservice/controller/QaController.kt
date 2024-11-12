package org.dataland.datalandqaservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandqaservice.api.QaApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.QaReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.toDatasetQaReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReviewManager
import org.dataland.datalandqaservice.repositories.QaReviewRepository
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
    @Autowired var datasetQaReviewRepository: QaReviewRepository,
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var objectMapper: ObjectMapper,
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
        val datasetQaReviewLogEntry = validateDataIdAndGetDataReviewStatus(dataId)
        logger.info("Assigning quality status ${qaStatus.name} to dataset with ID $dataId")
        datasetQaReviewRepository.save(
            QaReviewEntity(
                dataId = dataId,
                companyId = datasetQaReviewLogEntry.companyId,
                companyName = datasetQaReviewLogEntry.companyName,
                dataType = datasetQaReviewLogEntry.dataType,
                reportingPeriod = datasetQaReviewLogEntry.reportingPeriod,
                timestamp = datasetQaReviewLogEntry.timestamp,
                qaStatus = qaStatus,
                reviewerId = reviewerId,
                comment = comment,
            ),
        )

        val qaStatusChangeMessage =
            QaStatusChangeMessage(
                changedQaStatusDataId = dataId,
                updatedQaStatus = qaStatus,
                currentlyActiveDataId =
                    getDataIdOfCurrentlyActiveDataset(
                        datasetQaReviewLogEntry.companyId,
                        datasetQaReviewLogEntry.dataType,
                        datasetQaReviewLogEntry.reportingPeriod,
                    ),
            )

        sendQaStatusChangeMessage(
            qaStatusChangeMessage = qaStatusChangeMessage,
            correlationId = correlationId,
        )
    }

    /**
     * Retrieve dataId of currently active dataset for same triple (companyId, dataType, reportingPeriod)
     * @param companyId
     * @param dataType
     * @param reportingPeriod
     * @return Returns the dataId of the active dataset, or an empty string if no active dataset can be found
     */
    private fun getDataIdOfCurrentlyActiveDataset(
        companyId: String,
        dataType: DataTypeEnum,
        reportingPeriod: String,
    ): String =
        datasetQaReviewRepository
            .findByCompanyIdAndDataTypeAndReportingPeriod(companyId, dataType, reportingPeriod)
            ?.filter { it.qaStatus == QaStatus.Accepted }
            ?.maxByOrNull { it.timestamp }
            ?.dataId ?: ""

    /**
     * Validates that a dataset corresponding to a data ID needs to be reviewed
     * @param dataId the ID of the data to validate
     * @returns the ReviewQueueEntity corresponding the dataId
     */
    fun validateDataIdAndGetDataReviewStatus(dataId: String): QaReviewEntity =
        datasetQaReviewRepository.findByDataId(dataId)
            ?: throw InvalidInputApiException(
                "There is no reviewable dataset with ID $dataId.",
                "There is no reviewable dataset with ID $dataId.",
            )

    /**
     * Sends the QA Status Change Message to MessageQueue
     * @param qaStatusChangeMessage QAStatusChangeMessage containing the dataId of the changed data set, the new QA
     * status and the dataId of the newly active dataset
     * @param correlationId the ID of the process
     */
    fun sendQaStatusChangeMessage(
        qaStatusChangeMessage: QaStatusChangeMessage,
        correlationId: String,
    ) {
        logger.info("Send QA status change message to messageQueue.")
        val messageBody = objectMapper.writeValueAsString(qaStatusChangeMessage)

        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            messageBody, MessageType.QA_STATUS_CHANGED, correlationId, ExchangeName.DATA_QUALITY_ASSURED,
            RoutingKeyNames.DATA,
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
        logger.info("Received request to respond with number of unreviewed datasets")

        return ResponseEntity.ok(
            qaReviewManager.getNumberOfPendingDatasets(
                dataTypes = dataTypes,
                reportingPeriods = reportingPeriods, companyName = companyName,
            ),
        )
    }
}

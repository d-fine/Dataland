package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.GetDataRequestsSearchFilter
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * This service checks if freshly uploaded and validated data answers a data request
 */
@Service("DataRequestUpdater")
class DataRequestUpdater(
    @Autowired private val messageUtils: MessageQueueUtils,
    @Autowired private val metaDataControllerApi: MetaDataControllerApi,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val companyDataControllerApi: CompanyDataControllerApi,
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
) {
    private val logger = LoggerFactory.getLogger(SingleDataRequestManager::class.java)

    /**
     * Checks if for a given dataset there are open requests with matching company identifier, reporting period
     * and data type and sets their status to answered
     * @param jsonString the message describing the result of the completed QA process
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value = Queue(
                    "dataQualityAssuredCommunityManagerDataManager",
                    arguments = [
                        Argument(name = "x-dead-letter-exchange", value = ExchangeName.DeadLetter),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
                exchange = Exchange(ExchangeName.DataQualityAssured, declare = "false"),
                key = [RoutingKeyNames.data],
            ),
        ],
    )
    @Transactional
    fun changeRequestStatusAfterUpload(
        @Payload jsonString: String,
        @Header(MessageHeaderKey.Type) type: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
    ) {
        messageUtils.validateMessageType(type, MessageType.QaCompleted)
        val qaCompletedMessage = objectMapper.readValue(jsonString, QaCompletedMessage::class.java)
        val dataId = qaCompletedMessage.identifier
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty")
        }
        logger.info("Received data QA completed message for dataset with ID $dataId")
        if (qaCompletedMessage.validationResult != QaStatus.Accepted) {
            logger.info("Dataset with ID $dataId was not accepted and request matching is cancelled")
            return
        }
        messageUtils.rejectMessageOnException {
            val metaData = metaDataControllerApi.getDataMetaInfo(dataId)
            val companyName = companyDataControllerApi.getCompanyInfo(metaData.companyId).companyName
            val dataRequestEntities = dataRequestRepository.searchDataRequestEntity(
                GetDataRequestsSearchFilter(
                    metaData.dataType.value, "", RequestStatus.Open, metaData.reportingPeriod, metaData.companyId,
                ),
            )
            dataRequestEntities.forEach {
                val properties = mapOf(
                    "companyId" to metaData.companyId,
                    "companyName" to companyName,
                    "dataType" to metaData.dataType.value,
                    "reportingPeriods" to metaData.reportingPeriod,
                    "creationTimeStamp" to Date(it.creationTimestamp).toString(),
                )
                // todo receiver from userId
                val message = TemplateEmailMessage(
                    emailTemplateType = TemplateEmailMessage.Type.DataRequestedAnswered,
                    receiver = "johannes.haerkoetter@d-fine.com",
                    properties = properties,
                )
                cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                    objectMapper.writeValueAsString(message),
                    MessageType.SendTemplateEmail,
                    correlationId,
                    ExchangeName.SendEmail,
                    RoutingKeyNames.templateEmail,
                )
            }
            dataRequestRepository.updateDataRequestEntitiesFromOpenToAnswered(
                metaData.companyId,
                metaData.reportingPeriod,
                metaData.dataType.value,
            )
            logger.info(
                "Changed Request Status for company Id ${metaData.companyId}, " +
                    "reporting period ${metaData.reportingPeriod} and framework ${metaData.dataType.name}",
            )
        }
    }
}

package org.dataland.datalandcommunitymanager.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.*
/**
 * Manage sending emails to user regarding data requests
 */
@Service("DataRequestEmailSender")
class DataRequestEmailSender(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
) {
    /**
     * Method to informs user by mail that his request is answered.
     * @param dataRequestEntity the dataRequestEntity
     * @param companyName name of the company
     * @param correlationId correlation Id
     */
    fun sendDataRequestedAnsweredEmail(
        dataRequestEntity: DataRequestEntity,
        companyName: String,
        correlationId: String = UUID.randomUUID().toString(),
    ) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm:ss")
        val creationTimestamp = dateFormat.format(dataRequestEntity.creationTimestamp)
        val dataTypeName = dataRequestEntity.dataType
        val properties = mapOf(
            "companyId" to dataRequestEntity.datalandCompanyId,
            "companyName" to companyName,
            "dataType" to dataRequestEntity.dataType,
            "reportingPeriods" to dataRequestEntity.reportingPeriod,
            "creationTimestamp" to creationTimestamp,
            "dataTypeName" to dataTypeName,
        )
        val userId = "byUserId@testmail.com" //todo userId -> user mail
        val message = TemplateEmailMessage(
            emailTemplateType = TemplateEmailMessage.Type.DataRequestedAnswered,
            receiver = "",
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
}

package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.TimeZone

/**
 * A class that provided utility for generating emails messages for data request responses
 */
@Service("DataRequestResponseEmailSender")
class DataRequestResponseEmailSender(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val companyDataControllerApi: CompanyDataControllerApi,
    @Value("\${dataland.community-manager.data-request.answered.stale-days-threshold}")
    private val staleDaysThreshold: String,
) {
    /**
     * Method to retrieve companyName by companyId
     * @param companyId dataland companyId
     * @returns companyName as string
     */
    private fun getCompanyNameById(companyId: String): String =
        companyDataControllerApi.getCompanyInfo(companyId).companyName.ifEmpty { companyId }

    /**
     * Method to convert unit time in ms to human-readable date
     * @param creationTimestamp unix time in ms
     * @returns human-readable date as string
     */
    private fun convertUnitTimeInMsToDate(creationTimestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm")
        dateFormat.timeZone = TimeZone.getTimeZone("Europe/Berlin")
        return dateFormat.format(creationTimestamp)
    }

    private fun getProperties(
        dataRequestEntity: DataRequestEntity,
        staleDaysThreshold: String,
    ): Map<String, String> =
        mapOf(
            "companyId" to dataRequestEntity.datalandCompanyId,
            "companyName" to getCompanyNameById(dataRequestEntity.datalandCompanyId),
            "dataType" to dataRequestEntity.dataType,
            "reportingPeriod" to dataRequestEntity.reportingPeriod,
            "creationDate" to convertUnitTimeInMsToDate(dataRequestEntity.creationTimestamp),
            "dataTypeDescription" to dataRequestEntity.getDataTypeDescription(),
            "dataRequestId" to dataRequestEntity.dataRequestId,
            "closedInDays" to staleDaysThreshold,
        )

    /**
     * Method to informs user by mail that his request is answered.
     * @param dataRequestEntity the dataRequestEntity
     * @param emailType the template email message type
     * @param correlationId the correlation id
     */
    fun sendDataRequestResponseEmail(
        dataRequestEntity: DataRequestEntity,
        emailType: TemplateEmailMessage.Type,
        correlationId: String,
    ) {
        val properties = getProperties(dataRequestEntity, staleDaysThreshold)
        val message =
            TemplateEmailMessage(
                emailTemplateType = emailType,
                receiver = TemplateEmailMessage.UserIdEmailRecipient(dataRequestEntity.userId),
                properties = properties,
            )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(message),
            MessageType.SEND_TEMPLATE_EMAIL,
            correlationId,
            ExchangeName.SEND_EMAIL,
            RoutingKeyNames.TEMPLATE_EMAIL,
        )
    }
}

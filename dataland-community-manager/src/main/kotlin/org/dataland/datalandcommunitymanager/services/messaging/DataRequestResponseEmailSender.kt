package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestAnswered
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestClosed
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestNonSourceable
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestUpdated
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.TimeZone
import kotlin.String

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

    /**
     * Method to inform the respective user by mail that his request is closed.
     * @param dataRequestEntity the dataRequestEntity
     * @param correlationId the correlation id
     */
    fun sendDataRequestClosedEmail(
        dataRequestEntity: DataRequestEntity,
        correlationId: String,
    ) {
        val dataRequestClosed =
            DataRequestClosed(
                companyName = getCompanyNameById(dataRequestEntity.datalandCompanyId),
                dataTypeLabel = dataRequestEntity.getDataTypeDescription(),
                reportingPeriod = dataRequestEntity.reportingPeriod,
                creationDate = convertUnitTimeInMsToDate(dataRequestEntity.creationTimestamp),
                dataRequestId = dataRequestEntity.dataRequestId,
                closedInDays = staleDaysThreshold.toInt(),
            )
        val message =
            EmailMessage(
                dataRequestClosed, listOf(EmailRecipient.UserId(dataRequestEntity.userId)), emptyList(), emptyList(),
            )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(message),
            MessageType.SEND_EMAIL,
            correlationId,
            ExchangeName.SEND_EMAIL,
            RoutingKeyNames.EMAIL,
        )
    }

    /**
     * Method to inform user by mail that his request is answered.
     * @param dataRequestEntity the dataRequestEntity
     * @param correlationId the correlation id
     */
    fun sendDataRequestAnsweredEmail(
        dataRequestEntity: DataRequestEntity,
        correlationId: String,
    ) {
        val dataRequestAnswered =
            DataRequestAnswered(
                companyName = getCompanyNameById(dataRequestEntity.datalandCompanyId),
                reportingPeriod = dataRequestEntity.reportingPeriod,
                dataTypeLabel = dataRequestEntity.getDataTypeDescription(),
                creationDate = convertUnitTimeInMsToDate(dataRequestEntity.creationTimestamp),
                dataRequestId = dataRequestEntity.dataRequestId,
                closedInDays = staleDaysThreshold.toInt(),
            )
        val message =
            EmailMessage(
                dataRequestAnswered, listOf(EmailRecipient.UserId(dataRequestEntity.userId)), emptyList(), emptyList(),
            )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(message),
            MessageType.SEND_EMAIL,
            correlationId,
            ExchangeName.SEND_EMAIL,
            RoutingKeyNames.EMAIL,
        )
    }

    /**
     * Method to inform user by mail that his request is non-sourceable.
     * @param dataRequestEntity the dataRequestEntity
     * @param correlationId the correlation id
     */
    fun sendDataRequestNonSourceableEmail(
        dataRequestEntity: DataRequestEntity,
        correlationId: String,
    ) {
        val dataRequestNonSourceableMail =
            DataRequestNonSourceable(
                companyName = getCompanyNameById(dataRequestEntity.datalandCompanyId),
                reportingPeriod = dataRequestEntity.reportingPeriod,
                dataTypeLabel = dataRequestEntity.getDataTypeDescription(),
                creationDate = convertUnitTimeInMsToDate(dataRequestEntity.creationTimestamp),
                dataRequestId = dataRequestEntity.dataRequestId,
                nonSourceableComment = dataRequestEntity.requestStatusChangeReason,
            )
        val message =
            EmailMessage(
                dataRequestNonSourceableMail, listOf(EmailRecipient.UserId(dataRequestEntity.userId)), emptyList(), emptyList(),
            )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(message),
            MessageType.SEND_EMAIL,
            correlationId,
            ExchangeName.SEND_EMAIL,
            RoutingKeyNames.EMAIL,
        )
    }

    /**
     * Function to send an e-mail notification to a user with a closed data request that there
     * has been a QA approval for a dataset with regard to the same company, reporting period and
     * framework.
     */
    fun sendEmailToUserWithClosedRequest(
        dataRequestEntity: DataRequestEntity,
        correlationId: String,
    ) {
        val dataRequestUpdatedMail =
            DataRequestUpdated(
                companyName = getCompanyNameById(dataRequestEntity.datalandCompanyId),
                reportingPeriod = dataRequestEntity.reportingPeriod,
                dataTypeLabel = dataRequestEntity.getDataTypeDescription(),
                creationDate = convertUnitTimeInMsToDate(dataRequestEntity.creationTimestamp),
                dataRequestId = dataRequestEntity.dataRequestId,
            )
        val message =
            EmailMessage(
                typedEmailContent = dataRequestUpdatedMail,
                receiver = listOf(EmailRecipient.UserId(dataRequestEntity.userId)),
                cc = emptyList(),
                bcc = emptyList(),
            )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body = objectMapper.writeValueAsString(message),
            type = MessageType.SEND_EMAIL,
            correlationId = correlationId,
            exchange = ExchangeName.SEND_EMAIL,
            routingKey = RoutingKeyNames.EMAIL,
        )
    }
}

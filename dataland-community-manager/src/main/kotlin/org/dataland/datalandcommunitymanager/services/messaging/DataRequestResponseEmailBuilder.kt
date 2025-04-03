package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.utils.CompanyInfoService
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.DataAvailableEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.DataNonSourceableEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.DataUpdatedEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.TimeZone

/**
 * A class that provided utility for generating emails messages for immediate data request responses
 */
@Service("DataRequestResponseEmailBuilder")
class DataRequestResponseEmailBuilder(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val companyInfoService: CompanyInfoService,
    @Autowired private val objectMapper: ObjectMapper,
    @Value("\${dataland.community-manager.data-request.answered.stale-days-threshold}")
    private val staleDaysThreshold: String,
) {
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
     * Method to inform user by mail that his request is answered.
     * @param dataRequestEntity the dataRequestEntity
     * @param correlationId the correlation id
     */
    fun buildDataRequestAnsweredEmailAndSendCEMessage(
        dataRequestEntity: DataRequestEntity,
        correlationId: String,
    ) {
        val dataAvailableEmailContent =
            DataAvailableEmailContent(
                companyName = companyInfoService.getValidCompanyNameOrId(dataRequestEntity.datalandCompanyId),
                reportingPeriod = dataRequestEntity.reportingPeriod,
                dataTypeLabel = dataRequestEntity.getDataTypeDescription(),
                creationDate = convertUnitTimeInMsToDate(dataRequestEntity.creationTimestamp),
                dataRequestId = dataRequestEntity.dataRequestId,
                closedInDays = staleDaysThreshold.toInt(),
            )
        val message =
            EmailMessage(
                dataAvailableEmailContent, listOf(EmailRecipient.UserId(dataRequestEntity.userId)), emptyList(), emptyList(),
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
    fun buildDataRequestNonSourceableEmailAndSendCEMessage(
        dataRequestEntity: DataRequestEntity,
        correlationId: String,
    ) {
        val dataNonSourceableEmailContentMail =
            DataNonSourceableEmailContent(
                companyName = companyInfoService.getValidCompanyNameOrId(dataRequestEntity.datalandCompanyId),
                reportingPeriod = dataRequestEntity.reportingPeriod,
                dataTypeLabel = dataRequestEntity.getDataTypeDescription(),
                creationDate = convertUnitTimeInMsToDate(dataRequestEntity.creationTimestamp),
                dataRequestId = dataRequestEntity.dataRequestId,
                nonSourceableComment = dataRequestEntity.requestStatusChangeReason,
            )
        val message =
            EmailMessage(
                dataNonSourceableEmailContentMail, listOf(EmailRecipient.UserId(dataRequestEntity.userId)), emptyList(), emptyList(),
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
    fun buildDataUpdatedEmailAndSendCEMessage(
        dataRequestEntity: DataRequestEntity,
        correlationId: String,
    ) {
        val message = buildDataUpdatedEmailMessage(dataRequestEntity)
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body = objectMapper.writeValueAsString(message),
            type = MessageType.SEND_EMAIL,
            correlationId = correlationId,
            exchange = ExchangeName.SEND_EMAIL,
            routingKey = RoutingKeyNames.EMAIL,
        )
    }

    /**
     * Function to build the EmailMessage object corresponding to the e-mail sent to
     * a user with a closed request after a relevant QA status update event happened.
     */
    fun buildDataUpdatedEmailMessage(dataRequestEntity: DataRequestEntity): EmailMessage {
        val dataUpdatedEmailContentMail =
            DataUpdatedEmailContent(
                companyName = companyInfoService.getValidCompanyNameOrId(dataRequestEntity.datalandCompanyId),
                reportingPeriod = dataRequestEntity.reportingPeriod,
                dataTypeLabel = dataRequestEntity.getDataTypeDescription(),
                creationDate = convertUnitTimeInMsToDate(dataRequestEntity.creationTimestamp),
                dataRequestId = dataRequestEntity.dataRequestId,
            )
        return EmailMessage(
            typedEmailContent = dataUpdatedEmailContentMail,
            receiver = listOf(EmailRecipient.UserId(dataRequestEntity.userId)),
            cc = emptyList(),
            bcc = emptyList(),
        )
    }
}

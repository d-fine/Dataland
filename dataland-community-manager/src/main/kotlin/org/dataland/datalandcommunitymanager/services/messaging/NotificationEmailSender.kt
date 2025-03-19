package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.ElementaryEventEntity
import org.dataland.datalandcommunitymanager.services.NotificationService.NotificationEmailType
import org.dataland.datalandcommunitymanager.utils.readableFrameworkNameMapping
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.InternalEmailContentTable
import org.dataland.datalandmessagequeueutils.messages.email.MultipleDatasetsUploadedEngagement
import org.dataland.datalandmessagequeueutils.messages.email.TypedEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.Value
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * A service used to send external engagement emails and internal emails in the NotificationService.
 */
@Service("NotificationEmailSender")
class NotificationEmailSender(
    @Autowired val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired val objectMapper: ObjectMapper,
) {
    private val internalEmailSubject = "Dataland Notification Email has been sent"
    private val internalEmailTextTitle = "An IR Notification Email has been sent"
    private val internalEmailHtmlTitle = "IR Notification Email has been sent"

    /**
     * Sends both external and internal notification emails based on the specified parameters.
     *
     * @param latestElementaryEvent The most recent elementary event entity. That triggered the notification email.
     * @param unprocessedElementaryEvents A list of elementary event entities that are unprocessed
     * and contained in the summary email.
     * @param companyId TODO
     * @param receiver A list of recipient email addresses for the company.
     * @param correlationId The correlation identifier for tracking the email notification.
     */
    fun sendExternalAndInternalInvestorRelationshipSummaryEmail(
        unprocessedEvents: List<NotificationEventEntity>,
        companyId: UUID,
        receiver: List<String>,
        correlationId: String,
    ) {
        val (externalEmailContent, internalEmailContent) =
                    buildExternalAndInternalInvestorRelationshipSummaryEmail(unprocessedEvents, companyId, receiver)

        receiver.forEach {
            sendEmailMessage(externalEmailContent, listOf(EmailRecipient.EmailAddress(it)), emptyList(), correlationId)
        }

        sendEmailMessage(internalEmailContent, listOf(EmailRecipient.Internal), listOf(EmailRecipient.InternalCc), correlationId)
    }

    fun sendDataRequestSummaryEmail(
    ) {
        // TODO
    }

    private fun buildExternalAndInternalInvestorRelationshipSummaryEmail(
        unprocessedEvents: List<NotificationEventEntity>,
        companyId: String,
        receiver: List<String>,
    ): Pair<TypedEmailContent, TypedEmailContent> {
        val frameworkData =
            unprocessedEvents
                .groupBy { it.framework }
                .mapValues { entry -> entry.value.map { it.reportingPeriod } }

        val externalEmailContent =
            MultipleDatasetsUploadedEngagement(
                companyName = getCompanyName(companyId), // TODO: from companyRolesManager?
                companyId = companyId,
                frameworkData =
                    frameworkData.map {
                        MultipleDatasetsUploadedEngagement.FrameworkData(readableFrameworkNameMapping[it.key] ?: "", it.value)
                    },
            )

        val internalEmailContent =
            InternalEmailContentTable(
                internalEmailSubject, internalEmailTextTitle, internalEmailHtmlTitle,
                listOf(
                    "Company" to companyIdAndNameValue(externalEmailContent.companyId, externalEmailContent.companyName),
                    "Frameworks" to frameworkValue(frameworkData, externalEmailContent.companyId),
                    "Notification Email Type" to Value.Text("Summary"),
                    "Receiver" to receiver.map(Value::EmailAddressWithSubscriptionStatus).let(Value::List),
                ),
            )

        return Pair(externalEmailContent, internalEmailContent)
    }

    private fun buildDataRequestSummaryEmail(
    ) {
        // TODO
    }


    private fun companyIdAndNameValue(
        companyId: String,
        companyName: String,
    ): Value =
        Value.List(
            Value.RelativeLink("/companies/$companyId", companyName),
            Value.Text("($companyId)"),
            separator = " ",
        )

    private fun dataTypeLink(
        dataType: DataTypeEnum,
        companyId: String,
    ): Value =
        Value.RelativeLink(
            "/companies/$companyId/frameworks/${dataType.value}",
            readableFrameworkNameMapping[dataType] ?: dataType.name,
        )

    private fun frameworkValue(
        frameworkData: Map<DataTypeEnum, List<String>>,
        companyId: String,
    ): Value =
        frameworkData
            .map {
                Value.List(
                    dataTypeLink(it.key, companyId),
                    reportingPeriodsValue(it.value),
                    separator = " ",
                )
            }.let(Value::List)

    private fun reportingPeriodsValue(reportingPeriods: List<String>): Value =
        Value.List(reportingPeriods.map(Value::Text), separator = " ", start = " (", end = ")")

    private fun sendEmailMessage(
        typedEmailContent: TypedEmailContent,
        receiver: List<EmailRecipient>,
        cc: List<EmailRecipient>,
        correlationId: String,
    ) {
        val message = EmailMessage(typedEmailContent, receiver, cc, emptyList())
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(message),
            MessageType.SEND_EMAIL,
            correlationId,
            ExchangeName.SEND_EMAIL,
            RoutingKeyNames.EMAIL,
        )
    }
}

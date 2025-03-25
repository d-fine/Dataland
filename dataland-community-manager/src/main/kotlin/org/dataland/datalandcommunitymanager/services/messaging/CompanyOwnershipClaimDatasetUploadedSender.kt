package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.utils.CompanyInfoService
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
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * A service used to send external claim company ownership, when dataset is uploaded emails
 * and related internal emails in the NotificationService.
 */
@Service("CompanyOwnershipClaimDatasetUploadedSender")
class CompanyOwnershipClaimDatasetUploadedSender(
    @Autowired val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val companyInfoService: CompanyInfoService,
    @Autowired val objectMapper: ObjectMapper,
) {
    /**
     * Sends both external and internal Investor Relationship notification emails based on the specified parameters.
     *
     * @param unprocessedEvents A list of notification event entities that are unprocessed
     * and contained in the summary email.
     * @param companyId The identifier of the company in Dataland.
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

    /**
     * Builds the content for external and internal Investor Relationship summary emails.
     *
     * @param events List of NotificationEventEntity objects to process.
     * @param companyId UUID of the company for the email.
     * @param receiver List of recipient email addresses.
     * @return A pair of TypedEmailContent representing external and internal emails.
     */
    private fun buildExternalAndInternalInvestorRelationshipSummaryEmail(
        events: List<NotificationEventEntity>,
        companyId: UUID,
        receiver: List<String>,
    ): Pair<TypedEmailContent, TypedEmailContent> {
        // Group unprocessed events by framework and map them to reporting periods
        val frameworkData =
            events
                .groupBy { it.framework }
                .mapValues { entry -> entry.value.map { it.reportingPeriod } }

        // Create external email content detailing multiple dataset uploads
        val externalEmailContent =
            MultipleDatasetsUploadedEngagement(
                companyName = companyInfoService.checkIfCompanyIdIsValidAndReturnNameOrId(companyId.toString()),
                companyId = companyId.toString(),
                frameworkData =
                    frameworkData.map {
                        MultipleDatasetsUploadedEngagement.FrameworkData(
                            readableFrameworkNameMapping[it.key] ?: "",
                            it.value,
                        )
                    },
            )

        // Prepare details for the internal email content
        val internalEmailSubject = "Dataland Notification Email has been sent"
        val internalEmailTextTitle = "An IR Notification Email has been sent"
        val internalEmailHtmlTitle = "IR Notification Email has been sent"
        val internalEmailContent =
            InternalEmailContentTable(
                internalEmailSubject, internalEmailTextTitle, internalEmailHtmlTitle,
                listOf(
                    "Company" to
                        companyIdAndNameValue(
                            externalEmailContent.companyId,
                            externalEmailContent.companyName,
                        ),
                    "Frameworks" to frameworkValue(frameworkData, externalEmailContent.companyId),
                    "Notification Email Type" to Value.Text("Summary"),
                    "Receiver" to receiver.map(Value::EmailAddressWithSubscriptionStatus).let(Value::List),
                ),
            )

        // Return both external and internal email content
        return Pair(externalEmailContent, internalEmailContent)
    }

    /**
     * Constructs a Value representing the company ID and name link.
     *
     * @param companyId ID of the company.
     * @param companyName Name of the company.
     * @return A Value object representing the company link and ID.
     */
    private fun companyIdAndNameValue(
        companyId: String,
        companyName: String,
    ): Value =
        // Create a list value that includes a clickable link to the company page and its ID
        Value.List(
            Value.RelativeLink("/companies/$companyId", companyName),
            Value.Text("($companyId)"),
            separator = " ",
        )

    /**
     * Constructs a Value representing the link to the dataset for the given data type.
     *
     * @param dataType Enum representing the data type of the framework.
     * @param companyId ID of the company.
     * @return A Value object representing the data type link.
     */
    private fun dataTypeLink(
        dataType: DataTypeEnum,
        companyId: String,
    ): Value =
        // Create a relative link value for each data type within the company's framework
        Value.RelativeLink(
            "/companies/$companyId/frameworks/${dataType.value}",
            readableFrameworkNameMapping[dataType] ?: dataType.name,
        )

    /**
     * Constructs a Value representing framework data as a list including reporting periods.
     *
     * @param frameworkData A map of DataTypeEnum to a list of reporting periods.
     * @param companyId ID of the company.
     * @return A Value object representing the framework data.
     */
    private fun frameworkValue(
        frameworkData: Map<DataTypeEnum, List<String>>,
        companyId: String,
    ): Value =
        // Map each framework's data type to a composed list value with reporting periods
        frameworkData
            .map {
                Value.List(
                    dataTypeLink(it.key, companyId),
                    reportingPeriodsValue(it.value),
                    separator = " ",
                )
            }.let(Value::List)

    /**
     * Constructs a Value representing reporting periods as a formatted list.
     *
     * @param reportingPeriods List of reporting period strings.
     * @return A Value object encapsulating reporting periods.
     */
    private fun reportingPeriodsValue(reportingPeriods: List<String>): Value =
        // Format reporting periods into a readable list format
        Value.List(reportingPeriods.map(Value::Text), separator = " ", start = " (", end = ")")

    /**
     * Sends an email message to the queue with the specified content and recipients.
     *
     * @param typedEmailContent The content of the email message.
     * @param receiver The list of email recipients.
     * @param cc The list of CC recipients.
     * @param correlationId Unique identifier for tracking the message sending in the queue.
     */
    private fun sendEmailMessage(
        typedEmailContent: TypedEmailContent,
        receiver: List<EmailRecipient>,
        cc: List<EmailRecipient>,
        correlationId: String,
    ) {
        // Create an email message object based on the content and recipients
        val message = EmailMessage(typedEmailContent, receiver, cc, emptyList())
        // Convert message to JSON and send it to the message queue
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(message),
            MessageType.SEND_EMAIL,
            correlationId,
            ExchangeName.SEND_EMAIL,
            RoutingKeyNames.EMAIL,
        )
    }
}

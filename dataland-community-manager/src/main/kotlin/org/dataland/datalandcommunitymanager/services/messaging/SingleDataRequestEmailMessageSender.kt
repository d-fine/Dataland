package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.services.CompanyRolesManager
import org.dataland.datalandcommunitymanager.utils.readableFrameworkNameMapping
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.DatasetRequestedClaimOwnership
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.InternalEmailContentTable
import org.dataland.datalandmessagequeueutils.messages.email.Value
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * A class that manages generating emails messages for bulk and single data requests
 */
@Component
class SingleDataRequestEmailMessageSender(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired val companyApi: CompanyDataControllerApi,
    @Autowired private val companyRolesManager: CompanyRolesManager,
) {
    /**
     * Data structure holding the shared information of the sent messages
     */
    data class MessageInformation(
        val userAuthentication: DatalandJwtAuthentication,
        val datalandCompanyId: String,
        val dataType: DataTypeEnum,
        val reportingPeriods: Set<String>,
    )

    /**
     * Function that generates the message object for single data request internal mails
     */
    fun sendSingleDataRequestInternalMessage(
        messageInformation: MessageInformation,
        correlationId: String,
    ) {
        val companyName = companyApi.getCompanyInfo(messageInformation.datalandCompanyId).companyName
        val internalEmailContentTable =
            InternalEmailContentTable(
                "Dataland Single Data Request",
                "A single data request has been submitted",
                "Single Data Request",
                listOf(
                    "User" to Value.Text(messageInformation.userAuthentication.userDescription),
                    "E-Mail" to Value.Text(messageInformation.userAuthentication.username),
                    "First Name" to Value.Text(messageInformation.userAuthentication.firstName),
                    "Last Name" to Value.Text(messageInformation.userAuthentication.lastName),
                    "Data Type" to Value.Text(messageInformation.dataType.value),
                    "Reporting Periods" to Value.List(messageInformation.reportingPeriods.sorted().map(Value::Text)),
                    "Dataland Company ID" to Value.Text(messageInformation.datalandCompanyId),
                    "Company Name" to Value.Text(companyName),
                ),
            )
        val message =
            EmailMessage(
                internalEmailContentTable, listOf(EmailRecipient.Internal), listOf(EmailRecipient.InternalCc), emptyList(),
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
     * Function that generates the message object for single data request external mails
     */
    fun sendSingleDataRequestExternalMessage(
        messageInformation: MessageInformation,
        receiverSet: Set<String>,
        contactMessage: String?,
        correlationId: String,
    ) {
        val companyName = companyApi.getCompanyInfo(messageInformation.datalandCompanyId).companyName

        val emailData =
            DatasetRequestedClaimOwnership(
                companyId = messageInformation.datalandCompanyId,
                companyName = companyName,
                requesterEmail = messageInformation.userAuthentication.username,
                firstName = messageInformation.userAuthentication.firstName.takeIf { it.isNotBlank() },
                lastName = messageInformation.userAuthentication.lastName.takeIf { it.isNotBlank() },
                dataTypeLabel = readableFrameworkNameMapping.getValue(messageInformation.dataType),
                reportingPeriods = messageInformation.reportingPeriods.toList().sorted(),
                message = contactMessage.takeIf { !contactMessage.isNullOrBlank() },
            )

        val receiverList =
            receiverSet.flatMap {
                MessageEntity.addContact(it, companyRolesManager, messageInformation.datalandCompanyId)
            }

        receiverList.forEach { receiver ->
            val message =
                EmailMessage(
                    emailData, listOf(receiver), emptyList(), emptyList(),
                )
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                objectMapper.writeValueAsString(message), MessageType.SEND_EMAIL, correlationId,
                ExchangeName.SEND_EMAIL, RoutingKeyNames.EMAIL,
            )
        }
    }
}

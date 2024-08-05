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
import org.dataland.datalandmessagequeueutils.messages.InternalEmailMessage
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
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
) : DataRequestEmailMessageSenderBase() {
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
        val properties = mapOf(
            "User" to messageInformation.userAuthentication.userDescription,
            "E-Mail" to messageInformation.userAuthentication.username,
            "First Name" to messageInformation.userAuthentication.firstName,
            "Last Name" to messageInformation.userAuthentication.lastName,
            "Data Type" to messageInformation.dataType.value,
            "Reporting Periods" to formatReportingPeriods(messageInformation.reportingPeriods),
            "Dataland Company ID" to messageInformation.datalandCompanyId,
            "Company Name" to companyName,
        )
        val message = InternalEmailMessage(
            "Dataland Single Data Request",
            "A single data request has been submitted",
            "Single Data Request",
            properties,
        )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(message),
            MessageType.SendInternalEmail,
            correlationId,
            ExchangeName.SendEmail,
            RoutingKeyNames.internalEmail,
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
        val properties = mapOf(
            "companyId" to messageInformation.datalandCompanyId,
            "companyName" to companyName,
            "requesterEmail" to messageInformation.userAuthentication.username,
            "firstName" to messageInformation.userAuthentication.firstName.takeIf { it.isNotBlank() },
            "lastName" to messageInformation.userAuthentication.lastName.takeIf { it.isNotBlank() },
            "dataType" to readableFrameworkNameMapping.getValue(messageInformation.dataType),
            "reportingPeriods" to formatReportingPeriods(messageInformation.reportingPeriods),
            "message" to contactMessage.takeIf { !contactMessage.isNullOrBlank() },
        )

        val receiverList = receiverSet.flatMap {
            MessageEntity.realizeContact(it, companyRolesManager, messageInformation.datalandCompanyId)
        }

        receiverList.forEach {
            val message = TemplateEmailMessage(
                emailTemplateType = TemplateEmailMessage.Type.ClaimOwnership, receiver = it, properties = properties,
            )
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                objectMapper.writeValueAsString(message), MessageType.SendTemplateEmail, correlationId,
                ExchangeName.SendEmail, RoutingKeyNames.templateEmail,
            )
        }
    }
}

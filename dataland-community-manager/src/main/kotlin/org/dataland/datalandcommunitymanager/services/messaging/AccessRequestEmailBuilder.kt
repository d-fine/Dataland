package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.services.CompanyRolesManager
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.AccessToDatasetGranted
import org.dataland.datalandmessagequeueutils.messages.email.AccessToDatasetRequested
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.TimeZone

/**
 * This class manages the notification of requesters and company owners about access requests
 */
@Service("AccessRequestEmailSender")
class AccessRequestEmailBuilder(
    @Autowired private val companyRolesManager: CompanyRolesManager,
    @Autowired private val keycloakUserControllerApiService: KeycloakUserService,
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val companyApi: CompanyDataControllerApi,
) {
    /**
     * Data structure holding the granted access email information
     */
    data class GrantedEmailInformation(
        val datalandCompanyId: String,
        val dataType: String,
        val dataTypeDescription: String,
        val reportingPeriod: String,
        val userId: String,
        val dataRequestId: String,
        val creationTimestamp: Long,
    ) {
        constructor(dataRequestEntity: DataRequestEntity) :
            this(
                dataRequestEntity.datalandCompanyId,
                dataRequestEntity.dataType,
                dataRequestEntity.getDataTypeDescription(),
                dataRequestEntity.reportingPeriod,
                dataRequestEntity.userId,
                dataRequestEntity.dataRequestId,
                dataRequestEntity.creationTimestamp,
            )
    }

    /**
     * This method notifies the requester once access to the requested dataset was granted
     * @param emailInformation the email information of the company owner
     * @param correlationId the correlationId of the operation
     */
    fun notifyRequesterAboutGrantedRequest(
        emailInformation: GrantedEmailInformation,
        correlationId: String,
    ) {
        val companyName = companyApi.getCompanyInfo(emailInformation.datalandCompanyId).companyName

        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm")
        dateFormat.timeZone = TimeZone.getTimeZone("Europe/Berlin")

        val emailData =
            AccessToDatasetGranted(
                companyId = emailInformation.datalandCompanyId,
                companyName = companyName,
                dataType = emailInformation.dataType,
                dataTypeLabel = emailInformation.dataTypeDescription,
                reportingPeriod = emailInformation.reportingPeriod,
                creationDate = dateFormat.format(emailInformation.creationTimestamp),
            )
        val message =
            EmailMessage(
                emailData, listOf(EmailRecipient.UserId(emailInformation.userId)), emptyList(), emptyList(),
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
     Data structure holding the request access email information
     */
    data class RequestEmailInformation(
        val requesterUserId: String,
        val message: String?,
        val datalandCompanyId: String,
        val dataTypeDescription: String,
        val reportingPeriods: Set<String>,
        val contacts: Set<String>,
    ) {
        constructor(dataRequestEntity: DataRequestEntity) :
            this(
                dataRequestEntity.userId,
                dataRequestEntity.messageHistory.last().message,
                dataRequestEntity.datalandCompanyId,
                dataRequestEntity.getDataTypeDescription(),
                setOf(dataRequestEntity.reportingPeriod),
                dataRequestEntity.messageHistory.last().contactsAsSet(),
            )
    }

    /**
     * This method notifies the company owner about new pending access requests
     * @param emailInformation the requesters email information
     * @param correlationId the correlationId of the operation
     */
    fun notifyCompanyOwnerAboutNewRequest(
        emailInformation: RequestEmailInformation,
        correlationId: String,
    ) {
        val companyName = companyApi.getCompanyInfo(emailInformation.datalandCompanyId).companyName
        val requester = keycloakUserControllerApiService.getUser(emailInformation.requesterUserId)
        val contacts = emailInformation.contacts + setOf(MessageEntity.COMPANY_OWNER_KEYWORD)
        val receiverList =
            contacts.flatMap {
                MessageEntity.addContact(it, companyRolesManager, emailInformation.datalandCompanyId)
            }

        val emailData =
            AccessToDatasetRequested(
                companyId = emailInformation.datalandCompanyId,
                companyName = companyName,
                dataTypeLabel = emailInformation.dataTypeDescription,
                reportingPeriods = emailInformation.reportingPeriods.toList().sorted(),
                message = emailInformation.message.takeIf { it?.isNotBlank() ?: false },
                requesterEmail = requester.email,
                requesterFirstName = requester.firstName.takeIf { it?.isNotBlank() ?: false },
                requesterLastName = requester.lastName.takeIf { it?.isNotBlank() ?: false },
            )

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

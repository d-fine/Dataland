package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.services.CompanyRolesManager
import org.dataland.datalandcommunitymanager.services.KeycloakUserControllerApiService
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.*

@Service("AccessRequestEmailSender")
class AccessRequestEmailSender(
    @Autowired private val companyRolesManager: CompanyRolesManager,
    @Autowired private val keycloakUserControllerApiService: KeycloakUserControllerApiService,
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val companyApi: CompanyDataControllerApi,
) {

    data class GrantedEmailInformation(
        val datalandCompanyId: String,
        val dataTypeDescription: String,
        val reportingPeriod: String,
        val userId: String,
        val dataRequestId: String,
        val creationTimestamp: Long,
    ) {
        constructor(dataRequestEntity: DataRequestEntity) :
            this(
                dataRequestEntity.datalandCompanyId,
                dataRequestEntity.getDataTypeDescription(),
                dataRequestEntity.reportingPeriod,
                dataRequestEntity.userId,
                dataRequestEntity.dataRequestId,
                dataRequestEntity.creationTimestamp,
            )
    }

    fun notifyRequesterAboutGrantedRequest(emailInformation: GrantedEmailInformation, correlationId: String) {
        val companyName = companyApi.getCompanyInfo(emailInformation.datalandCompanyId).companyName

        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm")
        dateFormat.timeZone = TimeZone.getTimeZone("Europe/Berlin")

        val properties = mapOf(
            "dataRequestId" to emailInformation.dataRequestId,
            "companyName" to companyName,
            "dataType" to emailInformation.dataTypeDescription,
            "reportingPeriod" to emailInformation.reportingPeriod,
            "creationDate" to dateFormat.format(emailInformation.creationTimestamp),
        )

        val message = TemplateEmailMessage(
            emailTemplateType = TemplateEmailMessage.Type.DataAccessGranted,
            receiver = TemplateEmailMessage.UserIdEmailRecipient(emailInformation.userId),
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

    fun notifyCompanyOwnerAboutNewRequest(emailInformation: RequestEmailInformation, correlationId: String) {
        val reportingPeriods = emailInformation.reportingPeriods.toList().sorted().joinToString(", ")

        val requester = keycloakUserControllerApiService.getUser(emailInformation.requesterUserId)

        val contacts = emailInformation.contacts + setOf(MessageEntity.COMPANY_OWNER_KEYWORD)

        val receiverList = emailInformation.contacts.flatMap {
            MessageEntity.realizeContact(it, companyRolesManager, emailInformation.datalandCompanyId)
        }

        val properties = mapOf("ert" to "abs") // TODO FIll them an make template

        receiverList.forEach {
            val message = TemplateEmailMessage(
                emailTemplateType = TemplateEmailMessage.Type.DataAccessRequested,
                receiver = it,
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
}

package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.services.KeycloakUserControllerApiService
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Manage sending emails to user regarding data requests answered
 */
@Service("DataRequestedAnsweredEmailSender")
class DataRequestedAnsweredEmailMessageSender(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val keycloakUserControllerApiService: KeycloakUserControllerApiService,
    @Autowired private val companyDataControllerApi: CompanyDataControllerApi,
    @Value("\${dataland.community-manager.data-request.answered.stale-days-threshold}")
    private val staleDaysThreshold: String,
) : DataRequestResponseEmailSenderBase(keycloakUserControllerApiService, companyDataControllerApi) {
    /**
     * Method to informs user by mail that his request is answered.
     * @param dataRequestEntity the dataRequestEntity
     */
    fun sendDataRequestedAnsweredEmail(
        dataRequestEntity: DataRequestEntity,
        correlationId: String,
    ) {
        val properties = mapOf(
            "companyId" to dataRequestEntity.datalandCompanyId,
            "companyName" to getCompanyNameById(dataRequestEntity.datalandCompanyId),
            "dataType" to dataRequestEntity.dataType,
            "reportingPeriod" to dataRequestEntity.reportingPeriod,
            "creationDate" to convertUnitTimeInMsToDate(dataRequestEntity.creationTimestamp),
            "dataTypeDescription" to getDataTypeDescription(dataRequestEntity.dataType),
            "dataRequestId" to dataRequestEntity.dataRequestId,
            "closedIn" to "$staleDaysThreshold days",
        )
        val message = TemplateEmailMessage(
            emailTemplateType = TemplateEmailMessage.Type.DataRequestedAnswered,
            receiver = getUserEmailById(dataRequestEntity.userId),
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

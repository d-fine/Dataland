package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.InternalEmailMessage
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

/**
 * A class that manages generating emails messages for bulk and single data requests
 */
@Component
class SingleDataRequestEmailMessageSender(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired val companyApi: CompanyDataControllerApi,
) : DataRequestEmailMessageSenderBase() {
    private val logger = LoggerFactory.getLogger(SingleDataRequestEmailMessageSender::class.java)

    /**
     * Function that generates the message object for single data request mails
     */
    fun sendSingleDataRequestInternalMessage(
        userAuthentication: DatalandJwtAuthentication,
        datalandCompanyId: String,
        dataType: DataTypeEnum,
        reportingPeriods: Set<String>,
    ) {
        val correlationId = UUID.randomUUID().toString()
        val companyName = companyApi.getCompanyInfo(datalandCompanyId).companyName
        logger.info(
            "User with Id ${userAuthentication.userId} has submitted a single data request for company with" +
                " Id $datalandCompanyId and correlationId $correlationId",
        )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(
                InternalEmailMessage(
                    "Dataland Single Data Request",
                    "A single data request has been submitted",
                    "Single Data Request",
                    mapOf(
                        "User" to buildUserInfo(userAuthentication),
                        "Data Type" to dataType.value,
                        "Reporting Periods" to formatReportingPeriods(reportingPeriods),
                        "Dataland Company ID" to datalandCompanyId,
                        "Company Name" to companyName,
                    ),
                ),
            ),
            MessageType.SendInternalEmail,
            correlationId,
            ExchangeName.SendEmail,
            RoutingKeyNames.internalEmail,
        )
    }

    /**
     * Function that generates the message object for single data request mails
     */
    fun sendSingleDataRequestExternalMessage(
        receiver: String,
        userAuthentication: DatalandJwtAuthentication,
        datalandCompanyId: String,
        dataType: DataTypeEnum,
        reportingPeriods: Set<String>,
        contactMessage: String?,
    ) {
        val correlationId = UUID.randomUUID().toString()
        val companyName = companyApi.getCompanyInfo(datalandCompanyId).companyName
        logger.info(
            "User with Id ${userAuthentication.userId} has submitted a single data request for company with" +
                " Id $datalandCompanyId and correlationId $correlationId",
        )
        val properties = mapOf(
            "companyId" to datalandCompanyId,
            "companyName" to companyName,
            "requesterEmail" to userAuthentication.username,
            "dataType" to dataType.value,
            "reportingPeriods" to formatReportingPeriods(reportingPeriods),
            "message" to contactMessage.takeIf { !contactMessage.isNullOrBlank() },
        )
        val message = TemplateEmailMessage(
            emailTemplateType = TemplateEmailMessage.Type.DataRequestedClaimOwnership,
            receiver = receiver,
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

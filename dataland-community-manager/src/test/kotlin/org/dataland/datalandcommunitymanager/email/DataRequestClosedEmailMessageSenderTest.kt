package org.dataland.datalandcommunitymanager.email

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandcommunitymanager.services.KeycloakUserControllerApiService
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestClosedEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.DataRequestResponseEmailSenderUtils
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DataRequestClosedEmailMessageSenderTest {
    private val requestResponseEmailSenderUtils = DataRequestResponseEmailSenderUtils()
    private val objectMapper = jacksonObjectMapper()
    private lateinit var dataRequestId: String
    private lateinit var cloudEventMessageHandlerMock: CloudEventMessageHandler
    private lateinit var keycloakUserControllerApiService: KeycloakUserControllerApiService
    private val correlationId = requestResponseEmailSenderUtils.getCorrelationId()
    private val staleDaysThreshold = requestResponseEmailSenderUtils.getStaleInDays()
    private val dataTypes = requestResponseEmailSenderUtils.getListOfAllDataTypes()

    @BeforeEach
    fun setupAuthentication() {
        requestResponseEmailSenderUtils.setupAuthentication()
        keycloakUserControllerApiService = requestResponseEmailSenderUtils.getKeycloakControllerApiService()
    }

    @Test
    fun `validate that the output of the external email message sender is correctly build for all frameworks`() {
        dataTypes.forEach {
            val dataRequestEntity = requestResponseEmailSenderUtils.getDataRequestEntityWithDataType(it[0])
            dataRequestId = dataRequestEntity.dataRequestId
            cloudEventMessageHandlerMock =
                requestResponseEmailSenderUtils.getMockCloudEventMessageHandlerAndSetChecks(
                    it[0], it[1], dataRequestId, TemplateEmailMessage.Type.DataRequestClosed,
                )

            val dataRequestClosedEmailMessageSender =
                DataRequestClosedEmailMessageSender(
                    cloudEventMessageHandlerMock,
                    objectMapper, keycloakUserControllerApiService,
                    requestResponseEmailSenderUtils.getCompanyDataControllerMock(), staleDaysThreshold,
                )
            dataRequestClosedEmailMessageSender
                .sendDataRequestClosedEmail(dataRequestEntity, correlationId)
        }
    }
}

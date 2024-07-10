package org.dataland.datalandcommunitymanager.email

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.services.messaging.SingleDataRequestEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.readableFrameworkNameMapping
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.InternalEmailMessage
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.`when`
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SingleDataRequestEmailMessageSenderTest {
    private val objectMapper = jacksonObjectMapper()
    private lateinit var authenticationMock: DatalandJwtAuthentication
    private val cloudEventMessageHandlerMock = mock(CloudEventMessageHandler::class.java)
    private val companyName = "Test Inc."
    private val reportingPeriods = setOf("2022", "2023")
    private val reportingPeriodsAsString = "2022, 2023"
    private val datalandCompanyId = "59f05156-e1ba-4ea8-9d1e-d4833f6c7afc"
    private val correlationId = UUID.randomUUID().toString()

    private lateinit var singleDataRequestEmailMessageSender: SingleDataRequestEmailMessageSender

    @BeforeAll
    fun setup() {
        val mockSecurityContext = mock(SecurityContext::class.java)
        authenticationMock = AuthenticationMock.mockJwtAuthentication(
            "requester@example.com",
            "1234-221-1111elf",
            setOf(DatalandRealmRole.ROLE_USER),
        )
        `when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        `when`(authenticationMock.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
        val companyApiMock = mock(CompanyDataControllerApi::class.java)
        val companyInfoMock = mock(CompanyInformation::class.java)
        `when`(companyInfoMock.companyName).thenReturn(companyName)
        `when`(companyApiMock.getCompanyInfo(anyString())).thenReturn(companyInfoMock)
        singleDataRequestEmailMessageSender = SingleDataRequestEmailMessageSender(
            cloudEventMessageHandler = cloudEventMessageHandlerMock,
            objectMapper = objectMapper,
            companyApi = companyApiMock,
        )
    }

    @BeforeEach
    fun reset() {
        reset(cloudEventMessageHandlerMock)
    }

    private fun buildInternalEmailMessageMock() {
        `when`(
            cloudEventMessageHandlerMock.buildCEMessageAndSendToQueue(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
            ),
        ).then() {
            val arg1 = objectMapper.readValue(it.getArgument<String>(0), InternalEmailMessage::class.java)
            val arg2 = it.getArgument<String>(1)
            val arg3 = it.getArgument<String>(2)
            val arg4 = it.getArgument<String>(3)
            val arg5 = it.getArgument<String>(4)

            assertEquals("Dataland Single Data Request", arg1.subject)
            assertEquals("A single data request has been submitted", arg1.textTitle)
            assertEquals("Single Data Request", arg1.htmlTitle)
            assertEquals(authenticationMock.userDescription, arg1.properties.getValue("User"))
            assertEquals("lksg", arg1.properties.getValue("Data Type"))
            assertEquals(reportingPeriodsAsString, arg1.properties.getValue("Reporting Periods"))
            assertEquals(datalandCompanyId, arg1.properties.getValue("Dataland Company ID"))
            assertEquals(companyName, arg1.properties.getValue("Company Name"))
            assertEquals(MessageType.SendInternalEmail, arg2)
            assertEquals(correlationId, arg3)
            assertEquals(ExchangeName.SendEmail, arg4)
            assertEquals(RoutingKeyNames.internalEmail, arg5)
        }
    }

    @Test
    fun `validate that the output of the internal email message sender is correctly built`() {
        buildInternalEmailMessageMock()
        singleDataRequestEmailMessageSender.sendSingleDataRequestInternalMessage(
            SingleDataRequestEmailMessageSender.MessageInformation(
                authenticationMock, datalandCompanyId, DataTypeEnum.lksg, reportingPeriods,
            ),
            correlationId,
        )
    }

    private fun buildExternalEmailMessageMock() {
        `when`(
            cloudEventMessageHandlerMock.buildCEMessageAndSendToQueue(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
            ),
        ).then() {
            val arg1 = objectMapper.readValue(it.getArgument<String>(0), TemplateEmailMessage::class.java)
            val arg2 = it.getArgument<String>(1)
            val arg3 = it.getArgument<String>(2)
            val arg4 = it.getArgument<String>(3)
            val arg5 = it.getArgument<String>(4)

            assertEquals(TemplateEmailMessage.Type.ClaimOwnership, arg1.emailTemplateType)
            assertEquals("alphabet@example.com", arg1.receiver)
            assertEquals(datalandCompanyId, arg1.properties.getValue("companyId"))
            assertEquals(companyName, arg1.properties.getValue("companyName"))
            assertEquals(authenticationMock.username, arg1.properties.getValue("requesterEmail"))
            assertEquals(readableFrameworkNameMapping.getValue(DataTypeEnum.p2p), arg1.properties.getValue("dataType"))
            assertEquals(reportingPeriodsAsString, arg1.properties.getValue("reportingPeriods"))
            assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", arg1.properties.getValue("message"))
            assertEquals(MessageType.SendTemplateEmail, arg2)
            assertEquals(correlationId, arg3)
            assertEquals(ExchangeName.SendEmail, arg4)
            assertEquals(RoutingKeyNames.templateEmail, arg5)
        }
    }

//    @Test
//    fun `validate that the output of the external email message sender is correctly built`() {
//        buildExternalEmailMessageMock()
//        singleDataRequestEmailMessageSender.sendSingleDataRequestExternalMessage(
//            SingleDataRequestEmailMessageSender.MessageInformation(
//                authenticationMock, datalandCompanyId, DataTypeEnum.p2p, reportingPeriods,
//            ),
//            "alphabet@example.com",
//            "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
//            correlationId,
//        )
//        todo
//    }
}

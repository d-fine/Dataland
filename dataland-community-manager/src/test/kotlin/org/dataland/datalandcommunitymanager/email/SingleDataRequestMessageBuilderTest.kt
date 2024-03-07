package org.dataland.datalandcommunitymanager.email

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

class SingleDataRequestMessageBuilderTest {
    val objectMapper = jacksonObjectMapper() // TODO workaround.  to be discussed and investigated
    private lateinit var authenticationMock: DatalandJwtAuthentication
    private val cloudEventMessageHandlerMock = mock(CloudEventMessageHandler::class.java)
    private val companyName = "Test Inc."
    private val reportingPeriods = setOf("2022", "2023")
    private val datalandCompanyId = "59f05156-e1ba-4ea8-9d1e-d4833f6c7afc"
    private val correlationId = UUID.randomUUID().toString()

    fun buildUserInfo(
        userAuthentication: DatalandJwtAuthentication,
    ): String {
        return "User ${userAuthentication.username} (Keycloak ID: ${userAuthentication.userId})"
    }

    fun formatReportingPeriods(reportingPeriods: Set<String>) =
        reportingPeriods.toList().sorted().joinToString(", ")

    @BeforeEach
    fun setupAuthentication() {
        val mockSecurityContext = mock(SecurityContext::class.java)
        authenticationMock = AuthenticationMock.mockJwtAuthentication(
            "requester@bigplayer.com",
            "1234-221-1111elf",
            setOf(DatalandRealmRole.ROLE_USER),
        )
        `when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        `when`(authenticationMock.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `validate that the output of the internal email message sender is correctly build`() {
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
            assertEquals(buildUserInfo(authenticationMock), arg1.properties.getValue("User"))
            assertEquals("lksg", arg1.properties.getValue("Data Type"))
            assertEquals("2022, 2023", arg1.properties.getValue("Reporting Periods"))
            assertEquals(datalandCompanyId, arg1.properties.getValue("Dataland Company ID"))
            assertEquals(companyName, arg1.properties.getValue("Company Name"))
            assertEquals(MessageType.SendInternalEmail, arg2)
            assertEquals(correlationId, arg3)
            assertEquals(ExchangeName.SendEmail, arg4)
            assertEquals(RoutingKeyNames.internalEmail, arg5)
        }

        val properties = mapOf(
            "User" to buildUserInfo(authenticationMock),
            "Data Type" to DataTypeEnum.lksg.toString(),
            "Reporting Periods" to formatReportingPeriods(reportingPeriods),
            "Dataland Company ID" to datalandCompanyId,
            "Company Name" to companyName,
        )
        val message = InternalEmailMessage(
            "Dataland Single Data Request",
            "A single data request has been submitted",
            "Single Data Request",
            properties,
        )
        cloudEventMessageHandlerMock.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(message),
            MessageType.SendInternalEmail,
            correlationId,
            ExchangeName.SendEmail,
            RoutingKeyNames.internalEmail,
        )
    }

    @Test
    fun `validate that the output of the external email message sender is correctly build`() {
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

            assertEquals(TemplateEmailMessage.Type.DataRequestedClaimOwnership, arg1.emailTemplateType)
            assertEquals("alphabet@dumy.com", arg1.receiver)
            assertEquals(datalandCompanyId, arg1.properties.getValue("companyId"))
            assertEquals(companyName, arg1.properties.getValue("companyName"))
            assertEquals(authenticationMock.username, arg1.properties.getValue("requesterEmail"))
            assertEquals(DataTypeEnum.p2p.toString(), arg1.properties.getValue("dataType"))
            assertEquals(formatReportingPeriods(reportingPeriods), arg1.properties.getValue("reportingPeriods"))
            assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", arg1.properties.getValue("message"))
            assertEquals(MessageType.SendTemplateEmail, arg2)
            assertEquals(correlationId, arg3)
            assertEquals(ExchangeName.SendEmail, arg4)
            assertEquals(RoutingKeyNames.templateEmail, arg5)
        }

        val properties = mapOf(
            "companyId" to datalandCompanyId,
            "companyName" to companyName,
            "requesterEmail" to authenticationMock.username,
            "dataType" to DataTypeEnum.p2p.toString(),
            "reportingPeriods" to formatReportingPeriods(reportingPeriods),
            "message" to "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
        )
        val message = TemplateEmailMessage(
            emailTemplateType = TemplateEmailMessage.Type.DataRequestedClaimOwnership,
            receiver = "alphabet@dumy.com",
            properties = properties,
        )
        cloudEventMessageHandlerMock.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(message),
            MessageType.SendTemplateEmail,
            correlationId,
            ExchangeName.SendEmail,
            RoutingKeyNames.templateEmail,
        )
    }
}

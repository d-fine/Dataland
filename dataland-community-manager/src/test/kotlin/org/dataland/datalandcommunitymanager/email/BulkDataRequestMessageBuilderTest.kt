package org.dataland.datalandcommunitymanager.email

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.InternalEmailMessage
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*
class BulkDataRequestMessageBuilderTest {
    val objectMapper = jacksonObjectMapper()
    private lateinit var authenticationMock: DatalandJwtAuthentication
    private val cloudEventMessageHandlerMock = Mockito.mock(CloudEventMessageHandler::class.java)
    private val correlationId = UUID.randomUUID().toString()
    private val bulkDataRequest = BulkDataRequest(
        companyIdentifiers = setOf(
            "AR8756188701," +
                "9856177321",
            "28f05156-e1ba-1ea8-8d1e-d4833f6c7afgh",
        ),
        dataTypes = setOf(DataTypeEnum.p2p, DataTypeEnum.lksg),
        reportingPeriods = setOf("2020, 2023"),
    )
    private val acceptedCompanyIdentifiers = listOf("AR8756188701,9856177321")
    fun formatReportingPeriods(reportingPeriods: Set<String>) =
        reportingPeriods.toList().sorted().joinToString(", ")

    @BeforeEach
    fun setupAuthentication() {
        val mockSecurityContext = Mockito.mock(SecurityContext::class.java)
        authenticationMock = AuthenticationMock.mockJwtAuthentication(
            "requester@bigplayer.com",
            "1234-221-1111elf",
            setOf(DatalandRealmRole.ROLE_USER),
        )
        Mockito.`when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        Mockito.`when`(authenticationMock.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `validate that the output of the bulk internal email message sender is correctly build`() {
        Mockito.`when`(
            cloudEventMessageHandlerMock.buildCEMessageAndSendToQueue(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
            ),
        ).then() {
            val arg1 = objectMapper.readValue(it.getArgument<String>(0), InternalEmailMessage::class.java)
            val arg2 = it.getArgument<String>(1)
            val arg3 = it.getArgument<String>(2)
            val arg4 = it.getArgument<String>(3)
            val arg5 = it.getArgument<String>(4)

            Assertions.assertEquals("Dataland Bulk Data Request", arg1.subject)
            Assertions.assertEquals("A bulk data request has been submitted", arg1.textTitle)
            Assertions.assertEquals("Bulk Data Request", arg1.htmlTitle)
            Assertions.assertEquals(authenticationMock.userDescription, arg1.properties.getValue("User"))
            Assertions.assertEquals("2020, 2023", arg1.properties.getValue("Reporting Periods"))
            Assertions.assertEquals(
                bulkDataRequest.dataTypes.joinToString(", ") { it.value },
                arg1.properties.getValue("Requested Frameworks"),
            )
            Assertions.assertEquals(
                acceptedCompanyIdentifiers.joinToString(", "),
                arg1.properties.getValue("Accepted Companies (Dataland ID)"),
            )
            Assertions.assertEquals(MessageType.SendInternalEmail, arg2)
            Assertions.assertEquals(correlationId, arg3)
            Assertions.assertEquals(ExchangeName.SendEmail, arg4)
            Assertions.assertEquals(RoutingKeyNames.internalEmail, arg5)
        }

        val properties = mapOf(
            "User" to authenticationMock.userDescription,
            "Reporting Periods" to formatReportingPeriods(bulkDataRequest.reportingPeriods),
            "Requested Frameworks" to bulkDataRequest.dataTypes.joinToString(", ") { it.value },
            "Accepted Companies (Dataland ID)" to acceptedCompanyIdentifiers.joinToString(", "),
        )
        val message = InternalEmailMessage(
            "Dataland Bulk Data Request",
            "A bulk data request has been submitted",
            "Bulk Data Request",
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
}

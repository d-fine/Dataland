package org.dataland.datalandbackend.email

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.DatalandCommunityManager
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.InternalEmailMessage
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

@SpringBootTest(classes = [DatalandCommunityManager::class], properties = ["spring.profiles.active=nodb"])
class SingleDataRequestEmailBuilderTest(@Autowired val objectMapper: ObjectMapper) {
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
    fun `validate that the output of the data request email message sender is correctly formatted`() {
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
        cloudEventMessageHandlerMock.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(
                InternalEmailMessage(
                    "Dataland Single Data Request",
                    "A single data request has been submitted",
                    "Single Data Request",
                    mapOf(
                        "User" to buildUserInfo(authenticationMock),
                        "Data Type" to DataTypeEnum.lksg.toString(),
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
}

package org.dataland.datalandbackend.services.messaging

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.dataland.datalandcommunitymanager.openApiClient.api.RequestControllerApi
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
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

class DataOwnershipSuccessfullyEmailMessageSenderTest {

    private val objectMapper = jacksonObjectMapper()
    private lateinit var authenticationMock: DatalandJwtAuthentication
    private val cloudEventMessageHandlerMock = Mockito.mock(CloudEventMessageHandler::class.java)
    private val companyName = "Test Inc."
    private val correlationId = UUID.randomUUID().toString()
    private val companyId = "59f05156-e1ba-4ea8-9d1e-d4833f6c7afc"
    private val userId = "1234-221-1111elf"
    private val userEmail = "$userId@example.com"
    private val numberOfOpenDataRequestsForCompany = "0"

    private val requestControllerApiMock = Mockito.mock(RequestControllerApi::class.java)
    private val authenticatedOkHttpClientMock = Mockito.mock(OkHttpClient::class.java)
    private val keycloakBaseUrlMock = "http://test"

    @BeforeEach
    fun setupAuthentication() {
        val mockSecurityContext = Mockito.mock(SecurityContext::class.java)
        authenticationMock = AuthenticationMock.mockJwtAuthentication(
            userEmail,
            userId,
            setOf(DatalandRealmRole.ROLE_USER),
        )
        Mockito.`when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        Mockito.`when`(authenticationMock.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `validate that the output of the external email message sender is correctly build for all frameworks`() {
        mockCloudEventMessageHandlerAndSetChecks()

        val dataOwnershipSuccessfullyEmailMessageSender =
            DataOwnershipSuccessfullyEmailMessageSender(
                cloudEventMessageHandlerMock,
                objectMapper,
                requestControllerApiMock,
                authenticatedOkHttpClientMock,
                keycloakBaseUrlMock,
            )

        val mockCall: Call = Mockito.mock(Call::class.java)
        val mockResponse: Response = Mockito.mock(Response::class.java)
        val mockResponseBody: ResponseBody = Mockito.mock(ResponseBody::class.java)

        val jsonStringRepresentation = "{\"email\": \"${userEmail}\", \"id\": \"${userId}\"}"

        Mockito.`when`(authenticatedOkHttpClientMock.newCall(any(Request::class.java))).thenReturn(mockCall)
        Mockito.`when`(mockCall.execute()).thenReturn(mockResponse)
        Mockito.`when`(mockResponse.body).thenReturn(mockResponseBody)
        Mockito.`when`(mockResponse.body!!.string()).thenReturn(jsonStringRepresentation)

        dataOwnershipSuccessfullyEmailMessageSender
            .sendDataOwnershipAcceptanceExternalEmailMessage(
                newDataOwnerId = userId,
                datalandCompanyId = companyId,
                companyName = companyName,
                correlationId = correlationId,
            )
    }

    private fun mockCloudEventMessageHandlerAndSetChecks() {
        Mockito.`when`(
            cloudEventMessageHandlerMock.buildCEMessageAndSendToQueue(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
            ),
        ).then() {
            val arg1 =
                objectMapper.readValue(it.getArgument<String>(0), TemplateEmailMessage::class.java)
            val arg2 = it.getArgument<String>(1)
            val arg3 = it.getArgument<String>(2)
            val arg4 = it.getArgument<String>(3)
            val arg5 = it.getArgument<String>(4)
            checkProperties(arg1.properties)
            Assertions.assertEquals(TemplateEmailMessage.Type.SuccessfullyClaimedOwnership, arg1.emailTemplateType)
            Assertions.assertEquals(userEmail, arg1.receiver)
            Assertions.assertEquals(MessageType.SendTemplateEmail, arg2)
            Assertions.assertEquals(correlationId, arg3)
            Assertions.assertEquals(ExchangeName.SendEmail, arg4)
            Assertions.assertEquals(RoutingKeyNames.templateEmail, arg5)
        }
    }
    private fun checkProperties(properties: Map<String, String?>) {
        Assertions.assertEquals(companyId, properties.getValue("companyId"))
        Assertions.assertEquals(companyName, properties.getValue("companyName"))
        Assertions.assertEquals(
            numberOfOpenDataRequestsForCompany,
            properties.getValue("numberOfOpenDataRequestsForCompany"),
        )
    }

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
}

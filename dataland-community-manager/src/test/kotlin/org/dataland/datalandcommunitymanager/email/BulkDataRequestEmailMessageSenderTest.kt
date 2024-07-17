package org.dataland.datalandcommunitymanager.email

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.services.messaging.BulkDataRequestEmailMessageSender
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

class BulkDataRequestEmailMessageSenderTest {
    val objectMapper = jacksonObjectMapper()
    private lateinit var authenticationMock: DatalandJwtAuthentication
    private val cloudEventMessageHandlerMock = Mockito.mock(CloudEventMessageHandler::class.java)
    private val mockProxyPrimaryUrl = "mockurl.dataland.com"
    private val correlationId = UUID.randomUUID().toString()
    private val companyName = "Company Name"
    private val bulkDataRequest = BulkDataRequest(
        companyIdentifiers = setOf(
            "AR8756188701," +
                "9856177321",
            "28f05156-e1ba-1ea8-8d1e-d4833f6c7afgh",
        ),
        dataTypes = setOf(DataTypeEnum.p2p, DataTypeEnum.lksg),
        reportingPeriods = setOf("2020, 2023"),
    )
    private val acceptedCompanyIdentifiers = listOf(
        CompanyIdAndName(companyId = "AR8756188701,9856177321", companyName = companyName),
    )
    private val expectedFormatting = "<a href=\"https://mockurl.dataland.com/companies/AR8756188701,9856177321\">" +
        "Company Name</a> (AR8756188701,9856177321)"
    private lateinit var bulkDataRequestEmailMessageSender: BulkDataRequestEmailMessageSender

    @BeforeEach
    fun setup() {
        val mockSecurityContext = Mockito.mock(SecurityContext::class.java)
        authenticationMock = AuthenticationMock.mockJwtAuthentication(
            "requester@example.com",
            "1234-221-1111elf",
            setOf(DatalandRealmRole.ROLE_USER),
        )
        Mockito.`when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        Mockito.`when`(authenticationMock.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
        val companyApiMock = Mockito.mock(CompanyDataControllerApi::class.java)
        val companyInfoMock = Mockito.mock(CompanyInformation::class.java)
        Mockito.`when`(companyInfoMock.companyName).thenReturn(companyName)
        Mockito.`when`(companyApiMock.getCompanyInfo(ArgumentMatchers.anyString())).thenReturn(companyInfoMock)
        bulkDataRequestEmailMessageSender = BulkDataRequestEmailMessageSender(
            cloudEventMessageHandler = cloudEventMessageHandlerMock,
            objectMapper = objectMapper,
            proxyPrimaryUrl = mockProxyPrimaryUrl,
        )
    }

    private fun buildInternalBulkEmailMessageMock() {
        Mockito.`when`(
            cloudEventMessageHandlerMock.buildCEMessageAndSendToQueue(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
            ),
        ).thenAnswer() {
            val arg1 = objectMapper.readValue(it.getArgument<String>(0), InternalEmailMessage::class.java)
            val arg2 = it.getArgument<String>(1)
            val arg3 = it.getArgument<String>(2)
            val arg4 = it.getArgument<String>(3)
            val arg5 = it.getArgument<String>(4)
            validateBuildInternalBulkEmailMessageMock(arg1, arg2, arg3, arg4, arg5)
        }
    }
    private fun validateBuildInternalBulkEmailMessageMock(
        arg1: InternalEmailMessage,
        arg2: String,
        arg3: String,
        arg4: String,
        arg5: String,
    ) {
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
            expectedFormatting,
            arg1.properties.getValue("Accepted Companies (Dataland ID)"),
        )
        Assertions.assertEquals(MessageType.SendInternalEmail, arg2)
        Assertions.assertEquals(correlationId, arg3)
        Assertions.assertEquals(ExchangeName.SendEmail, arg4)
        Assertions.assertEquals(RoutingKeyNames.internalEmail, arg5)
    }

    @Test
    fun `validate that the output of the bulk internal email message sender is correctly build`() {
        buildInternalBulkEmailMessageMock()
        bulkDataRequestEmailMessageSender.sendBulkDataRequestInternalMessage(
            bulkDataRequest,
            acceptedCompanyIdentifiers,
            correlationId,
        )
    }
}

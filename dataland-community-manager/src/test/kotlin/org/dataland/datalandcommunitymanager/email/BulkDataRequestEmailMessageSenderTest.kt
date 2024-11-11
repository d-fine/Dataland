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
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.KeyValueTable
import org.dataland.datalandmessagequeueutils.messages.email.Value
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

class BulkDataRequestEmailMessageSenderTest {
    private val objectMapper = jacksonObjectMapper()
    private lateinit var authenticationMock: DatalandJwtAuthentication
    private val cloudEventMessageHandlerMock = Mockito.mock(CloudEventMessageHandler::class.java)
    private val correlationId = UUID.randomUUID().toString()
    private val companyName = "Company Name"
    private val bulkDataRequest =
        BulkDataRequest(
            companyIdentifiers =
                setOf(
                    "AR8756188701," +
                        "9856177321",
                    "28f05156-e1ba-1ea8-8d1e-d4833f6c7afgh",
                ),
            dataTypes = setOf(DataTypeEnum.p2p, DataTypeEnum.lksg),
            reportingPeriods = setOf("2020", "2023"),
        )
    private val acceptedCompanyIdentifiers =
        listOf(
            CompanyIdAndName(companyId = "AR8756188701,9856177321", companyName = companyName),
        )
    private val expectedReportingPeriods = Value.List(Value.Text("2020"), Value.Text("2023"), separator = ", ")
    private val expectedFrameworks = Value.List(Value.Text("p2p"), Value.Text("lksg"), separator = ", ")

    private val expectedCompanies =
        Value.List(
            Value.List(
                Value.RelativeLink("/companies/AR8756188701,9856177321", "Company Name"),
                Value.Text("(AR8756188701,9856177321)"),
                separator = " "
            ),
            separator = ", "
        )

    private lateinit var bulkDataRequestEmailMessageSender: BulkDataRequestEmailMessageSender

    @BeforeEach
    fun setup() {
        val mockSecurityContext = Mockito.mock(SecurityContext::class.java)
        authenticationMock =
            AuthenticationMock.mockJwtAuthentication(
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
        bulkDataRequestEmailMessageSender =
            BulkDataRequestEmailMessageSender(
                cloudEventMessageHandler = cloudEventMessageHandlerMock,
                objectMapper = objectMapper,
            )
    }

    private fun buildInternalBulkEmailMessageMock() {
        Mockito
            .`when`(
                cloudEventMessageHandlerMock.buildCEMessageAndSendToQueue(any(), any(), any(), any(), any()),
            ).thenAnswer { invocation ->
                val emailMessage = objectMapper.readValue(invocation.getArgument<String>(0), EmailMessage::class.java)
                Assertions.assertTrue(emailMessage.typedEmailContent is KeyValueTable)
                val keyValueTable = emailMessage.typedEmailContent as KeyValueTable

                Assertions.assertEquals("Dataland Bulk Data Request", keyValueTable.subject)
                Assertions.assertEquals("A bulk data request has been submitted", keyValueTable.textTitle)
                Assertions.assertEquals("Bulk Data Request", keyValueTable.htmlTitle)

                val valueForKey: (String) -> Value? = { key -> keyValueTable.table.find { it.first == key }?.second }

                Assertions.assertEquals(Value.Text(authenticationMock.userDescription), valueForKey("User"))
                Assertions.assertEquals(expectedReportingPeriods, valueForKey("Reporting Periods"))
                Assertions.assertEquals(expectedFrameworks, valueForKey("Requested Frameworks"))
                Assertions.assertEquals(expectedCompanies, valueForKey("Accepted Companies (Dataland ID)"))

                Assertions.assertEquals(MessageType.SEND_EMAIL, invocation.getArgument<String>(1))
                Assertions.assertEquals(correlationId, invocation.getArgument<String>(2))
                Assertions.assertEquals(ExchangeName.SEND_EMAIL, invocation.getArgument<String>(3))
                Assertions.assertEquals(RoutingKeyNames.EMAIL, invocation.getArgument<String>(4))
            }
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

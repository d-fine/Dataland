package org.dataland.datalandcommunitymanager.email

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.services.KeycloakUserControllerApiService
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestedAnsweredEmailMessageSender
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.`when`
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

class DataRequestedAnsweredEmailMessageSenderTest {
    private val objectMapper = jacksonObjectMapper()
    private lateinit var authenticationMock: DatalandJwtAuthentication
    private lateinit var dataRequestId: String
    private val cloudEventMessageHandlerMock = mock(CloudEventMessageHandler::class.java)
    private val companyDataControllerMock = mock(CompanyDataControllerApi::class.java)
    private val keycloakUserControllerApiService = mock(KeycloakUserControllerApiService::class.java)
    private val companyName = "Test Inc."
    private val reportingPeriod = "2022"
    private val correlationId = UUID.randomUUID().toString()
    private val companyId = "59f05156-e1ba-4ea8-9d1e-d4833f6c7afc"
    private val userId = "1234-221-1111elf"
    private val userEmail = "$userId@example.com"
    private val staleDaysThreshold = "some Number"
    private val creationTimestamp = 1709820187875
    private val creationTimestampAsDate = "07 Mar 2024, 15:03"
    private val dataTypes = listOf(
        listOf("p2p", "WWF Pathway to Paris"),
        listOf("eutaxonomy-financials", "EU Taxonomy for financial companies"),
        listOf("eutaxonomy-non-financials", "EU Taxonomy for non-financial companies"),
        listOf("lksg", "LkSG"),
        listOf("sfdr", "SFDR"),
        listOf("sme", "SME"),
        listOf("esg-questionnaire", "ESG Questionnaire"),
        listOf("heimathafen", "Heimathafen"),
    )

    @BeforeEach
    fun setupAuthentication() {
        val mockSecurityContext = mock(SecurityContext::class.java)
        authenticationMock = AuthenticationMock.mockJwtAuthentication(
            userEmail,
            userId,
            setOf(DatalandRealmRole.ROLE_USER),
        )
        `when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        `when`(authenticationMock.credentials).thenReturn("")
        `when`(keycloakUserControllerApiService.getEmailAddress(userId)).thenReturn(userEmail)
        SecurityContextHolder.setContext(mockSecurityContext)
    }
    private fun setupCompanyDataController() {
        `when`(companyDataControllerMock.getCompanyInfo(companyId))
            .thenReturn(
                CompanyInformation(
                    companyName = companyName,
                    headquarters = "",
                    identifiers = emptyMap(),
                    countryCode = "",
                ),
            )
    }

    @Test
    fun `validate that the output of the external email message sender is correctly build for all frameworks`() {
        setupCompanyDataController()
        dataTypes.forEach {
            mockCloudEventMessageHandlerAndSetChecks(it[0], it[1])
            val dataRequestedAnsweredEmailMessageSender =
                DataRequestedAnsweredEmailMessageSender(
                    cloudEventMessageHandlerMock,
                    objectMapper, keycloakUserControllerApiService, companyDataControllerMock, staleDaysThreshold,
                )
            val dataRequestEntity = getDataRequestEntityWithDataType(it[0])
            dataRequestId = dataRequestEntity.dataRequestId
            dataRequestedAnsweredEmailMessageSender
                .sendDataRequestedAnsweredEmail(dataRequestEntity, correlationId)
            reset(cloudEventMessageHandlerMock)
        }
    }
    private fun getDataRequestEntityWithDataType(dataType: String): DataRequestEntity {
        return DataRequestEntity(
            userId = userId,
            creationTimestamp = creationTimestamp,
            dataType = dataType,
            reportingPeriod = reportingPeriod,
            datalandCompanyId = companyId,
        )
    }
    private fun mockCloudEventMessageHandlerAndSetChecks(dataType: String, dataTypeDescription: String) {
        `when`(
            cloudEventMessageHandlerMock.buildCEMessageAndSendToQueue(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
            ),
        ).then() {
            val arg1 =
                objectMapper.readValue(it.getArgument<String>(0), TemplateEmailMessage::class.java)
            val arg2 = it.getArgument<String>(1)
            val arg3 = it.getArgument<String>(2)
            val arg4 = it.getArgument<String>(3)
            val arg5 = it.getArgument<String>(4)
            assertEquals(TemplateEmailMessage.Type.DataRequestedAnswered, arg1.emailTemplateType)
            assertEquals(userEmail, arg1.receiver)
            checkProperties(arg1.properties, dataType, dataTypeDescription)
            assertEquals(MessageType.SendTemplateEmail, arg2)
            assertEquals(correlationId, arg3)
            assertEquals(ExchangeName.SendEmail, arg4)
            assertEquals(RoutingKeyNames.templateEmail, arg5)
        }
    }
    private fun checkProperties(properties: Map<String, String?>, dataType: String, dataTypeDescription: String) {
        assertEquals(companyId, properties.getValue("companyId"))
        assertEquals(companyName, properties.getValue("companyName"))
        assertEquals(dataType, properties.getValue("dataType"))
        assertEquals(dataTypeDescription, properties.getValue("dataTypeDescription"))
        assertEquals(reportingPeriod, properties.getValue("reportingPeriod"))
        assertEquals(creationTimestampAsDate, properties.getValue("creationDate"))
        assertEquals(dataRequestId, properties.getValue("dataRequestId"))
    }
}

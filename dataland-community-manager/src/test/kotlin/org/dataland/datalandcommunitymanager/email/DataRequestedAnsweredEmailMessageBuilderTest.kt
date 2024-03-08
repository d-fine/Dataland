package org.dataland.datalandcommunitymanager.email

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestedAnsweredEmailMessageSender
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

class DataRequestedAnsweredEmailMessageBuilderTest {
    private val objectMapper = jacksonObjectMapper()
    private lateinit var authenticationMock: DatalandJwtAuthentication
    private val cloudEventMessageHandlerMock = Mockito.mock(CloudEventMessageHandler::class.java)
    private val companyDataControllerMock = Mockito.mock(CompanyDataControllerApi::class.java)
    private val companyName = "Test Inc."
    private val reportingPeriod = "2022"
    private val companyId = "59f05156-e1ba-4ea8-9d1e-d4833f6c7afc"
    private val correlationId = UUID.randomUUID().toString()
    private val userId = "1234-221-1111elf"
    private val userEmail = "$userId@testemail.com"
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
        val mockSecurityContext = Mockito.mock(SecurityContext::class.java)
        authenticationMock = AuthenticationMock.mockJwtAuthentication(
            userEmail,
            userId,
            setOf(DatalandRealmRole.ROLE_USER),
        )
        Mockito.`when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        Mockito.`when`(authenticationMock.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
        Mockito.`when`(companyDataControllerMock.getCompanyInfo(companyId))
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
        dataTypes.forEach {
            setCloudEventMessageHandlerMockAndSetChecks(it[0], it[1])
            val dataRequestedAnsweredEmailMessageSender =
                DataRequestedAnsweredEmailMessageSender(cloudEventMessageHandlerMock, objectMapper)
            val dataRequestEntity = getDataRequestEntityWithDataType(it[0])
            dataRequestedAnsweredEmailMessageSender
                .sendDataRequestedAnsweredEmail(dataRequestEntity, companyName, correlationId)
            Mockito.reset(cloudEventMessageHandlerMock)
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
    private fun setCloudEventMessageHandlerMockAndSetChecks(dataType: String, dataTypeDescription: String) {
        Mockito.`when`(
            cloudEventMessageHandlerMock.buildCEMessageAndSendToQueue(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
            ),
        ).then() {
            val arg2 = it.getArgument<String>(1)
            val arg3 = it.getArgument<String>(2)
            val arg4 = it.getArgument<String>(3)
            val arg5 = it.getArgument<String>(4)
            val arg1 =
                objectMapper.readValue(it.getArgument<String>(0), TemplateEmailMessage::class.java)
            Assertions.assertEquals(TemplateEmailMessage.Type.DataRequestedAnswered, arg1.emailTemplateType)
            Assertions.assertEquals(userEmail, arg1.receiver)
            Assertions.assertEquals(companyId, arg1.properties.getValue("companyId"))
            Assertions.assertEquals(companyName, arg1.properties.getValue("companyName"))
            Assertions.assertEquals(dataType, arg1.properties.getValue("dataType"))
            Assertions.assertEquals(dataTypeDescription, arg1.properties.getValue("dataTypeDescription"))
            Assertions.assertEquals(reportingPeriod, arg1.properties.getValue("reportingPeriods"))
            Assertions.assertEquals(creationTimestampAsDate, arg1.properties.getValue("creationTimestamp"))
            Assertions.assertEquals(MessageType.SendTemplateEmail, arg2)
            Assertions.assertEquals(correlationId, arg3)
            Assertions.assertEquals(ExchangeName.SendEmail, arg4)
            Assertions.assertEquals(RoutingKeyNames.templateEmail, arg5)
        }
    }
}

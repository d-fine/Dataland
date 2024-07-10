//package org.dataland.datalandcommunitymanager.email
//
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
//import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
//import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
//import org.dataland.datalandemailservice.services.KeycloakUserControllerApiService
//import org.dataland.datalandcommunitymanager.services.messaging.DataRequestResponseEmailSender
//import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
//import org.dataland.datalandmessagequeueutils.constants.ExchangeName
//import org.dataland.datalandmessagequeueutils.constants.MessageType
//import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
//import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
//import org.dataland.keycloakAdapter.auth.DatalandRealmRole
//import org.dataland.keycloakAdapter.utils.AuthenticationMock
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.mockito.ArgumentMatchers
//import org.mockito.Mockito.mock
//import org.mockito.Mockito.`when`
//import org.springframework.security.core.context.SecurityContext
//import org.springframework.security.core.context.SecurityContextHolder
//import java.util.*
//
//class DataRequestResponseEmailSenderTest {
//    private val reportingPeriod = "2022"
//    private val companyId = "59f05156-e1ba-4ea8-9d1e-d4833f6c7afc"
//    private val userId = "1234-221-1111elf"
//    private val userEmail = "$userId@example.com"
//    private val creationTimestamp = 1709820187875
//    private val creationTimestampAsDate = "07 Mar 2024, 15:03"
//    private val companyName = "Test Inc."
//    private val objectMapper = jacksonObjectMapper()
//    private val correlationId = UUID.randomUUID().toString()
//    private val staleDaysThreshold = "some number"
//    private val dataTypes = getListOfAllDataTypes()
//    private lateinit var keycloakUserControllerApiService: KeycloakUserControllerApiService
//
//    @BeforeEach
//    fun setupAuthentication() {
//        val mockSecurityContext = mock(SecurityContext::class.java)
//        val authenticationMock = AuthenticationMock.mockJwtAuthentication(
//            userEmail,
//            userId,
//            setOf(DatalandRealmRole.ROLE_USER),
//        )
//        `when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
//        `when`(authenticationMock.credentials).thenReturn("")
//        SecurityContextHolder.setContext(mockSecurityContext)
//        keycloakUserControllerApiService = getKeycloakControllerApiService()
//    }
//    private fun getKeycloakControllerApiService(): KeycloakUserControllerApiService {
//        val keycloakUserControllerApiService = mock(KeycloakUserControllerApiService::class.java)
//        `when`(keycloakUserControllerApiService.getEmailAddress(userId)).thenReturn(userEmail)
//        return keycloakUserControllerApiService
//    }
//    private fun getDataRequestEntityWithDataType(dataType: String): DataRequestEntity {
//        return DataRequestEntity(
//            userId = userId,
//            creationTimestamp = creationTimestamp,
//            dataType = dataType,
//            reportingPeriod = reportingPeriod,
//            datalandCompanyId = companyId,
//        )
//    }
//    private fun checkPropertiesOfDataRequestResponseEmail(
//        dataRequestId: String,
//        properties: Map<String, String?>,
//        dataType: String,
//        dataTypeDescription: String,
//    ) {
//        assertEquals(companyId, properties.getValue("companyId"))
//        assertEquals(companyName, properties.getValue("companyName"))
//        assertEquals(dataType, properties.getValue("dataType"))
//        assertEquals(dataTypeDescription, properties.getValue("dataTypeDescription"))
//        assertEquals(reportingPeriod, properties.getValue("reportingPeriod"))
//        assertEquals(creationTimestampAsDate, properties.getValue("creationDate"))
//        assertEquals(dataRequestId, properties.getValue("dataRequestId"))
//        assertEquals(staleDaysThreshold, properties.getValue("closedInDays"))
//    }
//    private fun getCompanyDataControllerMock(): CompanyDataControllerApi {
//        val companyDataControllerMock = mock(CompanyDataControllerApi::class.java)
//        `when`(companyDataControllerMock.getCompanyInfo(companyId))
//            .thenReturn(
//                CompanyInformation(
//                    companyName = companyName,
//                    headquarters = "",
//                    identifiers = emptyMap(),
//                    countryCode = "",
//                ),
//            )
//        return companyDataControllerMock
//    }
//
//    private fun getListOfAllDataTypes(): List<List<String>> {
//        return listOf(
//            listOf("p2p", "WWF Pathways to Paris"),
//            listOf("eutaxonomy-financials", "EU Taxonomy for financial companies"),
//            listOf("eutaxonomy-non-financials", "EU Taxonomy for non-financial companies"),
//            listOf("lksg", "LkSG"),
//            listOf("sfdr", "SFDR"),
//            listOf("sme", "SME"),
//            listOf("esg-questionnaire", "ESG Questionnaire"),
//            listOf("heimathafen", "Heimathafen"),
//        )
//    }
//
//    private fun getMockCloudEventMessageHandlerAndSetChecks(
//        dataType: String,
//        dataTypeDescription: String,
//        dataRequestId: String,
//        emailMessageType: TemplateEmailMessage.Type,
//    ): CloudEventMessageHandler {
//        val cloudEventMessageHandlerMock = mock(CloudEventMessageHandler::class.java)
//        `when`(
//            cloudEventMessageHandlerMock.buildCEMessageAndSendToQueue(
//                ArgumentMatchers.anyString(),
//                ArgumentMatchers.anyString(),
//                ArgumentMatchers.anyString(),
//                ArgumentMatchers.anyString(),
//                ArgumentMatchers.anyString(),
//            ),
//        ).then() {
//            val arg1 =
//                objectMapper.readValue(it.getArgument<String>(0), TemplateEmailMessage::class.java)
//            val arg2 = it.getArgument<String>(1)
//            val arg3 = it.getArgument<String>(2)
//            val arg4 = it.getArgument<String>(3)
//            val arg5 = it.getArgument<String>(4)
//            assertEquals(emailMessageType, arg1.emailTemplateType)
//            assertEquals(userEmail, arg1.receiver)
//            checkPropertiesOfDataRequestResponseEmail(
//                dataRequestId, arg1.properties, dataType, dataTypeDescription,
//            )
//            assertEquals(MessageType.SendTemplateEmail, arg2)
//            assertEquals(correlationId, arg3)
//            assertEquals(ExchangeName.SendEmail, arg4)
//            assertEquals(RoutingKeyNames.templateEmail, arg5)
//        }
//        return cloudEventMessageHandlerMock
//    }
//
//    @Test
//    fun `validate that the output of the closed request email message sender is correctly build for all frameworks`() {
//        dataTypes.forEach {
//            val dataRequestEntity = getDataRequestEntityWithDataType(it[0])
//            val dataRequestId = dataRequestEntity.dataRequestId
//            val cloudEventMessageHandlerMock =
//                getMockCloudEventMessageHandlerAndSetChecks(
//                    it[0], it[1], dataRequestId, TemplateEmailMessage.Type.DataRequestClosed,
//                )
//
//            val dataRequestClosedEmailMessageSender =
//                DataRequestResponseEmailSender(
//                    cloudEventMessageHandlerMock,
//                    jacksonObjectMapper(), keycloakUserControllerApiService,
//                    getCompanyDataControllerMock(),
//                    staleDaysThreshold,
//                )
//            dataRequestClosedEmailMessageSender.sendDataRequestResponseEmail(
//                dataRequestEntity, TemplateEmailMessage.Type.DataRequestClosed, correlationId,
//            )
//        }
//    }
//
//    @Test
//    fun `check that the output of the answered request email message sender is correctly build for all frameworks`() {
//        dataTypes.forEach {
//            val dataRequestEntity = getDataRequestEntityWithDataType(it[0])
//            val dataRequestId = dataRequestEntity.dataRequestId
//            val cloudEventMessageHandlerMock =
//                getMockCloudEventMessageHandlerAndSetChecks(
//                    it[0], it[1], dataRequestId, TemplateEmailMessage.Type.DataRequestedAnswered,
//                )
//
//            val dataRequestClosedEmailMessageSender =
//                DataRequestResponseEmailSender(
//                    cloudEventMessageHandlerMock,
//                    jacksonObjectMapper(), keycloakUserControllerApiService,
//                    getCompanyDataControllerMock(),
//                    staleDaysThreshold,
//                )
//            dataRequestClosedEmailMessageSender.sendDataRequestResponseEmail(
//                dataRequestEntity, TemplateEmailMessage.Type.DataRequestedAnswered, correlationId,
//            )
//        }
//    }
//}

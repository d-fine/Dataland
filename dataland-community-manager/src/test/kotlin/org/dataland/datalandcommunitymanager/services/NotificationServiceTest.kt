package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandcommunitymanager.entities.ElementaryEventEntity
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.ElementaryEventType
import org.dataland.datalandcommunitymanager.repositories.ElementaryEventRepository
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationServiceTest {

    private lateinit var notificationService: NotificationService
    val notificationThresholdDays = 30
    val elementaryEventsThreshold = 10

    private val testCompanyId = UUID.randomUUID()
    private val testDataType = DataTypeEnum.heimathafen
    private val testReportingPeriod = "2022"

    private val testDataMetaInformation = DataMetaInformation(
        UUID.randomUUID().toString(),
        testCompanyId.toString(),
        testDataType,
        Instant.now().toEpochMilli(),
        testReportingPeriod,
        false,
        QaStatus.Pending,
        null,
    )

    private val testCompanyInformation = CompanyInformation(
        companyName = "Dummy Company",
        headquarters = "Berlin",
        identifiers = emptyMap(),
        countryCode = "DE",
        companyContactDetails = listOf("emailAddress@dummymail.de", "emailAddress@fakemailingfortest.de"),
    )

    private val testProxyPrimaryUrl = "www.dummy.com"
    private val testCorrelationId = UUID.randomUUID().toString()

    @BeforeAll
    fun setupNotificationService() {
        assertAssumptionsForTests()

        val cloudEventMessageHandlerMock = mock(CloudEventMessageHandler::class.java)
        val notificationEventRepository = mock(NotificationEventRepository::class.java)
        val elementaryEventRepository = mock(ElementaryEventRepository::class.java)
        val companyRolesManager = mock(CompanyRolesManager::class.java)
        val metaDataControllerApiMock = mock(MetaDataControllerApi::class.java)
        val companyDataControllerApiMock = mock(CompanyDataControllerApi::class.java)
        val objectMapper = jacksonObjectMapper()

        notificationService = NotificationService(
            cloudEventMessageHandlerMock,
            notificationEventRepository,
            elementaryEventRepository,
            companyDataControllerApiMock,
            companyRolesManager,
            objectMapper,
            notificationThresholdDays,
            elementaryEventsThreshold,
            testProxyPrimaryUrl,
        )
        `when`(metaDataControllerApiMock.getDataMetaInfo(anyString())).thenReturn(testDataMetaInformation)
        `when`(companyDataControllerApiMock.getCompanyInfo(testCompanyId.toString())).thenReturn(testCompanyInformation)
    }

    private fun assertAssumptionsForTests() {
        assertEquals(
            30,
            notificationThresholdDays,
            "The tests in this file only work with the assumption that the notification threshold is 30 days.",
        )
        assertEquals(
            10,
            elementaryEventsThreshold,
            "The tests in this file only work with the assumption that the elementary events threshold is 10.",
        )
    }

    private fun createUploadElementaryEventEntity(
        creationTimeInDaysBeforeNow: Int,
        framework: DataTypeEnum = testDataType,
        reportingPeriod: String = testReportingPeriod,
    ): ElementaryEventEntity {
        return ElementaryEventEntity(
            elementaryEventType = ElementaryEventType.UploadEvent,
            companyId = testCompanyId,
            framework = framework,
            reportingPeriod = reportingPeriod,
            creationTimestamp =
            Instant.now().minus(creationTimeInDaysBeforeNow.toLong(), ChronoUnit.DAYS).toEpochMilli(),
            notificationEvent = null,
        )
    }

    private fun createNotificationEventEntityForDataUploads(
        creationTimeInDaysBeforeNow: Long,
    ): NotificationEventEntity {
        return NotificationEventEntity(
            companyId = testCompanyId,
            elementaryEventType = ElementaryEventType.UploadEvent,
            creationTimestamp = Instant.now().minus(creationTimeInDaysBeforeNow, ChronoUnit.DAYS).toEpochMilli(),
        )
    }

    private fun setTheReturnValueForNotificationEventRepoQuery(
        notificationEventEntitiesToReturn: List<NotificationEventEntity>,
    ) {
        `when`(
            notificationService.notificationEventRepository
                .findNotificationEventByCompanyIdAndElementaryEventType(testCompanyId, ElementaryEventType.UploadEvent),
        )
            .thenReturn(
                notificationEventEntitiesToReturn,
            )
    }

    private fun parseJsonStringIntoTemplateEmailMessage(
        jsonString: String,
    ): TemplateEmailMessage {
        return notificationService.objectMapper.readValue(
            jsonString,
            TemplateEmailMessage::class.java,
        )
    }

    @Test
    fun `single mail if no notification event in last 30 days and one unprocessed elementary event`() {
        setTheReturnValueForNotificationEventRepoQuery(emptyList())
        val latestElementaryEvent = createUploadElementaryEventEntity(0)
        val unprocessedElementaryEvents = listOf(latestElementaryEvent)

        val notificationEmailType =
            notificationService.determineNotificationEmailType(
                latestElementaryEvent, unprocessedElementaryEvents,
            )
        assertEquals(NotificationService.NotificationEmailType.Single, notificationEmailType)
    }

    @Test
    fun `summary mail if no notification event in last 30 days and two unprocessed elementary events`() {
        val notificationEvent = createNotificationEventEntityForDataUploads(400)
        setTheReturnValueForNotificationEventRepoQuery(listOf(notificationEvent))
        val latestElementaryEvent = createUploadElementaryEventEntity(0)

        val unprocessedElementaryEvents = listOf(
            createUploadElementaryEventEntity(60),
            latestElementaryEvent,
        )
        val notificationEmailType =
            notificationService.determineNotificationEmailType(
                latestElementaryEvent,
                unprocessedElementaryEvents,
            )
        assertEquals(NotificationService.NotificationEmailType.Summary, notificationEmailType)
    }

    @Test
    fun `summary mail if notification event in last 30 days and unprocessed elementary events reach threshold`() {
        val notificationEvent = createNotificationEventEntityForDataUploads(29)
        setTheReturnValueForNotificationEventRepoQuery(listOf(notificationEvent))

        val latestElementaryEvent = createUploadElementaryEventEntity(21)
        val unprocessedElementaryEvents = mutableListOf(latestElementaryEvent)
        for (creationTimeInDaysBeforeNow in 29 downTo 22) {
            unprocessedElementaryEvents.add(
                createUploadElementaryEventEntity(creationTimeInDaysBeforeNow),
            )
        }
        val notificationEmailTypeForNineElementaryEvents =
            notificationService.determineNotificationEmailType(
                latestElementaryEvent,
                unprocessedElementaryEvents,
            )
        assertEquals(null, notificationEmailTypeForNineElementaryEvents)

        val newLatestElementaryEvent = createUploadElementaryEventEntity(0)

        unprocessedElementaryEvents.add(
            newLatestElementaryEvent,
        )
        val notificationEmailTypeForTenElementaryEvents =
            notificationService.determineNotificationEmailType(
                newLatestElementaryEvent,
                unprocessedElementaryEvents,
            )
        assertEquals(NotificationService.NotificationEmailType.Summary, notificationEmailTypeForTenElementaryEvents)
    }

    @Test
    fun `getting the last notification event for a company and elementary event type works as expected`() {
        val expectedLastNotificationEvent = createNotificationEventEntityForDataUploads(12)
        val notificationEvents = mutableListOf<NotificationEventEntity>()
        notificationEvents.add(expectedLastNotificationEvent)
        notificationEvents.add(createNotificationEventEntityForDataUploads(29))
        notificationEvents.add(createNotificationEventEntityForDataUploads(45))
        setTheReturnValueForNotificationEventRepoQuery(notificationEvents)

        val lastNotificationEvent =
            notificationService.getLastNotificationEventOrNull(testCompanyId, ElementaryEventType.UploadEvent)

        assertEquals(expectedLastNotificationEvent, lastNotificationEvent)
    }

    @Test
    fun `counting the days passed since the last notifiation event works as expected`() {
        val expectedDaysPassed: Long = 12
        val notificationEvents = mutableListOf<NotificationEventEntity>()
        notificationEvents.add(createNotificationEventEntityForDataUploads(expectedDaysPassed))
        notificationEvents.add(createNotificationEventEntityForDataUploads(29))
        notificationEvents.add(createNotificationEventEntityForDataUploads(45))
        setTheReturnValueForNotificationEventRepoQuery(notificationEvents)

        val daysPassedSinceLastNotificationEvent =
            notificationService.getDaysPassedSinceLastNotificationEvent(testCompanyId, ElementaryEventType.UploadEvent)

        assertEquals(expectedDaysPassed, daysPassedSinceLastNotificationEvent)
    }

    @Test
    fun `asserting that the check if last notification event is older than threshold works as expected`() {
        val notificationEvents = mutableListOf<NotificationEventEntity>()
        notificationEvents.add(createNotificationEventEntityForDataUploads(31))
        notificationEvents.add(createNotificationEventEntityForDataUploads(32))
        notificationEvents.add(createNotificationEventEntityForDataUploads(45))
        setTheReturnValueForNotificationEventRepoQuery(notificationEvents)

        assertTrue(
            notificationService.isLastNotificationEventOlderThanThreshold(
                testCompanyId,
                ElementaryEventType.UploadEvent,
            ),
        )

        notificationEvents.add(createNotificationEventEntityForDataUploads(30))
        setTheReturnValueForNotificationEventRepoQuery(notificationEvents)

        assertFalse(
            notificationService.isLastNotificationEventOlderThanThreshold(
                testCompanyId,
                ElementaryEventType.UploadEvent,
            ),
        )
    }

    @Test
    fun `check if the conversion of frameworks and reporting periods to a single string works as expected`() {
        val elementaryEvents = mutableListOf<ElementaryEventEntity>()
        elementaryEvents.add(createUploadElementaryEventEntity(5, DataTypeEnum.heimathafen, "2021"))
        elementaryEvents.add(createUploadElementaryEventEntity(6, DataTypeEnum.heimathafen, "2021"))
        elementaryEvents.add(createUploadElementaryEventEntity(8, DataTypeEnum.heimathafen, "2023"))
        elementaryEvents.add(createUploadElementaryEventEntity(12, DataTypeEnum.sfdr, "2024"))
        elementaryEvents.add(createUploadElementaryEventEntity(15, DataTypeEnum.lksg, "2020"))

        val expectedOutputString =
            "Heimathafen: 2021, 2023<br>SFDR: 2024<br>LkSG: 2020"

        val outputString = notificationService.createFrameworkAndYearStringFromElementaryEvents(elementaryEvents)

        assertEquals(expectedOutputString, outputString)
    }

    @Test
    fun `creating and storing a notification event works and is also reflected in the associated elementary events`() {
        val latestElementaryEvent = createUploadElementaryEventEntity(10)

        val unprocessedElementaryEvents = listOf(
            latestElementaryEvent,
            createUploadElementaryEventEntity(11),
        )

        lateinit var storedNotificationEventEntity: NotificationEventEntity

        `when`(notificationService.notificationEventRepository.saveAndFlush(any(NotificationEventEntity::class.java)))
            .thenAnswer {
                    invocation ->
                storedNotificationEventEntity = invocation.getArgument(0)

                assertEquals(testCompanyId, storedNotificationEventEntity.companyId)
                assertEquals(ElementaryEventType.UploadEvent, storedNotificationEventEntity.elementaryEventType)

                storedNotificationEventEntity
            }

        `when`(notificationService.elementaryEventRepository.saveAndFlush(any(ElementaryEventEntity::class.java)))
            .thenAnswer { invocation ->
                val elementaryEventEntityToStore = invocation.getArgument<ElementaryEventEntity>(0)

                assertEquals(storedNotificationEventEntity, elementaryEventEntityToStore.notificationEvent)

                elementaryEventEntityToStore
            }

        notificationService.createNotificationEventAndReferenceIt(latestElementaryEvent, unprocessedElementaryEvents)
    }

    private fun mockBuildingMessageAndSendingItToQueueForSingleMail() {
        Mockito.reset(notificationService.cloudEventMessageHandler)
        `when`(
            notificationService.cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
            ),
        ).then {
            val arg1 = parseJsonStringIntoTemplateEmailMessage(it.getArgument(0))
            val arg2 = it.getArgument<String>(1)
            val arg3 = it.getArgument<String>(2)
            val arg4 = it.getArgument<String>(3)
            val arg5 = it.getArgument<String>(4)

            assertEquals(TemplateEmailMessage.Type.SingleNotification, arg1.emailTemplateType)
            assertTrue(arg1.receiver.toString().contains("emailAddress@"))

            assertEquals(testCompanyInformation.companyName, arg1.properties["companyName"])
            assertEquals(testCompanyId.toString(), arg1.properties["companyId"])
            assertEquals(testDataType.toString(), arg1.properties["framework"])
            assertEquals(testReportingPeriod, arg1.properties["year"])
            assertEquals(testProxyPrimaryUrl, arg1.properties["baseUrl"])

            assertEquals(MessageType.SendTemplateEmail, arg2)
            assertEquals(testCorrelationId, arg3)
            assertEquals(ExchangeName.SendEmail, arg4)
            assertEquals(RoutingKeyNames.templateEmail, arg5)
        }
    }

    @Test
    fun `sending a message to queue containing the single email info works as expected`() {
        val latestElementaryEvent = createUploadElementaryEventEntity(10)
        val unprocessedElementaryEvents = listOf(latestElementaryEvent)
        val lastNotificationEvent = createNotificationEventEntityForDataUploads(10)

        setTheReturnValueForNotificationEventRepoQuery(listOf(lastNotificationEvent))
        mockBuildingMessageAndSendingItToQueueForSingleMail()
        notificationService.sendEmailMessagesToQueue(
            NotificationService.NotificationEmailType.Single,
            notificationService.buildEmailProperties(
                testCompanyInformation.companyName,
                NotificationService.NotificationEmailType.Single,
                latestElementaryEvent,
                unprocessedElementaryEvents,
            ),
            testCompanyInformation.companyContactDetails!!,
            testCorrelationId,
        )
    }

    private fun mockBuildingMessageAndSendingItToQueueForSummaryMail() {
        Mockito.reset(notificationService.cloudEventMessageHandler)
        `when`(
            notificationService.cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
            ),
        ).then {
            val arg1 = parseJsonStringIntoTemplateEmailMessage(it.getArgument(0))
            val arg2 = it.getArgument<String>(1)
            val arg3 = it.getArgument<String>(2)
            val arg4 = it.getArgument<String>(3)
            val arg5 = it.getArgument<String>(4)

            assertEquals(TemplateEmailMessage.Type.SummaryNotification, arg1.emailTemplateType)
            assertTrue(arg1.receiver.toString().contains("emailAddress@"))

            assertEquals(testCompanyInformation.companyName, arg1.properties["companyName"])
            assertEquals(testCompanyId.toString(), arg1.properties["companyId"])
            assertEquals("LkSG: 2020<br>SFDR: 2021<br>VSME: 2022", arg1.properties["frameworks"])
            assertEquals(testProxyPrimaryUrl, arg1.properties["baseUrl"])
            assertEquals("10", arg1.properties["numberOfDays"])

            assertEquals(MessageType.SendTemplateEmail, arg2)
            assertEquals(testCorrelationId, arg3)
            assertEquals(ExchangeName.SendEmail, arg4)
            assertEquals(RoutingKeyNames.templateEmail, arg5)
        }
    }

    @Test
    fun `validate that the output of the external email message sender is correctly built`() {
        val latestElementaryEvent = createUploadElementaryEventEntity(10, DataTypeEnum.lksg, "2020")

        val unprocessedElementaryEvents = listOf(
            latestElementaryEvent,
            createUploadElementaryEventEntity(11, DataTypeEnum.sfdr, "2021"),
            createUploadElementaryEventEntity(12, DataTypeEnum.vsme, "2022"),
        )
        val lastNotificationEvent = createNotificationEventEntityForDataUploads(10)

        setTheReturnValueForNotificationEventRepoQuery(listOf(lastNotificationEvent))
        mockBuildingMessageAndSendingItToQueueForSummaryMail()
        notificationService.sendEmailMessagesToQueue(
            NotificationService.NotificationEmailType.Summary,
            notificationService.buildEmailProperties(
                testCompanyInformation.companyName,
                NotificationService.NotificationEmailType.Summary,
                latestElementaryEvent,
                unprocessedElementaryEvents,
            ),
            testCompanyInformation.companyContactDetails!!,
            testCorrelationId,
        )
    }
}

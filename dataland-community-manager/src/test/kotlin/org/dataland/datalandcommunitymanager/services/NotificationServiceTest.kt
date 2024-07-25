package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.ArgumentMatchers.anyString
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
        companyContactDetails = listOf("email1@dummymail.de", "email2@fakemailingfortest.de"),
    )

    @BeforeAll
    fun setupNotificationService() {
        assertAssumptionsForTests()

        val cloudEventMessageHandlerMock = mock(CloudEventMessageHandler::class.java)
        val notificationEventRepository = mock(NotificationEventRepository::class.java)
        val elementaryEventRepository = mock(ElementaryEventRepository::class.java)
        val metaDataControllerApiMock = mock(MetaDataControllerApi::class.java)
        val companyDataControllerApiMock = mock(CompanyDataControllerApi::class.java)
        val companyRolesManagerMock = mock(CompanyRolesManager::class.java)
        val objectMapper = ObjectMapper()
        val proxyPrimaryUrl = "dummy"

        notificationService = NotificationService(
            cloudEventMessageHandlerMock,
            notificationEventRepository,
            elementaryEventRepository,
            companyDataControllerApiMock,
            companyRolesManagerMock,
            objectMapper,
            notificationThresholdDays,
            elementaryEventsThreshold,
            proxyPrimaryUrl,
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

    private fun setNotificationEventRepoMockReturnValue(
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

    @Test
    fun `single mail if no notification event in last 30 days and one unprocessed elementary event`() {
        setNotificationEventRepoMockReturnValue(emptyList())

        val unprocessedElementaryEvents = listOf(
            createUploadElementaryEventEntity(0),
        )
        val notificationEmailType =
            notificationService.checkNotificationRequirementsAndDetermineNotificationEmailType(
                unprocessedElementaryEvents,
            )
        assertEquals(NotificationService.NotificationEmailType.Single, notificationEmailType)
    }

    @Test
    fun `summary mail if no notification event in last 30 days and two unprocessed elementary events`() {
        val notificationEvent = createNotificationEventEntityForDataUploads(400)
        setNotificationEventRepoMockReturnValue(listOf(notificationEvent))

        val unprocessedElementaryEvents = listOf(
            createUploadElementaryEventEntity(60),
            createUploadElementaryEventEntity(0),
        )
        val notificationEmailType =
            notificationService.checkNotificationRequirementsAndDetermineNotificationEmailType(
                unprocessedElementaryEvents,
            )
        assertEquals(NotificationService.NotificationEmailType.Summary, notificationEmailType)
    }

    @Test
    fun `summary mail if notification event in last 30 days and unprocessed elementary events reach threshold`() {
        val notificationEvent = createNotificationEventEntityForDataUploads(29)
        setNotificationEventRepoMockReturnValue(listOf(notificationEvent))

        val unprocessedElementaryEvents = mutableListOf<ElementaryEventEntity>()
        for (creationTimeInDaysBeforeNow in 29 downTo 21) {
            unprocessedElementaryEvents.add(
                createUploadElementaryEventEntity(creationTimeInDaysBeforeNow),
            )
        }
        val notificationEmailTypeForNineElementaryEvents =
            notificationService.checkNotificationRequirementsAndDetermineNotificationEmailType(
                unprocessedElementaryEvents,
            )
        assertEquals(null, notificationEmailTypeForNineElementaryEvents)

        unprocessedElementaryEvents.add(
            createUploadElementaryEventEntity(0),
        )
        val notificationEmailTypeForTenElementaryEvents =
            notificationService.checkNotificationRequirementsAndDetermineNotificationEmailType(
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
        setNotificationEventRepoMockReturnValue(notificationEvents)

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
        setNotificationEventRepoMockReturnValue(notificationEvents)

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
        setNotificationEventRepoMockReturnValue(notificationEvents)

        assertTrue(
            notificationService.isLastNotificationEventOlderThanThreshold(
                testCompanyId,
                ElementaryEventType.UploadEvent,
            ),
        )

        notificationEvents.add(createNotificationEventEntityForDataUploads(30))
        setNotificationEventRepoMockReturnValue(notificationEvents)

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
            "${DataTypeEnum.heimathafen}: 2021 2021 2023\n" +
                "${DataTypeEnum.sfdr}: 2024\n" +
                "${DataTypeEnum.lksg}: 2020"

        val outputString = notificationService.createFrameworkAndYearStringFromElementaryEvents(elementaryEvents)

        assertEquals(expectedOutputString, outputString)
    }
}

package org.dataland.datalandcommunitymanager.services

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
import org.dataland.datalandcommunitymanager.services.messaging.NotificationEmailSender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.ArgumentMatchers.any
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

    private val testDataMetaInformation =
        DataMetaInformation(
            UUID.randomUUID().toString(),
            testCompanyId.toString(),
            testDataType,
            Instant.now().toEpochMilli(),
            testReportingPeriod,
            false,
            QaStatus.Pending,
            "test",
        )

    private val testCompanyInformation =
        CompanyInformation(
            companyName = "Dummy Company",
            headquarters = "Berlin",
            identifiers = emptyMap(),
            countryCode = "DE",
            companyContactDetails = listOf("emailAddress@dummymail.de", "emailAddress@fakemailingfortest.de"),
        )

    @BeforeAll
    fun setupNotificationService() {
        assertAssumptionsForTests()

        val notificationEventRepository = mock(NotificationEventRepository::class.java)
        val elementaryEventRepository = mock(ElementaryEventRepository::class.java)
        val companyRolesManager = mock(CompanyRolesManager::class.java)
        val metaDataControllerApiMock = mock(MetaDataControllerApi::class.java)
        val companyDataControllerApiMock = mock(CompanyDataControllerApi::class.java)
        val notificationEmailSender = mock(NotificationEmailSender::class.java)

        notificationService =
            NotificationService(
                notificationEventRepository,
                elementaryEventRepository,
                companyDataControllerApiMock,
                notificationEmailSender,
                companyRolesManager,
                notificationThresholdDays,
                elementaryEventsThreshold,
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
    ): ElementaryEventEntity =
        ElementaryEventEntity(
            elementaryEventType = ElementaryEventType.UploadEvent,
            companyId = testCompanyId,
            framework = framework,
            reportingPeriod = reportingPeriod,
            creationTimestamp =
                Instant.now().minus(creationTimeInDaysBeforeNow.toLong(), ChronoUnit.DAYS).toEpochMilli(),
            notificationEvent = null,
        )

    private fun createNotificationEventEntityForDataUploads(creationTimeInDaysBeforeNow: Long): NotificationEventEntity =
        NotificationEventEntity(
            companyId = testCompanyId,
            elementaryEventType = ElementaryEventType.UploadEvent,
            creationTimestamp = Instant.now().minus(creationTimeInDaysBeforeNow, ChronoUnit.DAYS).toEpochMilli(),
        )

    private fun setTheReturnValueForNotificationEventRepoQuery(notificationEventEntitiesToReturn: List<NotificationEventEntity>) {
        `when`(
            notificationService.notificationEventRepository
                .findNotificationEventByCompanyIdAndElementaryEventType(testCompanyId, ElementaryEventType.UploadEvent),
        ).thenReturn(
            notificationEventEntitiesToReturn,
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

        val unprocessedElementaryEvents =
            listOf(
                createUploadElementaryEventEntity(60),
                latestElementaryEvent,
            )
        val notificationEmailType =
            notificationService.determineNotificationEmailType(
                latestElementaryEvent,
                unprocessedElementaryEvents,
            )
        assertEquals(NotificationService.NotificationEmailType.Summary(400), notificationEmailType)
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
        assertEquals(NotificationService.NotificationEmailType.Summary(29), notificationEmailTypeForTenElementaryEvents)
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
    fun `counting the days passed since the last notification event works as expected`() {
        val expectedDaysPassed: Long = 12
        val notificationEvent = createNotificationEventEntityForDataUploads(expectedDaysPassed)
        val daysPassedSinceLastNotificationEvent =
            notificationService.getDaysPassedSinceNotificationEvent(notificationEvent)

        assertEquals(expectedDaysPassed, daysPassedSinceLastNotificationEvent)
    }

    @Test
    fun `asserting that the check if last notification event is older than threshold works as expected`() {
        val tooOldNotificationEvent = createNotificationEventEntityForDataUploads(31)
        assertTrue(notificationService.isNotificationEventOlderThanThreshold(tooOldNotificationEvent))

        val tooYoungNotificationEvent = createNotificationEventEntityForDataUploads(30)
        assertFalse(notificationService.isNotificationEventOlderThanThreshold(tooYoungNotificationEvent))
    }

    @Test
    fun `creating and storing a notification event works and is also reflected in the associated elementary events`() {
        val latestElementaryEvent = createUploadElementaryEventEntity(10)

        val unprocessedElementaryEvents =
            listOf(
                latestElementaryEvent,
                createUploadElementaryEventEntity(11),
            )

        lateinit var storedNotificationEventEntity: NotificationEventEntity

        `when`(notificationService.notificationEventRepository.saveAndFlush(any(NotificationEventEntity::class.java)))
            .thenAnswer { invocation ->
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
}

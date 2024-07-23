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
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.junit.jupiter.api.Assertions.assertEquals
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
        val cloudEventMessageHandlerMock = mock(CloudEventMessageHandler::class.java)
        val notificationEventRepository = mock(NotificationEventRepository::class.java)
        val metaDataControllerApiMock = mock(MetaDataControllerApi::class.java)
        val companyDataControllerApiMock = mock(CompanyDataControllerApi::class.java)
        val objectMapper = ObjectMapper()
        val notificationThresholdDays = 30
        val elementaryEventsThreshold = 10
        val proxyPrimaryUrl = "dummy"

        notificationService = NotificationService(
            cloudEventMessageHandlerMock,
            notificationEventRepository,
            companyDataControllerApiMock,
            objectMapper,
            notificationThresholdDays,
            elementaryEventsThreshold,
            proxyPrimaryUrl,
        )
        `when`(metaDataControllerApiMock.getDataMetaInfo(anyString())).thenReturn(testDataMetaInformation)
        `when`(companyDataControllerApiMock.getCompanyInfo(testCompanyId.toString())).thenReturn(testCompanyInformation)
    }

    private fun createUploadElementaryEventEntity(
        creationTimeInDaysBeforeNow: Int,
        notificationEventEntity: NotificationEventEntity? = null,
    ): ElementaryEventEntity {
        return ElementaryEventEntity(
            elementaryEventType = ElementaryEventType.UploadEvent,
            companyId = testCompanyId,
            framework = testDataType,
            reportingPeriod = testReportingPeriod,
            creationTimestamp =
            Instant.now().minus(creationTimeInDaysBeforeNow.toLong(), ChronoUnit.DAYS).toEpochMilli(),
            notificationEvent = notificationEventEntity,
        )
    }

    private fun createNotificationEventEntityForDataUploads(
        creationTimeInDaysBeforeNow: Long,
        elementaryEventEntities: List<ElementaryEventEntity>,
    ): NotificationEventEntity {
        return NotificationEventEntity(
            companyId = testCompanyId,
            elementaryEventType = ElementaryEventType.UploadEvent,
            creationTimestamp = Instant.now().minus(creationTimeInDaysBeforeNow, ChronoUnit.DAYS).toEpochMilli(),
            elementaryEvents = elementaryEventEntities,
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
    fun `single mail if no notification event in last 30 days and no past unprocessed elementary events`() {
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
    fun `summary mail if no notification event in last 30 days and one past unprocessed elementary event`() {
        val processedElementaryEvents = listOf(
            createUploadElementaryEventEntity(400),
            createUploadElementaryEventEntity(401),
        )
        val notificationEvent = createNotificationEventEntityForDataUploads(400, processedElementaryEvents)
        processedElementaryEvents.forEach { it.notificationEvent = notificationEvent }
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
    fun `no mail if one notification event in last 30 days but unprocessed elementary events less than threshold`() {
        val processedElementaryEvents = listOf(
            createUploadElementaryEventEntity(29),
            createUploadElementaryEventEntity(30),
        )
        val notificationEvent = createNotificationEventEntityForDataUploads(29, processedElementaryEvents)
        processedElementaryEvents.forEach { it.notificationEvent = notificationEvent }
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
    fun `no mail if notification event with many elementary events in last 30 days but threshold still not reached`() {
        val processedElementaryEvents = (29 downTo 12).map { creationTimeInDaysBeforeNow ->
            createUploadElementaryEventEntity(creationTimeInDaysBeforeNow)
        }
        val notificationEvent = createNotificationEventEntityForDataUploads(12, processedElementaryEvents)
        processedElementaryEvents.forEach { it.notificationEvent = notificationEvent }
        setNotificationEventRepoMockReturnValue(listOf(notificationEvent))

        val unprocessedElementaryEvents = (9 downTo 1).map { creationTimeInDaysBeforeNow ->
            createUploadElementaryEventEntity(creationTimeInDaysBeforeNow)
        }

        val notificationEmailTypeForNineUnprocessedElementaryEvents =
            notificationService.checkNotificationRequirementsAndDetermineNotificationEmailType(
                unprocessedElementaryEvents,
            )

        assertEquals(null, notificationEmailTypeForNineUnprocessedElementaryEvents)
    }
}

package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.entities.RequestStatusEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestStatusObject
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandcommunitymanager.services.messaging.CompanyOwnershipClaimDatasetUploadedSender
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestSummaryEmailSender
import org.dataland.datalandcommunitymanager.utils.TestUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatcher
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import java.util.UUID
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationServiceTest {
    private lateinit var notificationEventRepository: NotificationEventRepository
    private lateinit var companyRolesManager: CompanyRolesManager
    private lateinit var companyDataControllerApi: CompanyDataControllerApi
    private lateinit var notificationEmailSender: CompanyOwnershipClaimDatasetUploadedSender
    private lateinit var dataRequestSummaryEmailSender: DataRequestSummaryEmailSender
    private lateinit var notificationService: NotificationService

    private val userUUID = UUID.randomUUID()
    private val companyUUID = UUID.randomUUID()

    @BeforeEach
    fun setupNotificationService() {
        notificationEventRepository = mock(NotificationEventRepository::class.java)
        companyRolesManager = mock(CompanyRolesManager::class.java)
        companyDataControllerApi = mock(CompanyDataControllerApi::class.java)
        notificationEmailSender = mock(CompanyOwnershipClaimDatasetUploadedSender::class.java)
        dataRequestSummaryEmailSender = mock(DataRequestSummaryEmailSender::class.java)

        notificationService =
            NotificationService(
                notificationEventRepository, companyRolesManager, companyDataControllerApi, notificationEmailSender,
                dataRequestSummaryEmailSender,
            )
    }

    @Test
    fun `Test scheduledWeeklyEmailSending with no message`() {
        notificationService.scheduledWeeklyEmailSending()

        verifyNoMoreInteractions(notificationEmailSender)
        verifyNoMoreInteractions(dataRequestSummaryEmailSender)
    }

    @Test
    fun `Test scheduledWeeklyEmailSending with messages`() {
        val notificationEventEntity =
            NotificationEventEntity(
                UUID.randomUUID(),
                NotificationEventType.AvailableEvent,
                userUUID,
                false,
                companyUUID,
                DataTypeEnum.lksg,
                "2024",
            )
        val entityList = listOf(notificationEventEntity, notificationEventEntity)
        val companyInformation =
            CompanyInformation(
                companyName = "Sample Company",
                headquarters = "headquarters",
                identifiers = mapOf(),
                countryCode = "DE",
                companyContactDetails = listOf("sampleCompany@example.com"),
            )
        `when`(notificationEventRepository.findAllByNotificationEventTypesAndIsProcessedFalse(any()))
            .thenReturn(entityList)
        `when`(companyDataControllerApi.getCompanyInfo(companyUUID.toString())).thenReturn(companyInformation)

        notificationService.scheduledWeeklyEmailSending()

        // One E-mail should be sent with both events for the same company
        verify(
            notificationEmailSender, times(1),
        ).sendExternalAndInternalInvestorRelationshipSummaryEmail(
            unprocessedEvents = eq(entityList),
            companyId = eq(companyUUID),
            receiver = eq(listOf("sampleCompany@example.com")),
            correlationId = any(),
        )
        verify(
            dataRequestSummaryEmailSender, times(1),
        ).sendDataRequestSummaryEmail(
            unprocessedEvents = eq(entityList),
            userId = eq(userUUID),
        )
        verify(notificationEventRepository, times(2)).saveAll<NotificationEventEntity>(any())
    }

    @ParameterizedTest()
    @MethodSource("provideInputForCreateUserSpecificNotificationEvent")
    fun `Test createUserSpecificNotificationEvent`(
        requestStatusBefore: RequestStatus,
        requestStatusAfter: RequestStatus,
        immediateNotificationWasSent: Boolean,
        earlierQaApprovedVersionExists: Boolean,
    ) {
        TestUtils.mockSecurityContext()
        val dataRequestEntity =
            DataRequestEntity(
                userId = userUUID.toString(),
                creationTimestamp = 123,
                dataType = DataTypeEnum.lksg.toString(),
                reportingPeriod = "2024",
                datalandCompanyId = companyUUID.toString(),
                emailOnUpdate = false,
            )
        val storedDataRequestStatusObject = StoredDataRequestStatusObject(requestStatusBefore, 123, AccessStatus.Public, null, null)
        val requestStatusEntity = RequestStatusEntity(storedDataRequestStatusObject, dataRequestEntity)
        dataRequestEntity.addToDataRequestStatusHistory(requestStatusEntity)
        val notificationEventEntity =
            NotificationEventEntity(
                notificationEventType = NotificationEventType.UpdatedEvent,
                userId = userUUID,
                isProcessed = immediateNotificationWasSent,
                companyId = companyUUID,
                framework = DataTypeEnum.lksg,
                reportingPeriod = "2024",
            )

        notificationService.createUserSpecificNotificationEvent(
            dataRequestEntity,
            requestStatusAfter,
            immediateNotificationWasSent,
            earlierQaApprovedVersionExists,
        )

        verify(notificationEventRepository, times(1)).save(argThat(NotificationEventEntityMatcher(notificationEventEntity)))
    }

    companion object {
        @JvmStatic
        fun provideInputForCreateUserSpecificNotificationEvent(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    RequestStatus.Open,
                    RequestStatus.Answered,
                    true,
                    true,
                ),
            )
    }

    class NotificationEventEntityMatcher(
        private val expected: NotificationEventEntity,
    ) : ArgumentMatcher<NotificationEventEntity> {
        override fun matches(given: NotificationEventEntity): Boolean =
            expected.notificationEventType == given.notificationEventType &&
                expected.userId == given.userId &&
                expected.isProcessed == given.isProcessed &&
                expected.companyId == given.companyId &&
                expected.framework == given.framework &&
                expected.reportingPeriod == given.reportingPeriod
    }

    /* This entire test needs to be reviewed to see which test functions (if any) we still need.
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
        val uploadEventRepository = mock(UploadEventRepository::class.java)
        val companyRolesManager = mock(CompanyRolesManager::class.java)
        val metaDataControllerApiMock = mock(MetaDataControllerApi::class.java)
        val companyDataControllerApiMock = mock(CompanyDataControllerApi::class.java)
        val notificationEmailSender = mock(NotificationEmailSender::class.java)

        notificationService =
            NotificationService(
                notificationEventRepository,
                uploadEventRepository,
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
            elementaryEventType = NotificationEventType.UploadEvent,
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
            elementaryEventType = NotificationEventType.UploadEvent,
            creationTimestamp = Instant.now().minus(creationTimeInDaysBeforeNow, ChronoUnit.DAYS).toEpochMilli(),
        )

    private fun setTheReturnValueForNotificationEventRepoQuery(notificationEventEntitiesToReturn: List<NotificationEventEntity>) {
        `when`(
            notificationService.notificationEventRepository
                .findNotificationEventByCompanyIdAndElementaryEventType(testCompanyId, NotificationEventType.UploadEvent),
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
            notificationService.getLastNotificationEventOrNull(testCompanyId, NotificationEventType.UploadEvent)

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
                assertEquals(NotificationEventType.UploadEvent, storedNotificationEventEntity.elementaryEventType)

                storedNotificationEventEntity
            }

        `when`(notificationService.uploadEventRepository.saveAndFlush(any(ElementaryEventEntity::class.java)))
            .thenAnswer { invocation ->
                val elementaryEventEntityToStore = invocation.getArgument<ElementaryEventEntity>(0)

                assertEquals(storedNotificationEventEntity, elementaryEventEntityToStore.notificationEvent)

                elementaryEventEntityToStore
            }

        notificationService.createNotificationEventAndReferenceIt(latestElementaryEvent, unprocessedElementaryEvents)
    }
     */
}

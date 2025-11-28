package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalanduserservice.entity.NotificationEventEntity
import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.dataland.datalanduserservice.model.enums.NotificationEventType
import org.dataland.datalanduserservice.model.enums.NotificationFrequency
import org.dataland.datalanduserservice.repository.NotificationEventRepository
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID
import java.util.stream.Stream
import kotlin.reflect.KFunction

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationSchedulerTest {
    companion object {
        const val PORTFOLIONAME1 = "OnlySFDR"
        const val PORTFOLIONAME2 = "NoNotificationExpected"
        const val PORTFOLIONAME3 = "AllNotificationsExpected"
        val companyId1: UUID = UUID.randomUUID()
        val companyId2: UUID = UUID.randomUUID()
        val userId1: UUID = UUID.randomUUID()
        val userId2: UUID = UUID.randomUUID()
        val userId3: UUID = UUID.randomUUID()
        val mockPortfolio1 =
            PortfolioEntity(
                UUID.randomUUID(),
                PORTFOLIONAME1,
                userId1.toString(),
                Instant.now().toEpochMilli(),
                Instant.now().toEpochMilli(),
                mutableSetOf(companyId1.toString(), companyId2.toString()),
                true,
                setOf("sfdr"),
            )

        val mockPortfolio2 =
            PortfolioEntity(
                UUID.randomUUID(),
                PORTFOLIONAME2,
                userId2.toString(),
                Instant.now().toEpochMilli(),
                Instant.now().toEpochMilli(),
                mutableSetOf(companyId1.toString()),
                true,
                setOf("eutaxonomy"),
            )

        val mockPortfolio3 =
            PortfolioEntity(
                UUID.randomUUID(),
                PORTFOLIONAME3,
                userId3.toString(),
                Instant.now().toEpochMilli(),
                Instant.now().toEpochMilli(),
                mutableSetOf(companyId1.toString(), companyId2.toString()),
                true,
                setOf("sfdr", "eutaxonomy"),
            )

        val mockNotificationEventEntities =
            listOf(
                NotificationEventEntity(
                    UUID.randomUUID(),
                    NotificationEventType.AvailableEvent,
                    companyId1,
                    DataTypeEnum.sfdr,
                    "2025",
                ),
                NotificationEventEntity(
                    UUID.randomUUID(),
                    NotificationEventType.AvailableEvent,
                    companyId2,
                    DataTypeEnum.sfdr,
                    "2024",
                ),
                NotificationEventEntity(
                    UUID.randomUUID(),
                    NotificationEventType.AvailableEvent,
                    companyId2,
                    DataTypeEnum.eutaxonomyMinusFinancials,
                    "2024",
                ),
            )
    }

    private val mockDataRequestSummaryEmailBuilder = mock<DataRequestSummaryEmailBuilder>()
    private val mockPortfolioRepository = mock<PortfolioRepository>()
    private val mockNotificationEventRepository = mock<NotificationEventRepository>()
    private val notificationScheduler =
        NotificationScheduler(mockDataRequestSummaryEmailBuilder, mockPortfolioRepository, mockNotificationEventRepository)

    data class TestArgument(
        val function: KFunction<Unit>,
        val notificationFrequency: NotificationFrequency,
        val mockPortfolioList: List<PortfolioEntity>,
    )

    @BeforeEach
    fun setUp() {
        reset(mockDataRequestSummaryEmailBuilder)
    }

    fun eMailSchedulerParameters(): Stream<TestArgument> =
        Stream.of(
            TestArgument(
                NotificationScheduler::scheduledDailyEmailSending,
                NotificationFrequency.Daily,
                listOf(mockPortfolio1, mockPortfolio2, mockPortfolio3),
            ),
            TestArgument(
                NotificationScheduler::scheduledWeeklyEmailSending,
                NotificationFrequency.Weekly,
                listOf(mockPortfolio1, mockPortfolio2, mockPortfolio3),
            ),
            TestArgument(
                NotificationScheduler::scheduledMonthlyEmailSending,
                NotificationFrequency.Monthly,
                listOf(mockPortfolio1, mockPortfolio2, mockPortfolio3),
            ),
        )

    @ParameterizedTest
    @MethodSource("eMailSchedulerParameters")
    fun `test scheduledWeeklyEmailSending does create the expected messages`(ta: TestArgument) {
        whenever(mockPortfolioRepository.findAllByNotificationFrequencyAndIsMonitoredIsTrue(eq(ta.notificationFrequency)))
            .thenReturn(ta.mockPortfolioList)
        whenever(mockNotificationEventRepository.findAllByFrameworkAndCompanyIdInAndCreationTimestampGreaterThan(any(), any(), any()))
            .thenAnswer { invocation: org.mockito.invocation.InvocationOnMock ->
                val framework = invocation.getArgument<DataTypeEnum>(0)
                val companyIds = invocation.getArgument<List<UUID>>(1)
                mockNotificationEventEntities.filter {
                    it.framework == framework && it.companyId in companyIds
                }
            }

        ta.function.call(notificationScheduler)

        val notificationCaptor = argumentCaptor<List<NotificationEventEntity>>()
        val userIdCaptor = argumentCaptor<UUID>()
        val portfolioNamesCaptor = argumentCaptor<String>()
        verify(mockDataRequestSummaryEmailBuilder, times(2)).buildDataRequestSummaryEmailAndSendCEMessage(
            notificationCaptor.capture(),
            userIdCaptor.capture(),
            eq(ta.notificationFrequency),
            portfolioNamesCaptor.capture(),
        )
        assertEquals(listOf(PORTFOLIONAME1, PORTFOLIONAME3), portfolioNamesCaptor.allValues)
        assertEquals(listOf(userId1, userId3), userIdCaptor.allValues)
        assertEquals(listOf(mockNotificationEventEntities[0], mockNotificationEventEntities[1]), notificationCaptor.firstValue)
        assertEquals(mockNotificationEventEntities, notificationCaptor.secondValue)
    }
}

package org.dataland.datalanduserservice.service

import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalanduserservice.entity.NotificationEventEntity
import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.dataland.datalanduserservice.model.enums.NotificationEventType
import org.dataland.datalanduserservice.model.enums.NotificationFrequency
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationSchedulerTest {
    companion object {
        val companyId1: UUID = UUID.randomUUID()
        val companyId2: UUID = UUID.randomUUID()
        val userId1: UUID = UUID.randomUUID()
        val userId2: UUID = UUID.randomUUID()
        val userId3: UUID = UUID.randomUUID()
        val mockPortfolio1 =
            PortfolioEntity(
                UUID.randomUUID(),
                "portfolio1",
                userId1.toString(),
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                mutableSetOf(companyId1.toString(), companyId2.toString()),
                true,
                setOf("sfdr"),
            )

        val mockPortfolio2 =
            PortfolioEntity(
                UUID.randomUUID(),
                "portfolio2",
                userId2.toString(),
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                mutableSetOf(companyId1.toString(), companyId2.toString()),
                true,
                setOf("eutaxonomyMinusFinancials"),
            )

        val mockPortfolio3 =
            PortfolioEntity(
                UUID.randomUUID(),
                "portfolio3",
                userId3.toString(),
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                mutableSetOf(companyId1.toString(), companyId2.toString()),
                true,
                setOf("sfdr", "eutaxonomyMinusFinancials"),
            )

        val mockNotificationEventEntity1 =
            NotificationEventEntity(
                UUID.randomUUID(),
                NotificationEventType.AvailableEvent,
                UUID.randomUUID(),
                companyId1,
                DataTypeEnum.sfdr,
                "2025",
            )
        val mockNotificationEventEntity2 =
            NotificationEventEntity(
                UUID.randomUUID(),
                NotificationEventType.AvailableEvent,
                UUID.randomUUID(),
                companyId2,
                DataTypeEnum.sfdr,
                "2024",
            )
    }

    private val mockDataRequestSummaryEmailBuilder = mock<DataRequestSummaryEmailBuilder>()
    private val mockPortfolioRepository = mock<PortfolioRepository>()
    private val mockEntityManager = mock<EntityManager>()
    private val notificationScheduler =
        NotificationScheduler(mockDataRequestSummaryEmailBuilder, mockPortfolioRepository, mockEntityManager)

    data class TestArgument(
        val function: () -> Unit,
        val notificationFrequency: NotificationFrequency,
        val mockPortfolioList: List<PortfolioEntity>,
    )

    @BeforeEach
    fun setUp() {
        reset(mockDataRequestSummaryEmailBuilder)
    }

    fun eMailSchedulerTwoDimensionalParameters(): Stream<TestArgument> =
        Stream.of(
            TestArgument(
                { notificationScheduler.scheduledDailyEmailSending() },
                NotificationFrequency.Daily,
                listOf(mockPortfolio1, mockPortfolio2, mockPortfolio3),
            ),
            TestArgument(
                { notificationScheduler.scheduledWeeklyEmailSending() },
                NotificationFrequency.Weekly,
                listOf(mockPortfolio1, mockPortfolio2, mockPortfolio3),
            ),
            TestArgument(
                { notificationScheduler.scheduledMonthlyEmailSending() },
                NotificationFrequency.Monthly,
                listOf(mockPortfolio1, mockPortfolio2, mockPortfolio3),
            ),
        )

    @ParameterizedTest
    @MethodSource("eMailSchedulerTwoDimensionalParameters")
    fun `test scheduledWeeklyEmailSending does create the expected messages`(ta: TestArgument) {
        val mockQuery = mock(Query::class.java)
        whenever(mockPortfolioRepository.findAllByNotificationFrequencyAndIsMonitoredIsTrue(eq(ta.notificationFrequency)))
            .thenReturn(ta.mockPortfolioList)
        whenever(mockEntityManager.createNativeQuery(any(), eq(NotificationEventEntity::class.java))).thenReturn(mockQuery)
        whenever(mockQuery.resultList)
            .thenReturn(listOf(mockNotificationEventEntity1, mockNotificationEventEntity2))
        assert(mockNotificationEventEntity1.userId != mockNotificationEventEntity2.userId)

        ta.function()

        verify(mockDataRequestSummaryEmailBuilder, times(3)).buildDataRequestSummaryEmailAndSendCEMessage(
            any<List<NotificationEventEntity>>(),
            any<UUID>(),
            eq(ta.notificationFrequency),
            any<String>(),
        )
    }
}

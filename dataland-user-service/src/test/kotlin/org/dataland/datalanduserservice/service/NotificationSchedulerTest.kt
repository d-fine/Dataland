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
        val companyIdOnlySfdr: UUID = UUID.randomUUID()
        val companyIdSfdrAndEuTaxo: UUID = UUID.randomUUID()
        val portfolioOnlySfdr =
            PortfolioEntity(
                UUID.randomUUID(),
                "OnlySFDR",
                UUID.randomUUID().toString(),
                Instant.now().toEpochMilli(),
                Instant.now().toEpochMilli(),
                mutableSetOf(companyIdOnlySfdr.toString(), companyIdSfdrAndEuTaxo.toString()),
                true,
                setOf("sfdr"),
            )

        val portfolioNoNotifications =
            PortfolioEntity(
                UUID.randomUUID(),
                "NoNotificationExpected",
                UUID.randomUUID().toString(),
                Instant.now().toEpochMilli(),
                Instant.now().toEpochMilli(),
                mutableSetOf(companyIdOnlySfdr.toString()),
                true,
                setOf("eutaxonomy"),
            )

        val portfolioAllNotifications =
            PortfolioEntity(
                UUID.randomUUID(),
                "AllNotificationsExpected",
                UUID.randomUUID().toString(),
                Instant.now().toEpochMilli(),
                Instant.now().toEpochMilli(),
                mutableSetOf(companyIdOnlySfdr.toString(), companyIdSfdrAndEuTaxo.toString()),
                true,
                setOf("sfdr", "eutaxonomy"),
            )

        val mockNotificationEventEntities =
            listOf(
                NotificationEventEntity(
                    UUID.randomUUID(),
                    NotificationEventType.AvailableEvent,
                    companyIdOnlySfdr,
                    DataTypeEnum.sfdr,
                    "2025",
                ),
                NotificationEventEntity(
                    UUID.randomUUID(),
                    NotificationEventType.AvailableEvent,
                    companyIdSfdrAndEuTaxo,
                    DataTypeEnum.sfdr,
                    "2024",
                ),
                NotificationEventEntity(
                    UUID.randomUUID(),
                    NotificationEventType.AvailableEvent,
                    companyIdSfdrAndEuTaxo,
                    DataTypeEnum.eutaxonomyMinusFinancials,
                    "2024",
                ),
            )
    }

    private val mockPortfolioUpdateSummaryEmailBuilder = mock<PortfolioUpdateSummaryEmailBuilder>()
    private val mockPortfolioRepository = mock<PortfolioRepository>()
    private val mockNotificationEventRepository = mock<NotificationEventRepository>()
    private val notificationScheduler =
        NotificationScheduler(mockPortfolioUpdateSummaryEmailBuilder, mockPortfolioRepository, mockNotificationEventRepository)

    data class TestArgument(
        val function: KFunction<Unit>,
        val notificationFrequency: NotificationFrequency,
        val mockPortfolioList: List<PortfolioEntity>,
    )

    @BeforeEach
    fun setUp() {
        reset(mockPortfolioUpdateSummaryEmailBuilder)
    }

    fun eMailSchedulerParameters(): Stream<TestArgument> =
        Stream.of(
            TestArgument(
                NotificationScheduler::scheduledDailyEmailSending,
                NotificationFrequency.Daily,
                listOf(portfolioOnlySfdr, portfolioNoNotifications, portfolioAllNotifications),
            ),
            TestArgument(
                NotificationScheduler::scheduledWeeklyEmailSending,
                NotificationFrequency.Weekly,
                listOf(portfolioOnlySfdr, portfolioNoNotifications, portfolioAllNotifications),
            ),
            TestArgument(
                NotificationScheduler::scheduledMonthlyEmailSending,
                NotificationFrequency.Monthly,
                listOf(portfolioOnlySfdr, portfolioNoNotifications, portfolioAllNotifications),
            ),
        )

    @ParameterizedTest
    @MethodSource("eMailSchedulerParameters")
    fun `test scheduled email sending does create the expected messages`(ta: TestArgument) {
        whenever(mockPortfolioRepository.findAllByNotificationFrequencyAndIsMonitoredIsTrue(eq(ta.notificationFrequency)))
            .thenReturn(ta.mockPortfolioList)
        whenever(mockNotificationEventRepository.findAllByFrameworkInAndCompanyIdInAndCreationTimestampGreaterThan(any(), any(), any()))
            .thenAnswer { invocation: org.mockito.invocation.InvocationOnMock ->
                val frameworks = invocation.getArgument<List<DataTypeEnum>>(0)
                val companyIds = invocation.getArgument<List<UUID>>(1)
                mockNotificationEventEntities.filter {
                    it.framework in frameworks && it.companyId in companyIds
                }
            }

        ta.function.call(notificationScheduler)

        val notificationCaptor = argumentCaptor<List<NotificationEventEntity>>()
        val userIdCaptor = argumentCaptor<UUID>()
        val portfolioNamesCaptor = argumentCaptor<String>()
        verify(mockPortfolioUpdateSummaryEmailBuilder, times(2)).buildPortfolioMonitoringUpdateSummaryEmailAndSendCEMessage(
            notificationCaptor.capture(),
            userIdCaptor.capture(),
            eq(ta.notificationFrequency),
            portfolioNamesCaptor.capture(),
        )
        assertEquals(listOf(portfolioOnlySfdr.portfolioName, portfolioAllNotifications.portfolioName), portfolioNamesCaptor.allValues)
        assertEquals(listOf(portfolioOnlySfdr.userId, portfolioAllNotifications.userId), userIdCaptor.allValues.map { it.toString() })
        assertEquals(listOf(mockNotificationEventEntities[0], mockNotificationEventEntities[1]), notificationCaptor.firstValue)
        assertEquals(mockNotificationEventEntities, notificationCaptor.secondValue)
    }
}

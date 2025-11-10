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
import org.junit.jupiter.api.Test
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

class NotificationSchedulerTest {
    companion object {
        val companyId1 = UUID.randomUUID()
        val mockPortfolio1 =
            PortfolioEntity(
                UUID.randomUUID(),
                "portfolio1",
                UUID.randomUUID().toString(),
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                mutableSetOf(companyId1.toString()),
                true,
                setOf("sfdr"),
            )

        val mockNotificationEventEntity =
            NotificationEventEntity(
                UUID.randomUUID(),
                NotificationEventType.AvailableEvent,
                UUID.randomUUID(),
                companyId1,
                DataTypeEnum.sfdr,
                "2025",
            )
    }

    private val mockDataRequestSummaryEmailBuilder = mock<DataRequestSummaryEmailBuilder>()
    private val mockPortfolioRepository = mock<PortfolioRepository>()
    private val mockEntityManager = mock<EntityManager>()
    private val notificationScheduler =
        NotificationScheduler(mockDataRequestSummaryEmailBuilder, mockPortfolioRepository, mockEntityManager)

    @BeforeEach
    fun setUp() {
        reset(mockDataRequestSummaryEmailBuilder)
    }

    @Test
    fun `test scheduledWeeklyEmailSending does create the expected messages`() {
        val mockPortfolioList = listOf(mockPortfolio1)
        val mockQuery = mock(Query::class.java)
        whenever(mockPortfolioRepository.findAllByNotificationFrequencyAndMonitoredIsTrue(eq(NotificationFrequency.Weekly)))
            .thenReturn(mockPortfolioList)
        whenever(mockEntityManager.createNativeQuery(any(), eq(NotificationEventEntity::class.java))).thenReturn(mockQuery)
        whenever(mockQuery.resultList).thenReturn(listOf(mockNotificationEventEntity))

        notificationScheduler.scheduledWeeklyEmailSending()

        verify(mockDataRequestSummaryEmailBuilder, times(1)).buildDataRequestSummaryEmailAndSendCEMessage(
            any<List<NotificationEventEntity>>(),
            any<UUID>(),
            eq(NotificationFrequency.Weekly),
            any<String>(),
        )
    }
}

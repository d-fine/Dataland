package org.dataland.datalanduserservice.service

import jakarta.persistence.EntityManager
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class NotificationSchedulerTest {
    private val mockDataRequestSummaryEmailBuilder = mock<DataRequestSummaryEmailBuilder>()
    private val mockPortfolioRepository = mock<PortfolioRepository>()
    private val mockEntityManager = mock<EntityManager>()
    private val notificationScheduler =
        NotificationScheduler(mockDataRequestSummaryEmailBuilder, mockPortfolioRepository, mockEntityManager)

    @Test
    fun `test scheduledWeeklyEmailSending does create the expected messages`() {
        notificationScheduler.scheduledWeeklyEmailSending()
    }
}

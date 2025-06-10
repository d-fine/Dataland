package org.dataland.datalanduserservice.service

import org.dataland.datalanduserservice.exceptions.PortfolioNotFoundApiException
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.PortfolioMonitoringPatch
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PortfolioMonitoringServiceTest {
    private val mockPortfolioRepository = mock<PortfolioRepository>()
    private val mockSecurityContext = mock<SecurityContext>()
    private lateinit var portfolioMonitoringService: PortfolioMonitoringService

    private val dummyUserId = "userId"
    private val dummyPortfolioId = UUID.randomUUID().toString()
    private val dummyCorrelationId = UUID.randomUUID().toString()

    private val dummyPortfolio =
        BasePortfolio(
            portfolioId = dummyPortfolioId,
            portfolioName = "Portfolio",
            userId = dummyUserId,
            creationTimestamp = System.currentTimeMillis(),
            lastUpdateTimestamp = System.currentTimeMillis(),
            companyIds = mutableSetOf("companyId"),
            isMonitored = false,
            startingMonitoringPeriod = "2023",
            monitoredFrameworks = mutableSetOf("sfdr", "eutaxonomy"),
        )

    @BeforeEach
    fun setup() {
        resetSecurityContext()
        doAnswer { it.arguments[0] }.whenever(mockPortfolioRepository).save(any())
        portfolioMonitoringService = PortfolioMonitoringService(mockPortfolioRepository)
    }

    private fun resetSecurityContext() {
        val mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                "username",
                dummyUserId,
                setOf(),
            )
        doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `verify that patchMonitoring throws PortfolioNotFoundApiException if portfolio not found`() {
        val portfolioMonitoringPatch =
            PortfolioMonitoringPatch(
                isMonitored = true,
                startingMonitoringPeriod = "2022",
                monitoredFrameworks = mutableSetOf("sfdr"),
            )

        doReturn(null)
            .whenever(mockPortfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, UUID.fromString(dummyPortfolioId))

        assertThrows<PortfolioNotFoundApiException> {
            portfolioMonitoringService.patchMonitoring(dummyPortfolioId, portfolioMonitoringPatch, dummyCorrelationId)
        }
    }

    @Test
    fun `verify that non monitor values remain unchanged after patching monitoring`() {
        val originalPortfolio =
            dummyPortfolio.copy(
                portfolioName = "Original Portfolio",
                userId = dummyUserId,
                companyIds = mutableSetOf("companyA", "companyB"),
            )
        val portfolioMonitoringPatch =
            PortfolioMonitoringPatch(
                isMonitored = true,
                startingMonitoringPeriod = "2021",
                monitoredFrameworks = mutableSetOf("sfdr", "euro"),
            )

        doReturn(originalPortfolio.toPortfolioEntity())
            .whenever(mockPortfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, UUID.fromString(dummyPortfolio.portfolioId))

        val updatedPortfolio =
            portfolioMonitoringService.patchMonitoring(
                dummyPortfolio.portfolioId,
                portfolioMonitoringPatch,
                dummyCorrelationId,
            )

        assertEquals(originalPortfolio.portfolioName, updatedPortfolio.portfolioName)
        assertEquals(originalPortfolio.userId, updatedPortfolio.userId)
        assertEquals(originalPortfolio.companyIds, updatedPortfolio.companyIds)
        assertEquals(portfolioMonitoringPatch.isMonitored, updatedPortfolio.isMonitored)
        assertEquals(portfolioMonitoringPatch.startingMonitoringPeriod, updatedPortfolio.startingMonitoringPeriod)
        assertEquals(portfolioMonitoringPatch.monitoredFrameworks, updatedPortfolio.monitoredFrameworks)
    }
}

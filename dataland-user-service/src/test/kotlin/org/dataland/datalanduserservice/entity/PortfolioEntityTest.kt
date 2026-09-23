package org.dataland.datalanduserservice.entity

import org.dataland.datalanduserservice.model.enums.NotificationFrequency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class PortfolioEntityTest {
    private val portfolioId = UUID.randomUUID()
    private val companyId = "company-id"
    private val proxyCompanyId = "proxy-company-id"

    private fun buildEntity(
        monitoredFrameworks: Set<String>? = setOf("sfdr"),
        sharedUserIds: Set<String>? = setOf("shared-user"),
        proxyCompanyReplacements: MutableSet<ProxyCompanyReplacementEmbeddable> = mutableSetOf(),
    ) = PortfolioEntity(
        portfolioId = portfolioId,
        portfolioName = "Test Portfolio",
        userId = "user-id",
        creationTimestamp = 1L,
        lastUpdateTimestamp = 2L,
        companyIds = mutableSetOf(companyId),
        isMonitored = true,
        monitoredFrameworks = monitoredFrameworks,
        notificationFrequency = NotificationFrequency.Weekly,
        timeWindowThreshold = null,
        sharedUserIds = sharedUserIds,
        proxyCompanyReplacements = proxyCompanyReplacements,
    )

    @Test
    fun `test that toBasePortfolio converts proxyCompanyReplacements correctly`() {
        val replacement =
            ProxyCompanyReplacementEmbeddable(
                userId = "user-id",
                timestamp = 5L,
                proxiedCompanyId = companyId,
                proxyCompanyId = proxyCompanyId,
            )
        val entity = buildEntity(proxyCompanyReplacements = mutableSetOf(replacement))
        val basePortfolio = entity.toBasePortfolio()

        assertEquals(1, basePortfolio.proxyCompanyReplacements?.size)
        val convertedReplacement = basePortfolio.proxyCompanyReplacements?.first()
        assertEquals("user-id", convertedReplacement?.userId)
        assertEquals(5L, convertedReplacement?.timestamp)
        assertEquals(companyId, convertedReplacement?.proxiedCompanyId)
        assertEquals(proxyCompanyId, convertedReplacement?.proxyCompanyId)
    }

    @Test
    fun `test that toBasePortfolio yields null proxyCompanyReplacements when empty`() {
        val entity = buildEntity()
        val basePortfolio = entity.toBasePortfolio()
        assertNull(basePortfolio.proxyCompanyReplacements)
    }

    @Test
    fun `test that toBasePortfolio handles null monitoredFrameworks and sharedUserIds`() {
        val entity = buildEntity(monitoredFrameworks = null, sharedUserIds = null)
        val basePortfolio = entity.toBasePortfolio()
        assertTrue(basePortfolio.monitoredFrameworks.isEmpty())
        assertTrue(basePortfolio.sharedUserIds.isEmpty())
    }
}

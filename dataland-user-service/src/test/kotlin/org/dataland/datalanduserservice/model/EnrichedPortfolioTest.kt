package org.dataland.datalanduserservice.model

import org.dataland.datalanduserservice.model.enums.NotificationFrequency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EnrichedPortfolioTest {
    @Test
    fun `test that an EnrichedPortfolio can be constructed and its fields are accessible`() {
        val entry =
            EnrichedPortfolioEntry(
                companyId = "company-id",
                companyName = "Company Name",
                sector = "Sector",
                countryCode = "DE",
                companyCockpitRef = "cockpit-ref",
                frameworkHyphenatedNamesToDataRef = mapOf("sfdr" to "data-ref"),
                availableReportingPeriods = mapOf("sfdr" to "2023"),
            )
        val proxyCompanyReplacement =
            ProxyCompanyReplacement(
                userId = "user-id",
                timestamp = 1L,
                proxiedCompanyId = "proxied-company-id",
                proxyCompanyId = "proxy-company-id",
            )

        val enrichedPortfolio =
            EnrichedPortfolio(
                portfolioId = "portfolio-id",
                portfolioName = "Portfolio Name",
                userId = "user-id",
                entries = listOf(entry),
                isMonitored = true,
                monitoredFrameworks = setOf("sfdr"),
                notificationFrequency = NotificationFrequency.Weekly,
                timeWindowThreshold = TimeWindowThreshold.Standard,
                sharedUserIds = setOf("shared-user"),
                proxyCompanyReplacements = listOf(proxyCompanyReplacement),
            )

        assertEquals("portfolio-id", enrichedPortfolio.portfolioId)
        assertEquals("Portfolio Name", enrichedPortfolio.portfolioName)
        assertEquals("user-id", enrichedPortfolio.userId)
        assertEquals(listOf(entry), enrichedPortfolio.entries)
        assertEquals(true, enrichedPortfolio.isMonitored)
        assertEquals(setOf("sfdr"), enrichedPortfolio.monitoredFrameworks)
        assertEquals(NotificationFrequency.Weekly, enrichedPortfolio.notificationFrequency)
        assertEquals(TimeWindowThreshold.Standard, enrichedPortfolio.timeWindowThreshold)
        assertEquals(setOf("shared-user"), enrichedPortfolio.sharedUserIds)
        assertEquals(listOf(proxyCompanyReplacement), enrichedPortfolio.proxyCompanyReplacements)

        val copy = enrichedPortfolio.copy()
        assertEquals(enrichedPortfolio, copy)
        assertEquals(enrichedPortfolio.hashCode(), copy.hashCode())
        assertEquals(enrichedPortfolio.toString(), copy.toString())
    }
}

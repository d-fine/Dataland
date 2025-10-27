package org.dataland.datalanduserservice.utils

import org.dataland.datalanduserservice.model.EnrichedPortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolioEntry
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

object TestUtils {
    fun createEnrichedPortfolio(): EnrichedPortfolio {
        val entryFinancials =
            mock<EnrichedPortfolioEntry> {
                on { companyId } doReturn "c1"
                on { sector } doReturn "financials"
            }
        val entryNonFinancials =
            mock<EnrichedPortfolioEntry> {
                on { companyId } doReturn "c2"
                on { sector } doReturn "energy"
            }
        val entryUndefined =
            mock<EnrichedPortfolioEntry> {
                on { companyId } doReturn "c3"
                on { sector } doReturn null
            }

        return EnrichedPortfolio(
            portfolioId = "p1",
            portfolioName = "Portfolio 1",
            userId = "user1",
            entries = listOf(entryFinancials, entryNonFinancials, entryUndefined),
            isMonitored = null,
            monitoredFrameworks = null,
        )
    }
}

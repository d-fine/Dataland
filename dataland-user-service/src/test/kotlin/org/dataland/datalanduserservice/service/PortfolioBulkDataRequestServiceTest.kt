package org.dataland.datalanduserservice.service

import org.dataland.datalanduserservice.model.SectorType
import org.dataland.datalanduserservice.model.TimeWindowThreshold
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class PortfolioBulkDataRequestServiceTest : AbstractPortfolioBulkDataRequestServiceTest() {
    @Test
    fun `does not post request if portfolio is not monitored`() {
        stubCompanyApi(COMPANY_ID_1, 0, SectorType.FINANCIALS.name)
        stubCompanyApi(COMPANY_ID_2, 0, SectorType.FINANCIALS.name)

        stubPortfolioRepo(emptyList())
        service.createBulkDataRequestsForAllMonitoredPortfolios()
        verifyNoInteractions(mockRequestApi)
    }

    @Test
    fun `posts bulk requests for all framework and sector types inside grouping`() {
        stubCompanyApi(COMPANY_ID_1, 0, SectorType.FINANCIALS.name)
        stubCompanyApi(COMPANY_ID_2, -1, "consumer")
        val portfolioEntity =
            buildMonitoredPortfolioEntity(
                companyIds = setOf(COMPANY_ID_1, COMPANY_ID_2),
                frameworks = setOf(EUTAXONOMY, SFDR),
                portfolioName = "My monitored test portfolio",
            )
        stubPortfolioRepo(listOf(portfolioEntity))
        service.createBulkDataRequestsForAllMonitoredPortfolios()

        val expected =
            listOf(
                Triple(setOf(COMPANY_ID_1), setOf(SFDR), setOf("2024")),
                Triple(setOf(COMPANY_ID_1), setOf(EUTAXONOMY_FINANCIALS_2026_73), setOf("2024")),
                Triple(setOf(COMPANY_ID_2), setOf(EUTAXONOMY_NON_FINANCIALS_2026_73), setOf("2023")),
                Triple(setOf(COMPANY_ID_2), setOf(SFDR), setOf("2023")),
            )
        expected.forEach { (ids, types, periods) ->
            verifyBulkDataRequestPosted(ids, types, periods)
        }
        verifyNoMoreInteractions(mockRequestApi)
    }

    @Test
    fun `posts right eutaxonomy requests depending on company sector`() {
        val nonFinancialCompanyId = UUID.randomUUID().toString()
        val financialCompanyId = UUID.randomUUID().toString()
        val noSectorCompanyId = UUID.randomUUID().toString()
        stubCompanyApi(nonFinancialCompanyId, 0, "nonfinancials")
        stubCompanyApi(financialCompanyId, 0, "financials")
        stubCompanyApi(noSectorCompanyId, 0, null)
        val portfolioEntity =
            buildMonitoredPortfolioEntity(
                companyIds = setOf(nonFinancialCompanyId, financialCompanyId, noSectorCompanyId),
                frameworks = setOf(EUTAXONOMY),
                portfolioName = "My monitored eutaxonomy test portfolio",
            )
        stubPortfolioRepo(listOf(portfolioEntity))

        service.createBulkDataRequestsForAllMonitoredPortfolios()

        verifyBulkDataRequestPosted(setOf(nonFinancialCompanyId), setOf(EUTAXONOMY_NON_FINANCIALS_2026_73), setOf("2024"))
        verifyBulkDataRequestPosted(setOf(financialCompanyId), setOf(EUTAXONOMY_FINANCIALS_2026_73), setOf("2024"))
        verifyBulkDataRequestPosted(
            setOf(noSectorCompanyId),
            setOf(EUTAXONOMY_FINANCIALS_2026_73, EUTAXONOMY_NON_FINANCIALS_2026_73),
            setOf("2024"),
        )
        verifyNoMoreInteractions(mockRequestApi)
    }

    @Test
    fun `posts only sfdr when framework does not include eutaxonomy`() {
        stubCompanyApi(COMPANY_ID, 0, "random")
        val portfolioEntity =
            buildMonitoredPortfolioEntity(
                companyIds = setOf(COMPANY_ID),
                frameworks = setOf(SFDR),
                portfolioName = "My monitored sfdr test portfolio",
            )
        stubPortfolioRepo(listOf(portfolioEntity))
        service.createBulkDataRequestsForAllMonitoredPortfolios()
        verifyBulkDataRequestPosted(setOf(COMPANY_ID), setOf(SFDR), setOf("2024"))
        verifyNoMoreInteractions(mockRequestApi)
    }

    @Test
    fun `posts bulk requests for extended time window`() {
        val companyIdWithRecentFye = UUID.randomUUID().toString()
        val companyIdWithDistantFye = UUID.randomUUID().toString()

        doReturn(
            stubCompany(companyIdWithRecentFye, RECENT_FYE, 0, SectorType.FINANCIALS.name),
        ).whenever(mockCompanyDataApi).getCompanyById(companyIdWithRecentFye)

        doReturn(
            stubCompany(companyIdWithDistantFye, DISTANT_FYE, 0, SectorType.FINANCIALS.name),
        ).whenever(mockCompanyDataApi).getCompanyById(companyIdWithDistantFye)

        val portfolioWithExtendedMonthsTimeWindowThreshold =
            buildMonitoredPortfolioEntity(
                companyIds = setOf(companyIdWithRecentFye, companyIdWithDistantFye),
                frameworks = setOf(SFDR),
            ).copy(timeWindowThreshold = TimeWindowThreshold.Extended)
        stubPortfolioRepo(listOf(portfolioWithExtendedMonthsTimeWindowThreshold))

        service.createBulkDataRequestsForAllMonitoredPortfolios()

        verifyBulkDataRequestPosted(setOf(companyIdWithRecentFye, companyIdWithDistantFye), setOf(SFDR), setOf("2024"))
        verifyBulkDataRequestPosted(setOf(companyIdWithRecentFye), setOf(SFDR), setOf("2023"))
        verifyNoMoreInteractions(mockRequestApi)
    }

    @Test
    fun `posts bulk requests for six months time window`() {
        val companyIdWithRecentFye = UUID.randomUUID().toString()
        val companyIdWithDistantFye = UUID.randomUUID().toString()

        doReturn(
            stubCompany(companyIdWithRecentFye, RECENT_FYE, 0, SectorType.FINANCIALS.name),
        ).whenever(mockCompanyDataApi).getCompanyById(companyIdWithRecentFye)

        doReturn(
            stubCompany(companyIdWithDistantFye, DISTANT_FYE, 0, SectorType.FINANCIALS.name),
        ).whenever(mockCompanyDataApi).getCompanyById(companyIdWithDistantFye)

        val portfolioWithSixMonthsTimeWindowThreshold =
            buildMonitoredPortfolioEntity(
                companyIds = setOf(companyIdWithRecentFye, companyIdWithDistantFye),
                frameworks = setOf(SFDR),
            )
        stubPortfolioRepo(listOf(portfolioWithSixMonthsTimeWindowThreshold))

        service.createBulkDataRequestsForAllMonitoredPortfolios()

        verifyBulkDataRequestPosted(setOf(companyIdWithRecentFye), setOf(SFDR), setOf("2024"))
        verifyNoMoreInteractions(mockRequestApi)
    }

    @Test
    fun `no requests posted if reporting info not available`() {
        whenever(mockCompanyDataApi.getCompanyById(COMPANY_ID)).thenReturn(
            stubCompany(COMPANY_ID, null, 1, "financials"),
        )

        val portfolioEntity =
            buildMonitoredPortfolioEntity(
                companyIds = setOf(COMPANY_ID),
                frameworks = setOf(EUTAXONOMY, SFDR),
                portfolioName = "My monitored test portfolio",
            )
        stubPortfolioRepo(listOf(portfolioEntity))
        service.createBulkDataRequestsForAllMonitoredPortfolios()
        verifyNoInteractions(mockRequestApi)
    }
}

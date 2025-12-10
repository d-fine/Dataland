package org.dataland.datalanduserservice.service

import org.dataland.dataSourcingService.openApiClient.api.RequestControllerApi
import org.dataland.dataSourcingService.openApiClient.model.BulkDataRequest
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.dataland.datalanduserservice.model.SectorType
import org.dataland.datalanduserservice.model.TimeWindowThreshold
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Answers
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.argThat
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class PortfolioBulkDataRequestServiceTest {
    companion object {
        private val TODAY: LocalDate = LocalDate.of(2025, 2, 15)
        private val FYE: LocalDate = LocalDate.of(2024, 12, 31)
        private const val USER_ID = "user id"
        private const val PORTFOLIO_ID = "00000000-0000-0000-0000-000000000001"
        private const val COMPANY_ID_1 = "Company id 1"
        private const val COMPANY_ID_2 = "Company id 2"
        private const val COMPANY_ID = "Company id"
        private val TIMESTAMP = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        private const val EUTAXONOMY_FINANCIALS = "eutaxonomy-financials"
        private const val EUTAXONOMY_NON_FINANCIALS = "eutaxonomy-non-financials"
        private const val NUCLEAR_AND_GAS = "nuclear-and-gas"
        private const val SFDR = "sfdr"
        private const val EUTAXONOMY = "eutaxonomy"

        private val mockLocalDate = mockStatic(LocalDate::class.java, Answers.CALLS_REAL_METHODS)

        @BeforeAll
        @JvmStatic
        fun setupMockLocalDate() {
            mockLocalDate.`when`<LocalDate> { LocalDate.now() }.thenReturn(TODAY)
        }

        @AfterAll
        @JvmStatic
        fun teardownMockLocalDate() {
            mockLocalDate.close()
        }
    }

    private val mockCompanyDataApi = mock<CompanyDataControllerApi>()
    private lateinit var reportingInfoService: CompanyReportingInfoService
    private val mockRequestApi = mock<RequestControllerApi>()
    private val mockPortfolioRepo = mock<PortfolioRepository>()
    private lateinit var service: PortfolioBulkDataRequestService

    @BeforeEach
    fun setup() {
        reportingInfoService = CompanyReportingInfoService(mockCompanyDataApi)
        service =
            PortfolioBulkDataRequestService(
                reportingInfoService,
                mockRequestApi,
                mockPortfolioRepo,
            )
        reportingInfoService.resetData()
        clearInvocations(mockCompanyDataApi, mockRequestApi, mockPortfolioRepo)
    }

    private fun stubCompany(
        id: String,
        fiscalYearEnd: LocalDate?,
        reportingPeriodShift: Int?,
        sector: String?,
    ): StoredCompany =
        StoredCompany(
            id,
            CompanyInformation(
                companyName = "Company name for $id",
                headquarters = "HQ",
                identifiers = emptyMap(),
                countryCode = "DE",
                fiscalYearEnd = fiscalYearEnd,
                reportingPeriodShift = reportingPeriodShift,
                sector = sector,
            ),
            emptyList(),
        )

    private fun stubCompanyApi(
        id: String,
        reportingPeriodShift: Int?,
        sector: String?,
    ) {
        whenever(mockCompanyDataApi.getCompanyById(id)).thenReturn(
            stubCompany(id, FYE, reportingPeriodShift, sector),
        )
    }

    private fun stubPortfolioRepo(portfolios: List<PortfolioEntity>) {
        whenever(mockPortfolioRepo.findAllByIsMonitoredTrue()).thenReturn(portfolios)
    }

    private fun buildMonitoredPortfolioEntity(
        companyIds: Set<String>,
        frameworks: Set<String>,
        portfolioName: String = "Test portfolio",
    ) = PortfolioEntity(
        portfolioId = UUID.fromString(PORTFOLIO_ID),
        portfolioName = portfolioName,
        userId = USER_ID,
        creationTimestamp = TIMESTAMP,
        lastUpdateTimestamp = TIMESTAMP,
        companyIds = companyIds.toMutableSet(),
        isMonitored = true,
        monitoredFrameworks = frameworks,
        timeWindowThreshold = TimeWindowThreshold.SIX_MONTHS,
    )

    @Test
    fun `posts bulk requests for sixteen months time window`() {
        val companyIdWithRecentFye = UUID.randomUUID().toString()
        val recentFye = LocalDate.of(2024, 12, 31)

        whenever(mockCompanyDataApi.getCompanyById(companyIdWithRecentFye)).thenReturn(
            stubCompany(companyIdWithRecentFye, recentFye, 0, SectorType.FINANCIALS.name),
        )

        val portfolioWithSixteenMonthsTimeWindowThreshold =
            buildMonitoredPortfolioEntity(
                companyIds = setOf(companyIdWithRecentFye),
                frameworks = setOf(SFDR),
            ).copy(timeWindowThreshold = TimeWindowThreshold.SIXTEEN_MONTHS)
        stubPortfolioRepo(listOf(portfolioWithSixteenMonthsTimeWindowThreshold))
        service.createBulkDataRequestsForAllMonitoredPortfolios()
        println("PortfolioWith16month: $portfolioWithSixteenMonthsTimeWindowThreshold")

        // Debug output
        println(reportingInfoService.getCachedReportingYearAndSectorInformation())
        val cached = reportingInfoService.getCachedReportingYearAndSectorInformation()
        println("Cached companies: ${cached.keys}")

        verify(mockRequestApi).postBulkDataRequest(
            argThat<BulkDataRequest> {
                companyIdentifiers == setOf(companyIdWithRecentFye) && reportingPeriods == setOf("2024")
            },
            eq(USER_ID),
        )

        verify(mockRequestApi).postBulkDataRequest(
            argThat<BulkDataRequest> {
                companyIdentifiers == setOf(companyIdWithRecentFye) && reportingPeriods == setOf("2023")
            },
            eq(USER_ID),
        )
    }

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
                Triple(setOf(COMPANY_ID_1), setOf(EUTAXONOMY_FINANCIALS, NUCLEAR_AND_GAS), setOf("2024")),
                Triple(setOf(COMPANY_ID_2), setOf(EUTAXONOMY_NON_FINANCIALS, NUCLEAR_AND_GAS), setOf("2023")),
                Triple(setOf(COMPANY_ID_2), setOf(SFDR), setOf("2023")),
            )
        expected.forEach { (ids, types, periods) ->
            verify(mockRequestApi).postBulkDataRequest(
                argThat<BulkDataRequest> {
                    companyIdentifiers == ids && dataTypes == types && reportingPeriods == periods
                },
                eq(USER_ID),
            )
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

        verify(mockRequestApi).postBulkDataRequest(
            argThat<BulkDataRequest> {
                companyIdentifiers == setOf(nonFinancialCompanyId) &&
                    dataTypes == setOf(EUTAXONOMY_NON_FINANCIALS, NUCLEAR_AND_GAS) &&
                    reportingPeriods == setOf("2024")
            },
            eq(USER_ID),
        )
        verify(mockRequestApi).postBulkDataRequest(
            argThat<BulkDataRequest> {
                companyIdentifiers == setOf(financialCompanyId) &&
                    dataTypes == setOf(EUTAXONOMY_FINANCIALS, NUCLEAR_AND_GAS) &&
                    reportingPeriods == setOf("2024")
            },
            eq(USER_ID),
        )
        verify(mockRequestApi).postBulkDataRequest(
            argThat<BulkDataRequest> {
                companyIdentifiers == setOf(noSectorCompanyId) &&
                    dataTypes == setOf(EUTAXONOMY_FINANCIALS, EUTAXONOMY_NON_FINANCIALS, NUCLEAR_AND_GAS) &&
                    reportingPeriods == setOf("2024")
            },
            eq(USER_ID),
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
        verify(mockRequestApi).postBulkDataRequest(
            argThat<BulkDataRequest> {
                companyIdentifiers == setOf(COMPANY_ID) &&
                    dataTypes == setOf(SFDR) &&
                    reportingPeriods == setOf("2024")
            },
            eq(USER_ID),
        )
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

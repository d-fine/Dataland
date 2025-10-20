package org.dataland.datalanduserservice.service

import org.dataland.dataSourcingService.openApiClient.api.RequestControllerApi
import org.dataland.dataSourcingService.openApiClient.model.BulkDataRequest
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.SectorType
import org.dataland.datalanduserservice.repository.PortfolioRepository
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

class PortfolioBulkDataRequestServiceTest {
    companion object {
        private val TODAY: LocalDate = LocalDate.of(2025, 1, 15)
        private val FYE: LocalDate = LocalDate.of(2024, 12, 31)
        private const val USER_ID = "user id"
        private const val PORTFOLIO_ID = "Portfolio id"
        private const val COMPANY_ID_1 = "Company id 1"
        private const val COMPANY_ID_2 = "Company id 2"
        private const val COMPANY_ID = "Company id"
        private val TIMESTAMP = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        private const val EUTAXONOMY_FINANCIALS = "eutaxonomy-financials"
        private const val EUTAXONOMY_NON_FINANCIALS = "eutaxonomy-non-financials"
        private const val NUCLEAR_AND_GAS = "nuclear-and-gas"
        private const val SFDR = "sfdr"
        private const val EUTAXONOMY = "eutaxonomy"
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

    private fun buildPortfolio(
        companyIds: Set<String>,
        frameworks: Set<String>,
        isMonitored: Boolean,
        portfolioName: String = "Test portfolio",
    ) = BasePortfolio(
        portfolioId = PORTFOLIO_ID,
        userId = USER_ID,
        companyIds = companyIds,
        monitoredFrameworks = frameworks,
        isMonitored = isMonitored,
        portfolioName = portfolioName,
        creationTimestamp = TIMESTAMP,
        lastUpdateTimestamp = TIMESTAMP,
    )

    @Test
    fun `does not post request if portfolio is not monitored`() =
        mockStatic(LocalDate::class.java, Answers.CALLS_REAL_METHODS).use { mockedStatic ->
            mockedStatic.`when`<LocalDate> { LocalDate.now() }.thenReturn(TODAY)

            stubCompanyApi(COMPANY_ID_1, 0, SectorType.FINANCIALS.name)
            stubCompanyApi(COMPANY_ID_2, 0, SectorType.FINANCIALS.name)

            val basePortfolio =
                buildPortfolio(
                    companyIds = setOf(COMPANY_ID_1, COMPANY_ID_2),
                    frameworks = setOf(EUTAXONOMY, SFDR),
                    isMonitored = false,
                    portfolioName = "My not monitored test portfolio",
                )
            service.createBulkDataRequestsForPortfolioIfMonitored(basePortfolio)
            verifyNoInteractions(mockRequestApi)
        }

    @Test
    fun `posts bulk requests for all framework and sector types inside grouping`() =
        mockStatic(LocalDate::class.java, Answers.CALLS_REAL_METHODS).use { mockedStatic ->
            mockedStatic.`when`<LocalDate> { LocalDate.now() }.thenReturn(TODAY)

            stubCompanyApi(COMPANY_ID_1, 0, SectorType.FINANCIALS.name)
            stubCompanyApi(COMPANY_ID_2, -1, "consumer")
            val basePortfolio =
                buildPortfolio(
                    companyIds = setOf(COMPANY_ID_1, COMPANY_ID_2),
                    frameworks = setOf(EUTAXONOMY, SFDR),
                    isMonitored = true,
                    portfolioName = "My monitored test portfolio",
                )
            service.createBulkDataRequestsForPortfolioIfMonitored(basePortfolio)

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
    fun `posts only eutaxonomy when framework does not include sfdr`() =
        mockStatic(LocalDate::class.java, Answers.CALLS_REAL_METHODS).use { mockedStatic ->
            mockedStatic.`when`<LocalDate> { LocalDate.now() }.thenReturn(TODAY)

            stubCompanyApi(COMPANY_ID, 0, "nonfinancials")
            val basePortfolio =
                buildPortfolio(
                    companyIds = setOf(COMPANY_ID),
                    frameworks = setOf(EUTAXONOMY),
                    isMonitored = true,
                    portfolioName = "My monitored eutaxonomy test portfolio",
                )
            service.createBulkDataRequestsForPortfolioIfMonitored(basePortfolio)
            verify(mockRequestApi).postBulkDataRequest(
                argThat<BulkDataRequest> {
                    companyIdentifiers == setOf(COMPANY_ID) &&
                        dataTypes == setOf(EUTAXONOMY_NON_FINANCIALS, NUCLEAR_AND_GAS) &&
                        reportingPeriods == setOf("2024")
                },
                eq(USER_ID),
            )
            verifyNoMoreInteractions(mockRequestApi)
        }

    @Test
    fun `posts only sfdr when framework does not include eutaxonomy`() =
        mockStatic(LocalDate::class.java, Answers.CALLS_REAL_METHODS).use { mockedStatic ->
            mockedStatic.`when`<LocalDate> { LocalDate.now() }.thenReturn(TODAY)

            stubCompanyApi(COMPANY_ID, 0, "random")
            val basePortfolio =
                buildPortfolio(
                    companyIds = setOf(COMPANY_ID),
                    frameworks = setOf(SFDR),
                    isMonitored = true,
                    portfolioName = "My monitored sfdr test portfolio",
                )
            service.createBulkDataRequestsForPortfolioIfMonitored(basePortfolio)
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
    fun `no requests posted if reporting info not available`() =
        mockStatic(LocalDate::class.java, Answers.CALLS_REAL_METHODS).use { mockedStatic ->
            mockedStatic.`when`<LocalDate> { LocalDate.now() }.thenReturn(TODAY)

            whenever(mockCompanyDataApi.getCompanyById(COMPANY_ID)).thenReturn(
                stubCompany(COMPANY_ID, null, 1, "financials"),
            )

            val basePortfolio =
                buildPortfolio(
                    companyIds = setOf(COMPANY_ID),
                    frameworks = setOf(EUTAXONOMY, SFDR),
                    isMonitored = true,
                    portfolioName = "My monitored test portfolio",
                )
            service.createBulkDataRequestsForPortfolioIfMonitored(basePortfolio)
            verifyNoInteractions(mockRequestApi)
        }
}

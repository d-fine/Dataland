package org.dataland.datalanduserservice.service

import org.dataland.dataSourcingService.openApiClient.api.RequestControllerApi
import org.dataland.dataSourcingService.openApiClient.model.BulkDataRequest
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalanduserservice.model.BasePortfolio
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

    private fun companyInfo(
        fiscalYearEnd: LocalDate?,
        reportingPeriodShift: Int?,
        sector: String?,
    ): CompanyInformation =
        CompanyInformation(
            companyName = "Company name",
            headquarters = "HQ",
            identifiers = emptyMap(),
            countryCode = "DE",
            fiscalYearEnd = fiscalYearEnd,
            reportingPeriodShift = reportingPeriodShift,
            sector = sector,
        )

    @Test
    fun `does not post request if portfolio is not monitored`() {
        val today = LocalDate.of(2025, 1, 15)
        val basePortfolio =
            BasePortfolio(
                portfolioId = "portfolio id 1",
                userId = "user id 1",
                companyIds = setOf("company id 1", "company id 2"),
                monitoredFrameworks = setOf("eutaxonomy", "sfdr"),
                isMonitored = false,
                portfolioName = "My not monitored test portfolio",
                creationTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                lastUpdateTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
            )

        mockStatic(LocalDate::class.java, Answers.CALLS_REAL_METHODS).use { mockedStatic ->
            mockedStatic.`when`<LocalDate> { LocalDate.now() }.thenReturn(today)

            // Set up stubs for all company IDs in the portfolio
            basePortfolio.companyIds.forEach { id ->
                val info = companyInfo(LocalDate.of(2024, 12, 31), 0, "financials")
                whenever(mockCompanyDataApi.getCompanyById(id)).thenReturn(StoredCompany(id, info, emptyList()))
            }
            service.createBulkDataRequestsForPortfolioIfMonitored(basePortfolio)

            verifyNoInteractions(mockRequestApi)
        }
    }

    @Test
    fun `posts bulk requests for all framework and sector types inside grouping`() {
        val today = LocalDate.of(2025, 1, 15)
        val info1 = companyInfo(LocalDate.of(2024, 12, 31), 0, "financials")
        val info2 = companyInfo(LocalDate.of(2024, 12, 31), -1, "consumer")
        whenever(mockCompanyDataApi.getCompanyById("Company id 1")).thenReturn(StoredCompany("Company id 1", info1, emptyList()))
        whenever(mockCompanyDataApi.getCompanyById("Company id 2")).thenReturn(StoredCompany("Company id 2", info2, emptyList()))
        val basePortfolio =
            BasePortfolio(
                portfolioId = "Portfolio id",
                userId = "user id",
                companyIds = setOf("Company id 1", "Company id 2"),
                monitoredFrameworks = setOf("eutaxonomy", "sfdr"),
                isMonitored = true,
                portfolioName = "My monitored test portfolio",
                creationTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                lastUpdateTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
            )

        mockStatic(LocalDate::class.java, Answers.CALLS_REAL_METHODS).use { mockedStatic ->
            mockedStatic.`when`<LocalDate> { LocalDate.now() }.thenReturn(today)

            service.createBulkDataRequestsForPortfolioIfMonitored(basePortfolio)

            val expected =
                listOf(
                    Triple(setOf("Company id 1"), setOf("sfdr"), setOf("2024")),
                    Triple(setOf("Company id 1"), setOf("eutaxonomy-financials", "nuclear-and-gas"), setOf("2024")),
                    Triple(setOf("Company id 2"), setOf("eutaxonomy-non-financials", "nuclear-and-gas"), setOf("2023")),
                    Triple(setOf("Company id 2"), setOf("sfdr"), setOf("2023")),
                )

            expected.forEach { (ids, types, periods) ->
                verify(mockRequestApi).postBulkDataRequest(
                    argThat<BulkDataRequest> {
                        companyIdentifiers == ids && dataTypes == types && reportingPeriods == periods
                    },
                    eq("user id"),
                )
            }
            verifyNoMoreInteractions(mockRequestApi)
        }
    }

    @Test
    fun `posts only eutaxonomy when framework does not include sfdr`() {
        val today = LocalDate.of(2025, 1, 15)
        val info =
            companyInfo(
                fiscalYearEnd = LocalDate.of(2024, 12, 31),
                reportingPeriodShift = 0,
                sector = "nonfinancials",
            )
        whenever(mockCompanyDataApi.getCompanyById("Company id")).thenReturn(StoredCompany("Company id", info, emptyList()))
        val basePortfolio =
            BasePortfolio(
                portfolioId = "Portfolio id",
                userId = "user id",
                companyIds = setOf("Company id"),
                monitoredFrameworks = setOf("eutaxonomy"),
                isMonitored = true,
                portfolioName = "My monitored eutaxonomy test portfolio",
                creationTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                lastUpdateTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
            )
        mockStatic(LocalDate::class.java, Answers.CALLS_REAL_METHODS).use { mockedStatic ->
            mockedStatic.`when`<LocalDate> { LocalDate.now() }.thenReturn(today)

            service.createBulkDataRequestsForPortfolioIfMonitored(basePortfolio)
            verify(mockRequestApi).postBulkDataRequest(
                argThat<BulkDataRequest> {
                    companyIdentifiers == setOf("Company id") &&
                        dataTypes == setOf("eutaxonomy-non-financials", "nuclear-and-gas") &&
                        reportingPeriods == setOf("2024")
                },
                eq("user id"),
            )
            verifyNoMoreInteractions(mockRequestApi)
        }
    }

    @Test
    fun `posts only sfdr when framework does not include eutaxonomy`() {
        val today = LocalDate.of(2025, 1, 15)
        val info =
            companyInfo(
                fiscalYearEnd = LocalDate.of(2024, 12, 31),
                reportingPeriodShift = 0,
                sector = "random",
            )
        whenever(mockCompanyDataApi.getCompanyById("Company id")).thenReturn(StoredCompany("Company id", info, emptyList()))
        val basePortfolio =
            BasePortfolio(
                portfolioId = "Portfolio id",
                userId = "user id",
                companyIds = setOf("Company id"),
                monitoredFrameworks = setOf("sfdr"),
                isMonitored = true,
                portfolioName = "My monitored sfdr test portfolio",
                creationTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                lastUpdateTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
            )
        mockStatic(LocalDate::class.java, Answers.CALLS_REAL_METHODS).use { mockedStatic ->
            mockedStatic.`when`<LocalDate> { LocalDate.now() }.thenReturn(today)
            service.createBulkDataRequestsForPortfolioIfMonitored(basePortfolio)
            verify(mockRequestApi).postBulkDataRequest(
                argThat<BulkDataRequest> {
                    companyIdentifiers == setOf("Company id") &&
                        dataTypes == setOf("sfdr") &&
                        reportingPeriods == setOf("2024")
                },
                eq("user id"),
            )
            verifyNoMoreInteractions(mockRequestApi)
        }
    }

    @Test
    fun `no requests posted if reporting info not available`() {
        val today = LocalDate.of(2025, 1, 15)
        whenever(mockCompanyDataApi.getCompanyById("Company id")).thenReturn(
            StoredCompany(
                companyId = "Company id",
                companyInformation =
                    CompanyInformation(
                        companyName = "Company name",
                        headquarters = "HQ",
                        identifiers = emptyMap(),
                        countryCode = "DE",
                        fiscalYearEnd = null,
                        reportingPeriodShift = 1,
                        sector = "financials",
                    ),
                dataRegisteredByDataland = emptyList(),
            ),
        )
        val basePortfolio =
            BasePortfolio(
                portfolioId = "Portfolio id",
                userId = "user id",
                companyIds = setOf("Company id"),
                monitoredFrameworks = setOf("eutaxonomy", "sfdr"),
                isMonitored = true,
                portfolioName = "My monitored test portfolio",
                creationTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                lastUpdateTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
            )
        mockStatic(LocalDate::class.java, Answers.CALLS_REAL_METHODS).use { mockedStatic ->
            mockedStatic.`when`<LocalDate> { LocalDate.now() }.thenReturn(today)
            service.createBulkDataRequestsForPortfolioIfMonitored(basePortfolio)
            verifyNoInteractions(mockRequestApi)
        }
    }
}

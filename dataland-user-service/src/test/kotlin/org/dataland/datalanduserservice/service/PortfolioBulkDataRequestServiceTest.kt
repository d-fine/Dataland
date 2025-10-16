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
import org.junit.jupiter.api.assertThrows
import org.mockito.Answers
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
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

    private fun stubCompany(
        id: String,
        fiscalYearEnd: LocalDate?,
        reportingPeriodShift: Int?,
        sector: String?,
        today: LocalDate,
    ) {
        val info =
            CompanyInformation(
                companyName = "Company name",
                headquarters = "HQ",
                identifiers = emptyMap(),
                countryCode = "DE",
                fiscalYearEnd = fiscalYearEnd,
                reportingPeriodShift = reportingPeriodShift,
                sector = sector,
            )
        whenever(mockCompanyDataApi.getCompanyById(id)).thenReturn(StoredCompany(id, info, emptyList()))
        // We rely on LocalDate.now for reporting-year calculation, so mock it:
        mockStatic(LocalDate::class.java, Answers.CALLS_REAL_METHODS).use { mockedStatic ->
            mockedStatic.`when`<LocalDate> { LocalDate.now() }.thenReturn(today)
            reportingInfoService.updateCompanies(listOf(id))
        }
    }

    @Test
    fun `does not post request if portfolio is not monitored`() {
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

        service.postBulkDataRequestIfMonitored(basePortfolio)

        verifyNoInteractions(mockRequestApi)
    }

    @Test
    fun `posts bulk requests for all framework and sector types inside grouping`() {
        val today = LocalDate.of(2025, 1, 15)
        stubCompany(
            id = "Company id 1",
            fiscalYearEnd = LocalDate.of(2024, 12, 31),
            reportingPeriodShift = 0,
            sector = "financials",
            today = today,
        )
        stubCompany(
            id = "Company id 2",
            fiscalYearEnd = LocalDate.of(2024, 12, 31),
            reportingPeriodShift = -1,
            sector = "consumer",
            today = today,
        )
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

        service.postBulkDataRequestIfMonitored(basePortfolio)

        // B1 Group: 2025, Sector.FINANCIALS
        verify(mockRequestApi).postBulkDataRequest(
            argThat<BulkDataRequest> {
                companyIdentifiers == setOf("Company id 1") &&
                    dataTypes == setOf("sfdr") &&
                    reportingPeriods == setOf("2024")
            },
            eq("user id"),
        )
        verify(mockRequestApi).postBulkDataRequest(
            argThat<BulkDataRequest> {
                companyIdentifiers == setOf("Company id 1") &&
                    dataTypes == setOf("eutaxonomy-financials", "nuclear-and-gas") &&
                    reportingPeriods == setOf("2024")
            },
            eq("user id"),
        )

        // B2 Group: 2024, Sector.NONFINANCIALS
        verify(mockRequestApi).postBulkDataRequest(
            argThat<BulkDataRequest> {
                companyIdentifiers == setOf("Company id 2") &&
                    dataTypes == setOf("eutaxonomy-non-financials", "nuclear-and-gas") &&
                    reportingPeriods == setOf("2023")
            },
            eq("user id"),
        )
        verify(mockRequestApi).postBulkDataRequest(
            argThat<BulkDataRequest> {
                companyIdentifiers == setOf("Company id 2") &&
                    dataTypes == setOf("sfdr") &&
                    reportingPeriods == setOf("2023")
            },
            eq("user id"),
        )
        verifyNoMoreInteractions(mockRequestApi)
    }

    @Test
    fun `posts only eutaxonomy when framework does not include sfdr`() {
        val today = LocalDate.of(2025, 1, 15)
        stubCompany(
            id = "Company id",
            fiscalYearEnd = LocalDate.of(2024, 12, 31),
            reportingPeriodShift = 0,
            sector = "nonfinancials",
            today = today,
        )
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
        service.postBulkDataRequestIfMonitored(basePortfolio)
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

    @Test
    fun `posts only sfdr when framework does not include eutaxonomy`() {
        val today = LocalDate.of(2025, 1, 15)
        stubCompany(
            id = "Company id",
            fiscalYearEnd = LocalDate.of(2024, 12, 31),
            reportingPeriodShift = 0,
            sector = "random",
            today = today,
        )
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
        service.postBulkDataRequestIfMonitored(basePortfolio)
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

    @Test
    fun `no requests posted if reporting info not available`() {
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

        reportingInfoService.updateCompanies(listOf("Company id"))
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
        service.postBulkDataRequestIfMonitored(basePortfolio)
        verifyNoInteractions(mockRequestApi)
    }

    @Test
    fun `exceptions in postBulkDataRequest are thrown`() {
        val today = LocalDate.of(2025, 1, 15)
        stubCompany(
            id = "F1",
            fiscalYearEnd = LocalDate.of(2024, 12, 31),
            reportingPeriodShift = 0,
            sector = "financials",
            today = today,
        )
        whenever(mockRequestApi.postBulkDataRequest(any(), any())).thenThrow(RuntimeException("fail"))
        val basePortfolio =
            BasePortfolio(
                portfolioId = "p6",
                userId = "u6",
                companyIds = setOf("F1"),
                monitoredFrameworks = setOf("eutaxonomy"),
                isMonitored = true,
                portfolioName = "My monitored eutaxonomy test portfolio",
                creationTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                lastUpdateTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
            )

        assertThrows<RuntimeException> { service.postBulkDataRequestIfMonitored(basePortfolio) }
    }
}

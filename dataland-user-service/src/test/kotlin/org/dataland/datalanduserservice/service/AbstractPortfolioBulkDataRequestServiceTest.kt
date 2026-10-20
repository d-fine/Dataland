package org.dataland.datalanduserservice.service

import org.dataland.dataSourcingService.openApiClient.api.RequestControllerApi
import org.dataland.dataSourcingService.openApiClient.model.BulkDataRequest
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.dataland.datalanduserservice.model.TimeWindowThreshold
import org.dataland.datalanduserservice.model.enums.NotificationFrequency
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.mockito.Answers
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.argThat
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.MonthDay
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Shared setup, mocks and helpers for the [PortfolioBulkDataRequestService] tests.
 */
abstract class AbstractPortfolioBulkDataRequestServiceTest {
    companion object {
        internal val TODAY: LocalDate = LocalDate.of(2025, 2, 15)
        internal val FYE = MonthDay.of(12, 31)
        internal val RECENT_FYE = MonthDay.of(12, 31)
        internal val DISTANT_FYE = MonthDay.of(6, 1)
        internal const val USER_ID = "user id"
        internal const val PORTFOLIO_ID = "00000000-0000-0000-0000-000000000001"
        internal const val COMPANY_ID_1 = "Company id 1"
        internal const val COMPANY_ID_2 = "Company id 2"
        internal const val COMPANY_ID = "Company id"
        internal val TIMESTAMP = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        internal const val EUTAXONOMY_FINANCIALS_2026_73 = "eutaxonomy-financials-2026-73"
        internal const val EUTAXONOMY_NON_FINANCIALS_2026_73 = "eutaxonomy-non-financials-2026-73"
        internal const val SFDR = "sfdr"
        internal const val EUTAXONOMY = "eutaxonomy"

        private lateinit var mockLocalDate: MockedStatic<LocalDate>

        @BeforeAll
        @JvmStatic
        fun setupMockLocalDate() {
            mockLocalDate = mockStatic(LocalDate::class.java, Answers.CALLS_REAL_METHODS)
            mockLocalDate.`when`<LocalDate> { LocalDate.now() }.thenReturn(TODAY)
        }

        @AfterAll
        @JvmStatic
        fun teardownMockLocalDate() {
            mockLocalDate.close()
        }
    }

    protected val mockCompanyDataApi = mock<CompanyDataControllerApi>()
    protected lateinit var reportingInfoService: CompanyReportingInfoService
    protected val mockRequestApi = mock<RequestControllerApi>()
    protected val mockPortfolioRepo = mock<PortfolioRepository>()
    protected lateinit var service: PortfolioBulkDataRequestService

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

    protected fun stubCompany(
        id: String,
        fiscalYearEnd: MonthDay?,
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
                fiscalYearEnd =
                    fiscalYearEnd
                        ?.atYear(2024)
                        ?.format(DateTimeFormatter.ofPattern("dd-MMM")),
                reportingPeriodShift = reportingPeriodShift,
                sector = sector,
            ),
            emptyList(),
        )

    protected fun stubCompanyApi(
        id: String,
        reportingPeriodShift: Int?,
        sector: String?,
    ) {
        doReturn(stubCompany(id, FYE, reportingPeriodShift, sector)).whenever(mockCompanyDataApi).getCompanyById(id)
    }

    protected fun stubPortfolioRepo(portfolios: List<PortfolioEntity>) {
        whenever(mockPortfolioRepo.findAllByIsMonitoredTrue()).thenReturn(portfolios)
    }

    protected fun buildMonitoredPortfolioEntity(
        companyIds: Set<String>,
        frameworks: Set<String>,
        portfolioName: String = "Test portfolio",
        portfolioId: UUID = UUID.fromString(PORTFOLIO_ID),
        userId: String = USER_ID,
        timeWindowThreshold: TimeWindowThreshold = TimeWindowThreshold.Standard,
    ) = PortfolioEntity(
        portfolioId = portfolioId,
        portfolioName = portfolioName,
        userId = userId,
        creationTimestamp = TIMESTAMP,
        lastUpdateTimestamp = TIMESTAMP,
        notificationFrequency = NotificationFrequency.Weekly,
        companyIds = companyIds.toMutableSet(),
        isMonitored = true,
        monitoredFrameworks = frameworks,
        timeWindowThreshold = timeWindowThreshold,
        sharedUserIds = emptySet(),
    )

    protected fun verifyBulkDataRequestPosted(
        expectedCompanyIds: Set<String>,
        expectedDataTypes: Set<String>,
        expectedReportingPeriods: Set<String>,
    ) {
        verify(mockRequestApi).postBulkDataRequest(
            argThat<BulkDataRequest> {
                companyIdentifiers == expectedCompanyIds &&
                    dataTypes == expectedDataTypes &&
                    reportingPeriods == expectedReportingPeriods
            },
            eq(USER_ID),
        )
    }
}

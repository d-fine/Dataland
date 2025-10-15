package org.dataland.datalanduserservice.service

import org.dataland.dataSourcingService.openApiClient.api.RequestControllerApi
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolioEntry
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Answers
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDate
import java.util.UUID
import java.util.stream.Stream

class PortfolioBulkDataRequestServiceTest {
    private val mockRequestControllerApi = mock<RequestControllerApi>()

    // private val mockPortfolioEnrichmentService = mock<PortfolioEnrichmentService>()
    private var portfolioBulkDataRequestService = mock<PortfolioBulkDataRequestService>()
    private lateinit var mockAuthentication: DatalandAuthentication
    private var mockCompanyDataControllerApi = mock<CompanyDataControllerApi>()
    private var mockPortfolioRepository = mock<PortfolioRepository>()
    private val mockSecurityContext = mock<SecurityContext>()

    @BeforeEach
    fun setup() {
        reset(mockRequestControllerApi, mockCompanyDataControllerApi, mockPortfolioRepository)
        portfolioBulkDataRequestService =
            PortfolioBulkDataRequestService(
                requestControllerApi = mockRequestControllerApi,
                companyDataControllerApi = mockCompanyDataControllerApi,
                portfolioRepository = mockPortfolioRepository,
            )
        resetSecurityContext(
            UUID.randomUUID().toString(),
            setOf(DatalandRealmRole.ROLE_USER),
        )
    }

    /**
     * Setting the security context to use the specified userId and set of roles.
     */
    private fun resetSecurityContext(
        userId: String,
        roles: Set<DatalandRealmRole>,
    ) {
        mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                "username",
                userId,
                roles,
            )
        doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    val basePortfolio =
        BasePortfolio(
            portfolioId = "123",
            portfolioName = "abc",
            userId = "xyz",
            creationTimestamp = 456,
            lastUpdateTimestamp = 789,
            companyIds = setOf("c1", "c2"),
            isMonitored = true,
            monitoredFrameworks = setOf("sfdr", "eutaxonomy"),
        )

    val enrichedPortfolio =
        EnrichedPortfolio(
            basePortfolio.portfolioId,
            basePortfolio.portfolioName,
            basePortfolio.userId,
            listOf(
                EnrichedPortfolioEntry(
                    companyId = "c1", "C1",
                    sector = "financials", "Germany", "ref1",
                    emptyMap(), emptyMap(),
                ),
                EnrichedPortfolioEntry(
                    companyId = "c2", "C2",
                    sector = "industry", "Sweden", "ref2",
                    emptyMap(), emptyMap(),
                ),
                EnrichedPortfolioEntry(
                    companyId = "c3", "C3",
                    sector = null, "Italy", "ref3",
                    emptyMap(), emptyMap(),
                ),
            ),
            basePortfolio.isMonitored,
            basePortfolio.monitoredFrameworks,
        )

/*
    @Test
    fun `publishPortfolioUpdate behaves correctly for various frameworks`() {
        whenever(mockPortfolioEnrichmentService.getEnrichedPortfolio(basePortfolio))
            .thenReturn(enrichedPortfolio)

        whenever(mockPortfolioEnrichmentService.getEnrichedPortfolio(any()))
            .thenReturn(enrichedPortfolio)

        portfolioBulkDataRequestService.postBulkDataRequestMessageIfMonitored(basePortfolio)

        val requestCaptor = argumentCaptor<BulkDataRequest>()

        verify(mockRequestControllerApi, times(4)).postBulkDataRequest(
            requestCaptor.capture(),
            any(),
        )

        val allPublishedTypes = requestCaptor.allValues.map { it.dataTypes.toSet() }.toSet()
        println("Captured frameworks: $allPublishedTypes")
        assertEquals(
            setOf(
                setOf(DataTypeEnum.eutaxonomyMinusFinancials.value, DataTypeEnum.nuclearMinusAndMinusGas.value),
                setOf(DataTypeEnum.eutaxonomyMinusNonMinusFinancials.value, DataTypeEnum.nuclearMinusAndMinusGas.value),
                setOf(
                    DataTypeEnum.eutaxonomyMinusFinancials.value,
                    DataTypeEnum.nuclearMinusAndMinusGas.value,
                    DataTypeEnum.eutaxonomyMinusNonMinusFinancials.value,
                ),
                setOf(DataTypeEnum.sfdr.value),
            ),
            allPublishedTypes,
        )
    }

    @Test
    fun `sendBulkDataRequestIfMonitored does nothing when portfolio is not monitored`() {
        val basePortfolio =
            BasePortfolio(
                portfolioId = "non-monitored-id",
                portfolioName = "Non-Monitored Portfolio",
                userId = "user",
                creationTimestamp = Instant.now().toEpochMilli(),
                lastUpdateTimestamp = Instant.now().toEpochMilli(),
                companyIds = setOf("c1", "c2"),
                isMonitored = false,
                monitoredFrameworks = setOf("eutaxonomy", "sfdr"),
            )

        portfolioBulkDataRequestService.postBulkDataRequestMessageIfMonitored(basePortfolio)

        verify(mockPortfolioEnrichmentService, never()).getEnrichedPortfolio(any())
        verify(mockRequestControllerApi, never()).postBulkDataRequest(
            any(),
            any(),
        )
    }

 */
    companion object {
        @JvmStatic
        fun paramStream(): Stream<Arguments> =
            Stream.of(
                // fiscalYearEnd, reportingPeriodShift, today, expectedReportingPeriod
                Arguments.of(LocalDate.of(2024, 1, 31), 0, 1L, 3L, LocalDate.of(2025, 12, 1), 2026),
                Arguments.of(LocalDate.of(2024, 1, 31), -1, 1L, 3L, LocalDate.of(2025, 12, 1), 2025),
                Arguments.of(LocalDate.of(2018, 1, 31), 0, 1L, 3L, LocalDate.of(2025, 12, 1), 2026),
                Arguments.of(LocalDate.of(2024, 1, 31), 0, 1L, 3L, LocalDate.of(2021, 12, 1), 2022),
                Arguments.of(LocalDate.of(2024, 1, 31), 0, 1L, 9L, LocalDate.of(2025, 6, 1), 2026),
                Arguments.of(LocalDate.of(2024, 1, 31), 0, 1L, 3L, LocalDate.of(2025, 6, 1), null),
            )
    }

    @ParameterizedTest
    @MethodSource("paramStream")
    fun `unittest getCompanyReportingYearInfoForCompany`(
        fiscalYearEnd: LocalDate,
        reportingPeriodShift: Int,
        threshold1: Long,
        threshold2: Long,
        today: LocalDate,
        expectedReportingPeriod: Int?,
    ) {
        val testCompanyInformation =
            CompanyInformation(
                companyName = "testCompany",
                headquarters = "testHeadquarters",
                identifiers = emptyMap(),
                countryCode = "DE",
                fiscalYearEnd = fiscalYearEnd,
                reportingPeriodShift = reportingPeriodShift,
                sector = "testSector",
            )

        portfolioBulkDataRequestService.threshold1InMonths = threshold1
        portfolioBulkDataRequestService.threshold2InMonths = threshold2

        val testStoredCompany =
            StoredCompany(
                companyId = UUID.randomUUID().toString(),
                companyInformation = testCompanyInformation,
                dataRegisteredByDataland = emptyList(),
            )

        mockStatic(LocalDate::class.java, Answers.CALLS_REAL_METHODS).use { mockedStatic ->
            mockedStatic
                .`when`<LocalDate> { LocalDate.now() }
                .thenReturn(today)
            val companyReportingYearAndSectorInfo = portfolioBulkDataRequestService.getCompanyReportingYearInfoForCompany(testStoredCompany)
            assertEquals(expectedReportingPeriod, companyReportingYearAndSectorInfo?.reportingPeriod?.toInt())
        }
    }
}

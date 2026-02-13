package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalanduserservice.model.SectorType
import org.dataland.datalanduserservice.model.TimeWindowThreshold
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Answers
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.MonthDay
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.stream.Stream

class CompanyReportingInfoServiceTest {
    private var mockCompanyDataControllerApi = mock<CompanyDataControllerApi>()
    private lateinit var companyReportingInfoService: CompanyReportingInfoService

    @BeforeEach
    fun setup() {
        reset(mockCompanyDataControllerApi)
        companyReportingInfoService =
            CompanyReportingInfoService(
                companyDataControllerApi = mockCompanyDataControllerApi,
            )
    }

    /**
     * Provides parameters for the parameterized test of getCompanyReportingYearInfoForCompany.
     *
     * Each argument set includes:
     * - fiscalYearEnd: The fiscal year-end date of the company.
     * - reportingPeriodShift: The reporting period shift value.
     * - today: The current date to simulate.
     * - expectedReportingPeriods: The expected reporting periods.
     * - timeWindowThreshold: The time window threshold of a portfolio.
     *
     * @return A stream of arguments for the parameterized test.
     */
    companion object {
        @JvmStatic
        fun paramStream(): Stream<Arguments> =
            Stream.of(
                Arguments.of(MonthDay.of(1, 31), 0, LocalDate.of(2025, 3, 16), TimeWindowThreshold.Standard, setOf(2025)),
                Arguments.of(MonthDay.of(1, 31), -1, LocalDate.of(2025, 3, 16), TimeWindowThreshold.Standard, setOf(2024)),
                Arguments.of(MonthDay.of(2, 28), 0, LocalDate.of(2025, 3, 29), TimeWindowThreshold.Standard, null),
                Arguments.of(MonthDay.of(1, 31), 0, LocalDate.of(2025, 2, 16), TimeWindowThreshold.Standard, null),
                Arguments.of(MonthDay.of(1, 31), 0, LocalDate.of(2025, 5, 31), TimeWindowThreshold.Standard, setOf(2025)),
                Arguments.of(MonthDay.of(12, 31), 0, LocalDate.of(2025, 7, 16), TimeWindowThreshold.Standard, null),
                Arguments
                    .of(MonthDay.of(12, 31), 0, LocalDate.of(2025, 2, 16), TimeWindowThreshold.Extended, setOf(2023, 2024)),
                Arguments
                    .of(MonthDay.of(12, 31), 0, LocalDate.of(2025, 7, 16), TimeWindowThreshold.Extended, setOf(2024)),
            )
    }

    @ParameterizedTest
    @MethodSource("paramStream")
    fun `unittest getCompanyReportingYearInfoForCompany`(
        fiscalYearEnd: MonthDay,
        reportingPeriodShift: Int,
        today: LocalDate,
        timeWindowThreshold: TimeWindowThreshold,
        expectedReportingPeriod: Set<Int>?,
    ) {
        val mockedStatic = mockStatic(LocalDate::class.java, Answers.CALLS_REAL_METHODS)
        mockedStatic
            .`when`<LocalDate> { LocalDate.now() }
            .thenReturn(today)
        val testCompanyInformation =
            CompanyInformation(
                companyName = "testCompany",
                headquarters = "testHeadquarters",
                identifiers = emptyMap(),
                countryCode = "DE",
                fiscalYearEnd = fiscalYearEnd.atYear(0).format(DateTimeFormatter.ofPattern("dd-MMM")),
                reportingPeriodShift = reportingPeriodShift,
                sector = "testSector",
            )

        val testStoredCompany =
            StoredCompany(
                companyId = UUID.randomUUID().toString(),
                companyInformation = testCompanyInformation,
                dataRegisteredByDataland = emptyList(),
            )
        whenever(mockCompanyDataControllerApi.getCompanyById(testStoredCompany.companyId))
            .thenReturn(testStoredCompany)
        companyReportingInfoService.updateCompanies(listOf(testStoredCompany.companyId), timeWindowThreshold)
        val companyReportingYearAndSectorInfo =
            companyReportingInfoService.getCachedReportingYearAndSectorInformation()

        assertEquals(
            expectedReportingPeriod,
            companyReportingYearAndSectorInfo[testStoredCompany.companyId]
                ?.mapNotNull { it.reportingPeriod.toIntOrNull() }
                ?.toSet(),
        )
        mockedStatic.close()
    }

    @Test
    fun `test resetData clears cache`() {
        val companyId = "123"
        val mockedStatic = mockLocalDateNow()
        val info = validCompanyInfo()
        whenever(mockCompanyDataControllerApi.getCompanyById(companyId)).thenReturn(
            StoredCompany(companyId, info, emptyList()),
        )

        companyReportingInfoService.updateCompanies(listOf(companyId), TimeWindowThreshold.Standard)

        assertTrue(companyReportingInfoService.getCachedReportingYearAndSectorInformation().isNotEmpty())
        companyReportingInfoService.resetData()

        assertTrue(companyReportingInfoService.getCachedReportingYearAndSectorInformation().isEmpty())
        assertTrue(companyReportingInfoService.getCachedCompanyIdsWithoutReportingYearInfo().isEmpty())
        mockedStatic.close()
    }

    @Test
    fun `sector string financials maps to FINANCIALS sector type`() {
        val companyId = "sec1"
        val info = validCompanyInfo().copy(sector = "financials")
        val mockedStatic = mockLocalDateNow()
        whenever(mockCompanyDataControllerApi.getCompanyById(companyId)).thenReturn(
            StoredCompany(companyId, info, emptyList()),
        )
        companyReportingInfoService.updateCompanies(listOf(companyId), TimeWindowThreshold.Standard)
        val entry = companyReportingInfoService.getCachedReportingYearAndSectorInformation()[companyId]?.first()
        assertEquals(SectorType.FINANCIALS, entry?.sector)
        mockedStatic.close()
    }

    @Test
    fun `lower case sector string other than financials maps to NONFINANCIALS sector type`() {
        val companyId = "sec2"
        val mockedStatic = mockLocalDateNow()
        val info = validCompanyInfo().copy(sector = "randomsector")
        whenever(mockCompanyDataControllerApi.getCompanyById(companyId)).thenReturn(
            StoredCompany(companyId, info, emptyList()),
        )
        companyReportingInfoService.updateCompanies(listOf(companyId), TimeWindowThreshold.Standard)
        val entry = companyReportingInfoService.getCachedReportingYearAndSectorInformation()[companyId]?.first()
        assertEquals(SectorType.NONFINANCIALS, entry?.sector)
        mockedStatic.close()
    }

    @Test
    fun `null sector string leads to UNKNOWN sector type`() {
        val companyId = "sec3"
        val info = validCompanyInfo().copy(sector = null)
        val mockedStatic = mockLocalDateNow()
        whenever(mockCompanyDataControllerApi.getCompanyById(companyId)).thenReturn(
            StoredCompany(companyId, info, emptyList()),
        )
        companyReportingInfoService.updateCompanies(listOf(companyId), TimeWindowThreshold.Standard)
        val entry = companyReportingInfoService.getCachedReportingYearAndSectorInformation()[companyId]?.first()
        assertEquals(SectorType.UNKNOWN, entry?.sector)
        mockedStatic.close()
    }

    @Test
    fun `updateCompanies ignores duplicate IDs`() {
        val companyId = "dup"
        val info = validCompanyInfo()
        val mockedStatic = mockLocalDateNow()
        whenever(mockCompanyDataControllerApi.getCompanyById(companyId)).thenReturn(
            StoredCompany(companyId, info, emptyList()),
        )
        companyReportingInfoService.updateCompanies(listOf(companyId, companyId, companyId), TimeWindowThreshold.Standard)
        val map = companyReportingInfoService.getCachedReportingYearAndSectorInformation()
        assertEquals(1, map.size)
        mockedStatic.close()
    }

    @Test
    fun `updateCompanies does nothing on empty input`() {
        companyReportingInfoService.updateCompanies(emptyList(), TimeWindowThreshold.Standard)
        assertTrue(companyReportingInfoService.getCachedReportingYearAndSectorInformation().isEmpty())
        assertTrue(companyReportingInfoService.getCachedCompanyIdsWithoutReportingYearInfo().isEmpty())
    }

    private fun mockLocalDateNow(): MockedStatic<LocalDate> {
        val today = LocalDate.of(2025, 3, 31)
        val mockedStatic = mockStatic(LocalDate::class.java, Answers.CALLS_REAL_METHODS)
        mockedStatic.`when`<LocalDate> { LocalDate.now() }.thenReturn(today)
        return mockedStatic
    }

    private fun validCompanyInfo() =
        CompanyInformation(
            companyName = "Valid",
            headquarters = "HQ",
            identifiers = emptyMap(),
            countryCode = "DE",
            fiscalYearEnd = "31-Dec",
            reportingPeriodShift = 0,
            sector = "financials",
        )
}

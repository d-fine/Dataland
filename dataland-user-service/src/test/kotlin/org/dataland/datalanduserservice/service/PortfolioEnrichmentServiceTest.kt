package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.DataAvailabilityControllerApi
import org.dataland.datalandbackend.openApiClient.api.NonSourceabilityControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackend.openApiClient.model.BasicDataDimensions
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifierValidationResult
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.enums.NotificationFrequency
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for the availability-merging behavior of [PortfolioEnrichmentService], i.e. whether confirmed
 * non-sourceable reporting periods are folded into the same "available reporting periods" information as
 * real, viewable data.
 */
class PortfolioEnrichmentServiceTest {
    private val mockDataAvailabilityControllerApi = mock<DataAvailabilityControllerApi>()
    private val mockCompanyDataControllerApi = mock<CompanyDataControllerApi>()
    private val mockNonSourceabilityControllerApi = mock<NonSourceabilityControllerApi>()

    private val portfolioEnrichmentService =
        PortfolioEnrichmentService(
            mockDataAvailabilityControllerApi,
            mockCompanyDataControllerApi,
            mockNonSourceabilityControllerApi,
        )

    private val testCompanyId = "test-company-id"
    private val testCompanyInformation =
        BasicCompanyInformation(
            companyId = testCompanyId,
            companyName = "test company",
            headquarters = "Test City",
            countryCode = "DE",
            sector = null,
            lei = "test-lei",
        )

    private fun testPortfolio(): BasePortfolio =
        BasePortfolio(
            portfolioId = "test-portfolio-id",
            portfolioName = "test portfolio",
            userId = "test-user-id",
            creationTimestamp = 0L,
            lastUpdateTimestamp = 0L,
            identifiers = setOf(testCompanyId),
            isMonitored = false,
            monitoredFrameworks = emptySet(),
            notificationFrequency = NotificationFrequency.Weekly,
            timeWindowThreshold = null,
            sharedUserIds = emptySet(),
        )

    private fun mockCompanyValidation() {
        whenever(mockCompanyDataControllerApi.postCompanyValidation(any()))
            .doReturn(listOf(CompanyIdentifierValidationResult(testCompanyId, testCompanyInformation)))
    }

    @Test
    fun `check that a reporting period only confirmed as non-sourceable is included in availableReportingPeriods`() {
        mockCompanyValidation()
        whenever(mockDataAvailabilityControllerApi.searchViewableDimensions(any())).doReturn(emptyList())
        whenever(mockNonSourceabilityControllerApi.searchNonSourceableDimensions(any()))
            .doReturn(setOf(BasicDataDimensions(testCompanyId, "sfdr", "2023")))

        val enrichedPortfolio = portfolioEnrichmentService.getEnrichedPortfolio(testPortfolio())

        val entry = enrichedPortfolio.entries.first { it.companyId == testCompanyId }
        Assertions.assertEquals("2023", entry.availableReportingPeriods["sfdr"])
    }

    @Test
    fun `check that a reporting period with real data and no non-sourceability entry is still included`() {
        mockCompanyValidation()
        whenever(mockDataAvailabilityControllerApi.searchViewableDimensions(any()))
            .doReturn(listOf(BasicDataDimensions(testCompanyId, "sfdr", "2023")))
        whenever(mockNonSourceabilityControllerApi.searchNonSourceableDimensions(any()))
            .doReturn(emptySet())

        val enrichedPortfolio = portfolioEnrichmentService.getEnrichedPortfolio(testPortfolio())

        val entry = enrichedPortfolio.entries.first { it.companyId == testCompanyId }
        Assertions.assertEquals("2023", entry.availableReportingPeriods["sfdr"])
    }

    @Test
    fun `check that real data and non-sourceable periods for the same framework are merged without duplicates`() {
        mockCompanyValidation()
        whenever(mockDataAvailabilityControllerApi.searchViewableDimensions(any()))
            .doReturn(listOf(BasicDataDimensions(testCompanyId, "sfdr", "2023")))
        whenever(mockNonSourceabilityControllerApi.searchNonSourceableDimensions(any()))
            .doReturn(
                setOf(
                    BasicDataDimensions(testCompanyId, "sfdr", "2022"),
                    BasicDataDimensions(testCompanyId, "sfdr", "2023"),
                ),
            )

        val enrichedPortfolio = portfolioEnrichmentService.getEnrichedPortfolio(testPortfolio())

        val entry = enrichedPortfolio.entries.first { it.companyId == testCompanyId }
        Assertions.assertEquals("2023, 2022", entry.availableReportingPeriods["sfdr"])
    }

    @Test
    fun `check that a company with no real data and no non-sourceability entry has no available reporting periods`() {
        mockCompanyValidation()
        whenever(mockDataAvailabilityControllerApi.searchViewableDimensions(any())).doReturn(emptyList())
        whenever(mockNonSourceabilityControllerApi.searchNonSourceableDimensions(any())).doReturn(emptySet())

        val enrichedPortfolio = portfolioEnrichmentService.getEnrichedPortfolio(testPortfolio())

        val entry = enrichedPortfolio.entries.first { it.companyId == testCompanyId }
        Assertions.assertTrue(entry.availableReportingPeriods.isEmpty())
    }
}

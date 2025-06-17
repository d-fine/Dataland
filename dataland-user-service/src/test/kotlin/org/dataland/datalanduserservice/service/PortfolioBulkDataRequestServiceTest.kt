package org.dataland.datalanduserservice.service

import org.dataland.datalandcommunitymanager.openApiClient.api.RequestControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.model.BulkDataRequest
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.utils.TestUtils.createEnrichedPortfolio
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.stream.Stream

class PortfolioBulkDataRequestServiceTest {
    private lateinit var requestControllerApi: RequestControllerApi
    private lateinit var portfolioEnrichmentService: PortfolioEnrichmentService
    private lateinit var service: PortfolioBulkDataRequestService

    @BeforeEach
    fun setup() {
        requestControllerApi = mock()
        portfolioEnrichmentService = mock()

        service =
            PortfolioBulkDataRequestService(
                requestControllerApi = requestControllerApi,
                portfolioEnrichmentService = portfolioEnrichmentService,
            )
    }

    private class BasePortfolioArgumentProvider : ArgumentsProvider {
        override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments?>? =
            Stream.of(
                args(
                    basePortfolio("p1", "Taxonomy", setOf("eutaxonomy"), "2020"),
                    3,
                    setOf(
                        BulkDataRequest.DataTypes.eutaxonomyMinusFinancials,
                        BulkDataRequest.DataTypes.eutaxonomyMinusNonMinusFinancials,
                        BulkDataRequest.DataTypes.nuclearMinusAndMinusGas,
                    ),
                ),
                args(
                    basePortfolio("p2", "SFDR", setOf("sfdr"), "2022"),
                    1,
                    setOf(BulkDataRequest.DataTypes.sfdr),
                ),
                args(
                    basePortfolio("p3", "Combined", setOf("sfdr", "eutaxonomy"), "2021"),
                    4,
                    setOf(
                        BulkDataRequest.DataTypes.sfdr,
                        BulkDataRequest.DataTypes.eutaxonomyMinusFinancials,
                        BulkDataRequest.DataTypes.eutaxonomyMinusNonMinusFinancials,
                        BulkDataRequest.DataTypes.nuclearMinusAndMinusGas,
                    ),
                ),
            )

        private fun nowMillis() = Instant.now().toEpochMilli()

        private fun basePortfolio(
            id: String,
            name: String,
            frameworks: Set<String>,
            startingYear: String,
        ) = BasePortfolio(
            portfolioId = id,
            portfolioName = name,
            userId = "user",
            creationTimestamp = nowMillis(),
            lastUpdateTimestamp = nowMillis(),
            companyIds = setOf("c1", "c2", "c3"),
            isMonitored = true,
            startingMonitoringPeriod = startingYear,
            monitoredFrameworks = frameworks,
        )

        private fun args(
            portfolio: BasePortfolio,
            requestCount: Int,
            dataTypes: Set<BulkDataRequest.DataTypes>,
            notify: Boolean = false,
        ): Arguments = Arguments.of(portfolio, requestCount, dataTypes, notify)
    }

    @ParameterizedTest
    @ArgumentsSource(BasePortfolioArgumentProvider::class)
    fun `sendBulkDataRequest behaves correctly for various frameworks`(
        basePortfolio: BasePortfolio,
        expectedRequestCount: Int,
        expectedDataTypes: Set<BulkDataRequest.DataTypes>?,
        expectedNotify: Boolean,
    ) {
        val enrichedPortfolio = createEnrichedPortfolio()
        whenever(portfolioEnrichmentService.getEnrichedPortfolio(basePortfolio)).thenReturn(enrichedPortfolio)

        service.sendBulkDataRequestIfMonitored(basePortfolio)

        val expectedMonitoringPeriods =
            (basePortfolio.startingMonitoringPeriod!!.toInt() until PortfolioBulkDataRequestService.UPPER_BOUND)
                .map { it.toString() }
                .toSet()

        val captor = argumentCaptor<BulkDataRequest>()
        verify(requestControllerApi, times(expectedRequestCount)).postBulkDataRequest(captor.capture(), eq("user1"))

        val capturedRequests = captor.allValues

        if (expectedDataTypes != null) {
            capturedRequests.forEach { request ->
                assertTrue(request.dataTypes.all { it in expectedDataTypes })
                assertEquals(expectedMonitoringPeriods, request.reportingPeriods)
                assertEquals(expectedNotify, request.notifyMeImmediately)
            }
        }
    }
}

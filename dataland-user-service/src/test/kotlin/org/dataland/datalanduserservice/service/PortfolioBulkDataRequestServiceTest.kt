package org.dataland.datalanduserservice.service

import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.utils.TestUtils.createEnrichedPortfolio
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.stream.Stream

class PortfolioBulkDataRequestServiceTest {
    private val mockPublisher = mock<MessageQueuePublisher>()
    private val mockPortfolioEnrichmentService = mock<PortfolioEnrichmentService>()
    private lateinit var portfolioBulkDataRequestService: PortfolioBulkDataRequestService

    @BeforeEach
    fun setup() {
        reset(mockPublisher, mockPortfolioEnrichmentService)
        portfolioBulkDataRequestService =
            PortfolioBulkDataRequestService(
                publisher = mockPublisher,
                portfolioEnrichmentService = mockPortfolioEnrichmentService,
            )
    }

    private class BasePortfolioArgumentProvider : ArgumentsProvider {
        override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments?>? =
            Stream.of(
                getArguments(
                    buildBasePortfolio("p1", "Taxonomy", setOf("eutaxonomy"), "2020"),
                    3,
                    setOf(
                        "eutaxonomy-financials",
                        "eutaxonomy-non-financials",
                        "nuclear-and-gas",
                    ),
                ),
                getArguments(
                    buildBasePortfolio("p2", "SFDR", setOf("sfdr"), "2022"),
                    1,
                    setOf("sfdr"),
                ),
                getArguments(
                    buildBasePortfolio("p3", "Combined", setOf("sfdr", "eutaxonomy"), "2021"),
                    4,
                    setOf(
                        "sfdr",
                        "eutaxonomy-financials",
                        "eutaxonomy-non-financials",
                        "nuclear-and-gas",
                    ),
                ),
            )

        private fun buildBasePortfolio(
            id: String,
            name: String,
            frameworks: Set<String>,
            startingYear: String,
        ) = BasePortfolio(
            portfolioId = id,
            portfolioName = name,
            userId = "user",
            creationTimestamp = Instant.now().toEpochMilli(),
            lastUpdateTimestamp = Instant.now().toEpochMilli(),
            companyIds = setOf("c1", "c2", "c3"),
            isMonitored = true,
            startingMonitoringPeriod = startingYear,
            monitoredFrameworks = frameworks,
        )

        private fun getArguments(
            portfolio: BasePortfolio,
            requestCount: Int,
            dataTypes: Set<String>,
            notify: Boolean = false,
        ): Arguments = Arguments.of(portfolio, requestCount, dataTypes, notify)
    }

    @ParameterizedTest
    @ArgumentsSource(BasePortfolioArgumentProvider::class)
    fun `publishPortfolioUpdate behaves correctly for various frameworks`(
        basePortfolio: BasePortfolio,
        expectedRequestCount: Int,
        expectedDataTypes: Set<String>?,
        expectedNotify: Boolean,
    ) {
        val enrichedPortfolio = createEnrichedPortfolio()
        whenever(mockPortfolioEnrichmentService.getEnrichedPortfolio(basePortfolio)).thenReturn(enrichedPortfolio)

        val expectedMonitoringPeriods =
            (basePortfolio.startingMonitoringPeriod!!.toInt() until PortfolioBulkDataRequestService.UPPER_BOUND)
                .map { it.toString() }
                .toSet()

        val captor = argumentCaptor<String>()
        verify(mockPublisher, times(expectedRequestCount)).publishPortfolioUpdate(
            captor.capture(),
            any(), any(), any(), any(), any(),
        )

        val capturedRequests = captor.allValues
    }
}

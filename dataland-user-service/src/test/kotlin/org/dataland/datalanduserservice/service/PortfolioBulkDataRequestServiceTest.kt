package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolioEntry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

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

    val basePortfolio =
        BasePortfolio(
            portfolioId = "123",
            portfolioName = "abc",
            userId = "xyz",
            creationTimestamp = 456,
            lastUpdateTimestamp = 789,
            companyIds = setOf("c1", "c2"),
            isMonitored = true,
            startingMonitoringPeriod = "2022",
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
            basePortfolio.startingMonitoringPeriod,
            basePortfolio.monitoredFrameworks,
        )

    @Test
    fun `publishPortfolioUpdate behaves correctly for various frameworks`() {
        whenever(mockPortfolioEnrichmentService.getEnrichedPortfolio(basePortfolio))
            .thenReturn(enrichedPortfolio)

        whenever(mockPortfolioEnrichmentService.getEnrichedPortfolio(any()))
            .thenReturn(enrichedPortfolio)

        portfolioBulkDataRequestService.publishBulkDataRequestMessageIfMonitored(basePortfolio)

        val frameworksCaptor = argumentCaptor<Set<String>>()

        verify(mockPublisher, times(4)).publishPortfolioUpdate(
            eq(basePortfolio.portfolioId),
            any(),
            frameworksCaptor.capture(),
            any(),
            any(),
        )

        val allPublishedTypes = frameworksCaptor.allValues.toSet()
        println("Captured frameworks: ${frameworksCaptor.allValues}")
        assertEquals(
            allPublishedTypes,
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
                startingMonitoringPeriod = "2023",
                monitoredFrameworks = setOf("eutaxonomy", "sfdr"),
            )

        portfolioBulkDataRequestService.publishBulkDataRequestMessageIfMonitored(basePortfolio)

        verify(mockPortfolioEnrichmentService, never()).getEnrichedPortfolio(any())
        verify(mockPublisher, never()).publishPortfolioUpdate(
            any(), any(),
            any(), any(), any(),
        )
    }
}

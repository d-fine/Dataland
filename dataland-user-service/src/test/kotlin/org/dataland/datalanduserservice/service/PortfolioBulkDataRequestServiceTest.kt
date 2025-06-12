import org.dataland.datalandcommunitymanager.openApiClient.api.RequestControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.model.BulkDataRequest
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolioEntry
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.dataland.datalanduserservice.service.PortfolioBulkDataRequestService
import org.dataland.datalanduserservice.service.PortfolioEnrichmentService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Year

class PortfolioBulkDataRequestServiceTest {
    private lateinit var requestControllerApi: RequestControllerApi
    private lateinit var portfolioEnrichmentService: PortfolioEnrichmentService
    private lateinit var service: PortfolioBulkDataRequestService

    private val mockPortfolioBulkDataRequestService = mock<PortfolioBulkDataRequestService>()
    private val mockPortfolioRepository = mock<PortfolioRepository>()

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

    private fun createMockedEnrichedPortfolio(): EnrichedPortfolio {
        val entryFinancials =
            mock<EnrichedPortfolioEntry> {
                on { companyId } doReturn "c1"
                on { sector } doReturn "financials"
            }
        val entryNonFinancials =
            mock<EnrichedPortfolioEntry> {
                on { companyId } doReturn "c2"
                on { sector } doReturn "energy"
            }
        val entryUndefined =
            mock<EnrichedPortfolioEntry> {
                on { companyId } doReturn "c3"
                on { sector } doReturn null
            }

        return EnrichedPortfolio(
            portfolioId = "p1",
            portfolioName = "Portfolio 1",
            userId = "user1",
            entries = listOf(entryFinancials, entryNonFinancials, entryUndefined),
            isMonitored = null,
            startingMonitoringPeriod = null,
            monitoredFrameworks = null,
        )
    }

    @Test
    fun `sendBulkDataRequest posts eutaxonomy requests when monitoredFrameworks contains eutaxonomy`() {
        val basePortfolio =
            BasePortfolio(
                portfolioId = "p1",
                portfolioName = "Portfolio 1",
                userId = "user1",
                creationTimestamp = System.currentTimeMillis(),
                lastUpdateTimestamp = System.currentTimeMillis(),
                companyIds = setOf("id1", "id2"),
                isMonitored = false,
                startingMonitoringPeriod = "2020",
                monitoredFrameworks = setOf("eutaxonomy"),
            )

        val enrichedPortfolio = createMockedEnrichedPortfolio()
        whenever(portfolioEnrichmentService.getEnrichedPortfolio(basePortfolio)).thenReturn(enrichedPortfolio)

        service.sendBulkDataRequest(basePortfolio)

        val expectedMonitoringPeriods = (2020 until Year.now().value).map { it.toString() }.toSet()

        val captor = argumentCaptor<BulkDataRequest>()
        verify(requestControllerApi, times(3)).postBulkDataRequest(captor.capture())
        val capturedRequests = captor.allValues

        assertEquals(setOf("c1"), capturedRequests[0].companyIdentifiers)

        assertEquals(setOf("c1"), capturedRequests[0].companyIdentifiers)
        assertEquals(
            setOf(
                BulkDataRequest.DataTypes.eutaxonomyMinusFinancials,
                BulkDataRequest.DataTypes.nuclearMinusAndMinusGas,
            ),
            capturedRequests[0].dataTypes,
        )
        assertEquals(expectedMonitoringPeriods, capturedRequests[0].reportingPeriods)
        assertFalse(capturedRequests[0].notifyMeImmediately)

        assertEquals(setOf("c2"), capturedRequests[1].companyIdentifiers)
        assertEquals(
            setOf(
                BulkDataRequest.DataTypes.eutaxonomyMinusNonMinusFinancials,
                BulkDataRequest.DataTypes.nuclearMinusAndMinusGas,
            ),
            capturedRequests[1].dataTypes,
        )
        assertEquals(expectedMonitoringPeriods, capturedRequests[1].reportingPeriods)
        assertFalse(capturedRequests[1].notifyMeImmediately)

        assertEquals(setOf("c3"), capturedRequests[2].companyIdentifiers)
        assertEquals(
            setOf(
                BulkDataRequest.DataTypes.eutaxonomyMinusFinancials,
                BulkDataRequest.DataTypes.eutaxonomyMinusNonMinusFinancials,
                BulkDataRequest.DataTypes.nuclearMinusAndMinusGas,
            ),
            capturedRequests[2].dataTypes,
        )
        assertEquals(expectedMonitoringPeriods, capturedRequests[2].reportingPeriods)
        assertFalse(capturedRequests[2].notifyMeImmediately)
    }

    @Test
    fun `sendBulkDataRequest posts sfdr request when monitoredFrameworks contains sfdr`() {
        val basePortfolio =
            BasePortfolio(
                portfolioId = "p2",
                portfolioName = "Portfolio 2",
                userId = "user2",
                creationTimestamp = System.currentTimeMillis(),
                lastUpdateTimestamp = System.currentTimeMillis(),
                companyIds = setOf("id1", "id2"),
                isMonitored = false,
                startingMonitoringPeriod = "2020",
                monitoredFrameworks = setOf("sfdr"),
            )

        val enrichedPortfolio = createMockedEnrichedPortfolio()
        whenever(portfolioEnrichmentService.getEnrichedPortfolio(basePortfolio)).thenReturn(enrichedPortfolio)

        service.sendBulkDataRequest(basePortfolio)

        val expectedMonitoringPeriods = (2021 until Year.now().value).map { it.toString() }.toSet()

        val captor = argumentCaptor<BulkDataRequest>()
        verify(requestControllerApi, times(3)).postBulkDataRequest(captor.capture())

        val capturedRequests = captor.allValues

        assertEquals(setOf("c1"), capturedRequests[0].companyIdentifiers)

        val request = argumentCaptor<BulkDataRequest>().firstValue

        assertEquals(enrichedPortfolio.entries.map { it.companyId }.toSet(), request.companyIdentifiers)
        assertEquals(setOf(BulkDataRequest.DataTypes.sfdr), request.dataTypes)
        assertEquals(expectedMonitoringPeriods, request.reportingPeriods)
        assertFalse(request.notifyMeImmediately)
    }

    @Test
    fun `sendBulkDataRequest throws exception on invalid startingMonitoringPeriod`() {
        val basePortfolio =
            BasePortfolio(
                portfolioId = "p3",
                portfolioName = "Portfolio 3",
                userId = "user3",
                creationTimestamp = System.currentTimeMillis(),
                lastUpdateTimestamp = System.currentTimeMillis(),
                companyIds = setOf("id1", "id2"),
                isMonitored = false,
                startingMonitoringPeriod = "Zweitausendzwanzig",
                monitoredFrameworks = setOf("eutaxonomy"),
            )

        val enrichedPortfolio = createMockedEnrichedPortfolio()
        whenever(portfolioEnrichmentService.getEnrichedPortfolio(basePortfolio)).thenReturn(enrichedPortfolio)

        val exception =
            assertThrows<IllegalArgumentException> {
                service.sendBulkDataRequest(basePortfolio)
            }
        assertTrue(exception.message!!.contains("Invalid start year"))
    }
}

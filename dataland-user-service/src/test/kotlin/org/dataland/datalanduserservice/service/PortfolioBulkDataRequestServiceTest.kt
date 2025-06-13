import org.dataland.datalandcommunitymanager.openApiClient.api.RequestControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.model.BulkDataRequest
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolioEntry
import org.dataland.datalanduserservice.service.PortfolioBulkDataRequestService
import org.dataland.datalanduserservice.service.PortfolioEnrichmentService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.Year
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

    companion object {
        @JvmStatic
        fun providePortfolios(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    BasePortfolio(
                        portfolioId = "p1",
                        portfolioName = "Portfolio 1",
                        userId = "user1",
                        creationTimestamp = Instant.now().toEpochMilli(),
                        lastUpdateTimestamp = Instant.now().toEpochMilli(),
                        companyIds = setOf("c1", "c2", "c3"),
                        isMonitored = true,
                        startingMonitoringPeriod = "2020",
                        monitoredFrameworks = setOf("eutaxonomy"),
                    ),
                    3,
                    setOf(
                        BulkDataRequest.DataTypes.eutaxonomyMinusFinancials,
                        BulkDataRequest.DataTypes.eutaxonomyMinusNonMinusFinancials,
                        BulkDataRequest.DataTypes.nuclearMinusAndMinusGas,
                    ),
                    false,
                ),
                Arguments.of(
                    BasePortfolio(
                        portfolioId = "p2",
                        portfolioName = "Portfolio 2",
                        userId = "user2",
                        creationTimestamp = Instant.now().toEpochMilli(),
                        lastUpdateTimestamp = Instant.now().toEpochMilli(),
                        companyIds = setOf("c1", "c2", "c3"),
                        isMonitored = true,
                        startingMonitoringPeriod = "2022",
                        monitoredFrameworks = setOf("sfdr"),
                    ),
                    1, setOf(BulkDataRequest.DataTypes.sfdr), false,
                ),
                Arguments.of(
                    BasePortfolio(
                        portfolioId = "p_combined",
                        portfolioName = "Combined Portfolio",
                        userId = "user_combined",
                        creationTimestamp = Instant.now().toEpochMilli(),
                        lastUpdateTimestamp = Instant.now().toEpochMilli(),
                        companyIds = setOf("c1", "c2", "c3"),
                        isMonitored = true,
                        startingMonitoringPeriod = "2021",
                        monitoredFrameworks = setOf("sfdr", "eutaxonomy"),
                    ),
                    4, null, false,
                ),
            )
    }

    @ParameterizedTest
    @MethodSource("providePortfolios")
    fun `sendBulkDataRequest behaves correctly for various frameworks`(
        basePortfolio: BasePortfolio,
        expectedRequestCount: Int,
        expectedDataTypes: Set<BulkDataRequest.DataTypes>?,
        expectedNotify: Boolean,
    ) {
        val enrichedPortfolio = createMockedEnrichedPortfolio()
        whenever(portfolioEnrichmentService.getEnrichedPortfolio(basePortfolio)).thenReturn(enrichedPortfolio)

        service.sendBulkDataRequest(basePortfolio)

        val expectedMonitoringPeriods =
            (basePortfolio.startingMonitoringPeriod!!.toInt() until Year.now().value).map { it.toString() }.toSet()

        val captor = argumentCaptor<BulkDataRequest>()
        verify(requestControllerApi, times(expectedRequestCount)).postBulkDataRequest(captor.capture())

        val capturedRequests = captor.allValues

        if (expectedDataTypes != null) {
            capturedRequests.forEach { request ->
                assertTrue(request.dataTypes.all { it in expectedDataTypes })
                assertEquals(expectedMonitoringPeriods, request.reportingPeriods)
                assertEquals(expectedNotify, request.notifyMeImmediately)
            }
        }
    }

    @Test
    fun `sendBulkDataRequest throws exception on invalid startingMonitoringPeriod`() {
        val basePortfolio =
            BasePortfolio(
                portfolioId = "p3",
                portfolioName = "Portfolio 3",
                userId = "user3",
                creationTimestamp = Instant.now().toEpochMilli(),
                lastUpdateTimestamp = Instant.now().toEpochMilli(),
                companyIds = setOf("c1", "c2", "c3"),
                isMonitored = true,
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

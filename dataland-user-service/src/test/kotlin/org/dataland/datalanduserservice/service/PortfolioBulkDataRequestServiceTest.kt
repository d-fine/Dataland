package org.dataland.datalanduserservice.service

import org.dataland.dataSourcingService.openApiClient.api.RequestControllerApi
import org.dataland.dataSourcingService.openApiClient.model.BulkDataRequest
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolioEntry
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant
import java.util.UUID

class PortfolioBulkDataRequestServiceTest {
    private val mockRequestControllerApi = mock<RequestControllerApi>()
    private val mockPortfolioEnrichmentService = mock<PortfolioEnrichmentService>()
    private lateinit var portfolioBulkDataRequestService: PortfolioBulkDataRequestService
    private lateinit var mockAuthentication: DatalandAuthentication
    private val mockSecurityContext = mock<SecurityContext>()

    @BeforeEach
    fun setup() {
        reset(mockRequestControllerApi, mockPortfolioEnrichmentService)
        portfolioBulkDataRequestService =
            PortfolioBulkDataRequestService(
                requestControllerApi = mockRequestControllerApi,
                portfolioEnrichmentService = mockPortfolioEnrichmentService,
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
                startingMonitoringPeriod = "2023",
                monitoredFrameworks = setOf("eutaxonomy", "sfdr"),
            )

        portfolioBulkDataRequestService.postBulkDataRequestMessageIfMonitored(basePortfolio)

        verify(mockPortfolioEnrichmentService, never()).getEnrichedPortfolio(any())
        verify(mockRequestControllerApi, never()).postBulkDataRequest(
            any(),
            any(),
        )
    }
}

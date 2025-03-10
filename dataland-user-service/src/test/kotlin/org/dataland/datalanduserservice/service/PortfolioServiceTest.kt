package org.dataland.datalanduserservice.service

import org.dataland.userservice.entity.PortfolioEntity
import org.dataland.userservice.exceptions.PortfolioNotFoundApiException
import org.dataland.userservice.model.PortfolioPayload
import org.dataland.userservice.repository.PortfolioRepository
import org.dataland.userservice.service.PortfolioService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PortfolioServiceTest {
    private val portfolioRepository = mock<PortfolioRepository>()
    private lateinit var portfolioService: PortfolioService

    private val dummyPortfolioId = UUID.randomUUID()
    private val dummyUserId = "userId"
    private val dummyCorrelationId = "correlationId"
    private val dummyCompanyId = "companyId"

    private val dummyPortfolioEntity1 = buildPortfolio(portfolioName = "Portfolio1", userId = dummyUserId)
    private val dummyPortfolioEntity2 =
        buildPortfolio(portfolioName = "Portfolio2", userId = dummyUserId, companyIds = setOf(dummyCompanyId))

    @BeforeEach
    fun setup() {
        reset(portfolioRepository)
        doAnswer { invocation -> invocation.arguments[0] as PortfolioEntity }
            .whenever(portfolioRepository)
            .save(any<PortfolioEntity>())

        portfolioService = PortfolioService(portfolioRepository)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `verify that existsPortfolioForUser returns the expected values`(boolean: Boolean) {
        doReturn(boolean).whenever(portfolioRepository).existsByUserIdAndPortfolioId(dummyUserId, dummyPortfolioId)
        assertEquals(
            boolean,
            portfolioService
                .existsPortfolioForUser(dummyUserId, dummyPortfolioId.toString(), dummyCorrelationId),
        )
    }

    @Test
    fun `verify that retrieving all portfolios for dummyUser1 yields correct results`() {
        doReturn(listOf(dummyPortfolioEntity1, dummyPortfolioEntity2))
            .whenever(portfolioRepository)
            .getAllByUserId(dummyUserId)
        assertEquals(2, portfolioService.getAllPortfoliosForUser(dummyUserId, dummyCorrelationId).size)
        assertEquals(
            listOf(dummyPortfolioEntity1.portfolioId.toString(), dummyPortfolioEntity2.portfolioId.toString()),
            portfolioService
                .getAllPortfoliosForUser(dummyUserId, dummyCorrelationId)
                .map { it.portfolioId },
        )
    }

    @Test
    fun `verify that retrieving portfolio by Id for dummyUser yields correct results`() {
        doReturn(dummyPortfolioEntity1)
            .whenever(portfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, dummyPortfolioEntity1.portfolioId)
        assertEquals(
            dummyPortfolioEntity1.toPortfolioResponse(),
            portfolioService.getPortfolioForUser(dummyUserId, dummyPortfolioEntity1.portfolioId.toString(), dummyCorrelationId),
        )
    }

    @Test
    fun `verify that service throws PortfolioNotFoundApiException if portfolio cannot be found for user`() {
        doReturn(null).whenever(portfolioRepository).getPortfolioByUserIdAndPortfolioId(dummyUserId, dummyPortfolioId)

        assertThrows<PortfolioNotFoundApiException> {
            portfolioService.getPortfolioForUser(
                dummyUserId,
                dummyPortfolioId.toString(),
                dummyCorrelationId,
            )
        }
    }

    @Test
    fun `test that adding a company to an existing portfolio works as expected`() {
        val expectedResponse =
            dummyPortfolioEntity1.copy(companyIds = mutableSetOf(dummyCompanyId)).toPortfolioResponse()

        doReturn(dummyPortfolioEntity1)
            .whenever(portfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, dummyPortfolioEntity1.portfolioId)

        assertEquals(
            expectedResponse,
            portfolioService
                .addCompany(
                    dummyUserId,
                    dummyPortfolioEntity1.portfolioId.toString(),
                    dummyCompanyId,
                    dummyCorrelationId,
                ),
        )
    }

    @Test
    fun `test that adding a company to a non existing portfolio throws a PortfolioNotFoundApiException`() {
        doReturn(null).whenever(portfolioRepository).getPortfolioByUserIdAndPortfolioId(dummyUserId, dummyPortfolioId)

        assertThrows<PortfolioNotFoundApiException> {
            portfolioService.addCompany(dummyUserId, dummyPortfolioId.toString(), dummyCompanyId, dummyCorrelationId)
        }
    }

    @Test
    fun `test that creating a new portfolio from payload creates the correct entity and returns the correct response`() {
        val portfolioPayload = dummyPortfolioEntity1.toPortfolioPayload()
        val portfolioEntityCaptor = argumentCaptor<PortfolioEntity>()
        val portfolioResponse =
            portfolioService.createPortfolio(dummyUserId, portfolioPayload, dummyCorrelationId)

        verify(portfolioRepository).save(portfolioEntityCaptor.capture())

        assertEquals(portfolioPayload.portfolioName, portfolioEntityCaptor.firstValue.portfolioName)
        assertEquals(portfolioPayload.userId, portfolioEntityCaptor.firstValue.userId)
        assertEquals(portfolioPayload.companyIds, portfolioEntityCaptor.firstValue.companyIds)
        assertEquals(portfolioPayload.dataTypes, portfolioEntityCaptor.firstValue.dataTypes)
        assertEquals(
            portfolioResponse,
            portfolioEntityCaptor.firstValue.toPortfolioResponse(),
        )
    }

    @Test
    fun `test that replacing an existing portfolio from payload creates the correct entity and returns the correct response`() {
        val portfolioEntityCaptor = argumentCaptor<PortfolioEntity>()

        doReturn(dummyPortfolioEntity1)
            .whenever(portfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, dummyPortfolioEntity1.portfolioId)

        val replacingPortfolioResponse =
            portfolioService.replacePortfolio(
                dummyUserId,
                dummyPortfolioEntity2.toPortfolioPayload(),
                dummyPortfolioEntity1.portfolioId.toString(),
                dummyCorrelationId,
            )

        verify(portfolioRepository).save(portfolioEntityCaptor.capture())

        assertEquals(dummyPortfolioEntity1.portfolioId.toString(), replacingPortfolioResponse.portfolioId)
        assertEquals(dummyPortfolioEntity1.creationTimestamp, replacingPortfolioResponse.creationTimestamp)
        assertTrue(replacingPortfolioResponse.creationTimestamp < replacingPortfolioResponse.lastUpdateTimestamp)
        assertEquals(
            replacingPortfolioResponse,
            portfolioEntityCaptor.firstValue.toPortfolioResponse(),
        )
    }

    @Test
    fun `test that attempting to replace a non existing portfolio throws a PortfolioNotFoundApiException`() {
        doReturn(null).whenever(portfolioRepository).getPortfolioByUserIdAndPortfolioId(dummyUserId, dummyPortfolioId)

        assertThrows<PortfolioNotFoundApiException> {
            portfolioService.replacePortfolio(
                dummyUserId,
                dummyPortfolioEntity2.toPortfolioPayload(),
                dummyPortfolioId.toString(),
                dummyCorrelationId,
            )
        }
    }

    @Test
    fun `test that removing a company from a portfolio works as expected`() {
        doReturn(dummyPortfolioEntity2)
            .whenever(portfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, dummyPortfolioEntity2.portfolioId)

        portfolioService.removeCompanyFromPortfolio(
            dummyUserId,
            dummyPortfolioEntity2.portfolioId.toString(),
            dummyCompanyId,
            dummyCorrelationId,
        )

        assertEquals(mutableSetOf<String>(), dummyPortfolioEntity2.companyIds)
        assertTrue(dummyPortfolioEntity2.creationTimestamp < dummyPortfolioEntity2.lastUpdateTimestamp)
    }

    @Test
    fun `test that removing a company from a non existing portfolio throws a PortfolioNotFoundApiException`() {
        doReturn(null).whenever(portfolioRepository).getPortfolioByUserIdAndPortfolioId(dummyUserId, dummyPortfolioId)

        assertThrows<PortfolioNotFoundApiException> {
            portfolioService.removeCompanyFromPortfolio(
                dummyUserId,
                dummyPortfolioId.toString(),
                dummyCompanyId,
                dummyCorrelationId,
            )
        }
    }

    /**
     * Used to create dummy portfolios efficiently
     */
    private fun buildPortfolio(
        portfolioId: UUID? = null,
        portfolioName: String,
        userId: String,
        companyIds: Set<String>? = null,
        dataTypes: Set<String>? = null,
    ) = PortfolioEntity(
        portfolioId = portfolioId ?: UUID.randomUUID(),
        portfolioName = portfolioName,
        userId = userId,
        creationTimestamp = Instant.now().toEpochMilli(),
        lastUpdateTimestamp = Instant.now().toEpochMilli(),
        companyIds = companyIds?.toMutableSet() ?: mutableSetOf(),
        dataTypes = dataTypes?.toMutableSet() ?: mutableSetOf(),
    )

    /**
     * Extensions function to easily create a payload for the upload process which resembles a given entity
     */
    private fun PortfolioEntity.toPortfolioPayload() =
        PortfolioPayload(
            portfolioName = this.portfolioName,
            userId = this.userId,
            companyIds = this.companyIds,
            dataTypes = this.dataTypes,
        )
}

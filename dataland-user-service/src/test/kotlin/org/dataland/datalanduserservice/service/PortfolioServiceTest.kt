package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.dataland.datalanduserservice.exceptions.PortfolioNotFoundApiException
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
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
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PortfolioServiceTest {
    private val mockPortfolioRepository = mock<PortfolioRepository>()
    private val mockSecurityContext = mock<SecurityContext>()
    private lateinit var portfolioService: PortfolioService
    private lateinit var mockAuthentication: DatalandAuthentication

    private val dummyCorrelationId = UUID.randomUUID().toString()
    private val dummyPortfolioId = UUID.randomUUID()
    private val dummyUserId = "userId"
    private val dummyCompanyId = "companyId"

    private val dummyPortfolio = buildPortfolio(portfolioName = "Portfolio", userId = dummyUserId)
    private val dummyPortfolio2 = buildPortfolio(portfolioName = "Portfolio 2", userId = dummyUserId)

    @BeforeEach
    fun setup() {
        reset(mockPortfolioRepository)
        this.resetSecurityContext()
        doAnswer { it.arguments[0] }.whenever(mockPortfolioRepository).save(any<PortfolioEntity>())

        portfolioService = PortfolioService(mockPortfolioRepository)
    }

    /**
     * Setting the security context to use dataland dummy user with role ROLE_USER
     */
    private fun resetSecurityContext() {
        mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                "username",
                dummyUserId,
                roles = setOf(DatalandRealmRole.ROLE_USER),
            )
        doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `verify that existsPortfolioForUser returns the expected values`(boolean: Boolean) {
        doReturn(boolean).whenever(mockPortfolioRepository).existsByUserIdAndPortfolioId(dummyUserId, dummyPortfolioId)
        assertEquals(
            boolean,
            portfolioService.existsPortfolioForUser(dummyPortfolioId.toString(), dummyCorrelationId),
        )
    }

    @Test
    fun `verify that retrieving all portfolios for dummyUser1 yields correct results`() {
        doReturn(listOf(dummyPortfolio.toPortfolioEntity(), dummyPortfolio2.toPortfolioEntity()))
            .whenever(mockPortfolioRepository)
            .getAllByUserId(dummyUserId)
        assertEquals(2, portfolioService.getAllPortfoliosForUser().size)
        assertEquals(
            listOf(dummyPortfolio.portfolioId.toString(), dummyPortfolio2.portfolioId.toString()),
            portfolioService.getAllPortfoliosForUser().map { it.portfolioId },
        )
    }

    @Test
    fun `verify that retrieving portfolio by Id for dummyUser yields correct results`() {
        doReturn(dummyPortfolio.toPortfolioEntity())
            .whenever(mockPortfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, UUID.fromString(dummyPortfolio.portfolioId))
        assertEquals(
            dummyPortfolio,
            portfolioService.getPortfolioForUser(dummyPortfolio.portfolioId),
        )
    }

    @Test
    fun `verify that service throws PortfolioNotFoundApiException if portfolio cannot be found for user`() {
        doReturn(null)
            .whenever(mockPortfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, dummyPortfolioId)

        assertThrows<PortfolioNotFoundApiException> {
            portfolioService.getPortfolioForUser(dummyPortfolioId.toString())
        }
    }

    @Test
    fun `test that creating a new portfolio creates the correct entity and returns the correct response`() {
        val portfolioEntityCaptor = argumentCaptor<PortfolioEntity>()
        portfolioService.createPortfolio(dummyPortfolio, dummyCorrelationId)

        verify(mockPortfolioRepository).save(portfolioEntityCaptor.capture())

        assertEquals(
            dummyPortfolio,
            portfolioEntityCaptor.firstValue.toBasePortfolio(),
        )
    }

    @Test
    fun `test that replacing an existing portfolio creates the correct entity and returns the correct response`() {
        val portfolioEntityCaptor = argumentCaptor<PortfolioEntity>()

        doReturn(dummyPortfolio.toPortfolioEntity())
            .whenever(mockPortfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, UUID.fromString(dummyPortfolio.portfolioId))

        assertDoesNotThrow {
            portfolioService.replacePortfolio(dummyPortfolio.portfolioId, dummyPortfolio2, dummyCorrelationId)
        }

        verify(mockPortfolioRepository).save(portfolioEntityCaptor.capture())

        assertEquals(dummyPortfolio.portfolioId, portfolioEntityCaptor.firstValue.portfolioId.toString())
        assertEquals(dummyPortfolio.creationTimestamp, portfolioEntityCaptor.firstValue.creationTimestamp)
    }

    @Test
    fun `test that attempting to replace a non existing portfolio throws a PortfolioNotFoundApiException`() {
        doReturn(null)
            .whenever(mockPortfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, dummyPortfolioId)

        assertThrows<PortfolioNotFoundApiException> {
            portfolioService.replacePortfolio(
                dummyPortfolioId.toString(),
                dummyPortfolio2,
                dummyCorrelationId,
            )
        }
    }

    /**
     * Used to create dummy portfolios efficiently
     */
    private fun buildPortfolio(
        portfolioId: String? = null,
        portfolioName: String,
        userId: String,
        companyIds: MutableSet<String>? = null,
        dataTypes: MutableSet<DataTypeEnum>? = null,
    ) = BasePortfolio(
        portfolioId = portfolioId ?: UUID.randomUUID().toString(),
        portfolioName = portfolioName,
        userId = userId,
        creationTimestamp = Instant.now().toEpochMilli(),
        lastUpdateTimestamp = Instant.now().toEpochMilli(),
        companyIds = companyIds ?: mutableSetOf(dummyCompanyId),
        frameworks = dataTypes ?: mutableSetOf(DataTypeEnum.sfdr),
    )
}

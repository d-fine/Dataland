package org.dataland.datalanduserservice.service

import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.dataland.datalanduserservice.exceptions.PortfolioNotFoundApiException
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.BasePortfolioName
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
import org.mockito.kotlin.times
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
    private val adminUserId = "adminUserId"
    private val dummyCompanyId = "companyId"

    private val dummyPortfolio = buildPortfolio(portfolioName = "Portfolio", userId = dummyUserId)
    private val dummyPortfolio2 = buildPortfolio(portfolioName = "Portfolio 2", userId = dummyUserId)

    @BeforeEach
    fun setup() {
        reset(mockPortfolioRepository)
        resetSecurityContext(dummyUserId, false) // will sometimes be overwritten at the beginning of a test
        doAnswer { it.arguments[0] }.whenever(mockPortfolioRepository).save(any<PortfolioEntity>())

        portfolioService = PortfolioService(mockPortfolioRepository)
    }

    /**
     * Setting the security context to use dataland dummy user with role ROLE_USER
     */
    private fun resetSecurityContext(
        userId: String,
        isAdmin: Boolean,
    ) {
        mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                "username",
                userId,
                roles =
                    if (isAdmin) {
                        setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_ADMIN)
                    } else {
                        setOf(DatalandRealmRole.ROLE_USER)
                    },
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
            listOf(dummyPortfolio.portfolioId, dummyPortfolio2.portfolioId),
            portfolioService.getAllPortfoliosForUser().map { it.portfolioId },
        )
    }

    @Test
    fun `verify that retrieving portfolio by Id for dummyUser yields correct results`() {
        doReturn(dummyPortfolio.toPortfolioEntity())
            .whenever(mockPortfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, UUID.fromString(dummyPortfolio.portfolioId))
        doReturn(dummyPortfolio.toPortfolioEntity())
            .whenever(mockPortfolioRepository)
            .getPortfolioByPortfolioId(UUID.fromString(dummyPortfolio.portfolioId))
        val portfolioReturned = assertDoesNotThrow { portfolioService.getPortfolio(dummyPortfolio.portfolioId) }
        verify(mockPortfolioRepository, times(1)).getPortfolioByUserIdAndPortfolioId(
            dummyUserId, UUID.fromString(dummyPortfolio.portfolioId),
        )
        verify(mockPortfolioRepository, times(0)).getPortfolioByUserIdAndPortfolioId(
            adminUserId, UUID.fromString(dummyPortfolio.portfolioId),
        )
        verify(mockPortfolioRepository, times(0)).getPortfolioByPortfolioId(any())
        assertEquals(
            dummyPortfolio,
            portfolioReturned,
        )
    }

    @Test
    fun `verify that dummy user cannot access admin portfolios`() {
        doReturn(dummyPortfolio.toPortfolioEntity())
            .whenever(mockPortfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(adminUserId, UUID.fromString(dummyPortfolio.portfolioId))
        doReturn(dummyPortfolio.toPortfolioEntity())
            .whenever(mockPortfolioRepository)
            .getPortfolioByPortfolioId(UUID.fromString(dummyPortfolio.portfolioId))
        assertThrows<PortfolioNotFoundApiException> {
            portfolioService.getPortfolio(dummyPortfolio.portfolioId)
        }
        verify(mockPortfolioRepository, times(1)).getPortfolioByUserIdAndPortfolioId(
            dummyUserId, UUID.fromString(dummyPortfolio.portfolioId),
        )
        verify(mockPortfolioRepository, times(0)).getPortfolioByUserIdAndPortfolioId(
            adminUserId, UUID.fromString(dummyPortfolio.portfolioId),
        )
        verify(mockPortfolioRepository, times(0)).getPortfolioByPortfolioId(any())
    }

    @Test
    fun `verify that admin can access dummy user portfolio`() {
        doReturn(dummyPortfolio.toPortfolioEntity())
            .whenever(mockPortfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, UUID.fromString(dummyPortfolio.portfolioId))
        doReturn(dummyPortfolio.toPortfolioEntity())
            .whenever(mockPortfolioRepository)
            .getPortfolioByPortfolioId(UUID.fromString(dummyPortfolio.portfolioId))
        resetSecurityContext(adminUserId, true)
        val portfolioReturned = assertDoesNotThrow { portfolioService.getPortfolio(dummyPortfolio.portfolioId) }
        verify(mockPortfolioRepository, times(0)).getPortfolioByUserIdAndPortfolioId(
            any(), any(),
        )
        verify(mockPortfolioRepository, times(1)).getPortfolioByPortfolioId(UUID.fromString(dummyPortfolio.portfolioId))
        assertEquals(
            dummyPortfolio,
            portfolioReturned,
        )
    }

    @Test
    fun `verify that service throws PortfolioNotFoundApiException if portfolio cannot be found for user`() {
        doReturn(null)
            .whenever(mockPortfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, dummyPortfolioId)

        assertThrows<PortfolioNotFoundApiException> {
            portfolioService.getPortfolio(dummyPortfolioId.toString())
        }
    }

    @Test
    fun `verify that portfolios can be retrieved for a user by his or her ID`() {
        doReturn(listOf(dummyPortfolio.toPortfolioEntity(), dummyPortfolio2.toPortfolioEntity()))
            .whenever(mockPortfolioRepository)
            .getAllByUserId(dummyUserId)
        resetSecurityContext(adminUserId, true)
        val portfolioList = portfolioService.getAllPortfoliosForUserById(dummyUserId)
        assertEquals(2, portfolioList.size)
        assertEquals(
            listOf(dummyPortfolio.portfolioId, dummyPortfolio2.portfolioId),
            portfolioList.map { it.portfolioId },
        )
    }

    @Test
    fun `verify that getAllPortfolios redirects to the repository with the correct pagination parameters`() {
        portfolioService.getAllPortfolios(50, 3)
        verify(mockPortfolioRepository).findAllWithPagination(50, 150)
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

    @Test
    fun `verify that retrieving all portfolio names for dummyUser1 yields correct results`() {
        doReturn(listOf(dummyPortfolio.toPortfolioEntity(), dummyPortfolio2.toPortfolioEntity()))
            .whenever(mockPortfolioRepository)
            .getAllByUserId(dummyUserId)
        val expectedPortfolioNames = listOf(BasePortfolioName(dummyPortfolio), BasePortfolioName(dummyPortfolio2))
        val portfolioNames = assertDoesNotThrow { portfolioService.getAllPortfolioNamesForCurrentUser() }
        assertEquals(expectedPortfolioNames, portfolioNames)
    }

    /**
     * Used to create dummy portfolios efficiently
     */
    private fun buildPortfolio(
        portfolioId: String? = null,
        portfolioName: String,
        userId: String,
        companyIds: MutableSet<String>? = null,
    ) = BasePortfolio(
        portfolioId = portfolioId ?: UUID.randomUUID().toString(),
        portfolioName = portfolioName,
        userId = userId,
        creationTimestamp = Instant.now().toEpochMilli(),
        lastUpdateTimestamp = Instant.now().toEpochMilli(),
        companyIds = companyIds ?: mutableSetOf(dummyCompanyId),
    )
}

package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.dataland.datalanduserservice.exceptions.PortfolioNotFoundApiException
import org.dataland.datalanduserservice.model.PortfolioPayload
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
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

    private val dummyPortfolioId = UUID.randomUUID()
    private val dummyUserId = "userId"
    private val dummyCompanyId = "companyId"
    private val dummyCompanyId2 = "companyId2"

    private val dummyPortfolioEntity = buildPortfolio(portfolioName = "Portfolio", userId = dummyUserId)
    private val dummyPortfolioEntity2 = buildPortfolio(portfolioName = "Portfolio 2", userId = dummyUserId)

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
            portfolioService.existsPortfolioForUser(dummyPortfolioId.toString()),
        )
    }

    @Test
    fun `verify that retrieving all portfolios for dummyUser1 yields correct results`() {
        doReturn(listOf(dummyPortfolioEntity, dummyPortfolioEntity2))
            .whenever(mockPortfolioRepository)
            .getAllByUserId(dummyUserId)
        assertEquals(2, portfolioService.getAllPortfoliosForUser().size)
        assertEquals(
            listOf(dummyPortfolioEntity.portfolioId.toString(), dummyPortfolioEntity2.portfolioId.toString()),
            portfolioService.getAllPortfoliosForUser().map { it.portfolioId },
        )
    }

    @Test
    fun `verify that retrieving portfolio by Id for dummyUser yields correct results`() {
        doReturn(dummyPortfolioEntity)
            .whenever(mockPortfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, dummyPortfolioEntity.portfolioId)
        assertEquals(
            dummyPortfolioEntity.toPortfolioResponse(),
            portfolioService.getPortfolioForUser(dummyPortfolioEntity.portfolioId.toString()),
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
    fun `test that adding a company to an existing portfolio works as expected`() {
        doReturn(dummyPortfolioEntity)
            .whenever(mockPortfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, dummyPortfolioEntity.portfolioId)

        val portfolioResponse =
            portfolioService.addCompany(
                dummyPortfolioEntity.portfolioId.toString(),
                dummyCompanyId,
            )

        assertEquals(mutableSetOf(dummyCompanyId), portfolioResponse.companyIds)
    }

    @Test
    fun `test that adding a company to a non existing portfolio throws a PortfolioNotFoundApiException`() {
        doReturn(null)
            .whenever(mockPortfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, dummyPortfolioId)

        assertThrows<PortfolioNotFoundApiException> {
            portfolioService.addCompany(dummyPortfolioId.toString(), dummyCompanyId)
        }
    }

    @Test
    fun `test that creating a new portfolio from payload creates the correct entity and returns the correct response`() {
        val portfolioPayload = dummyPortfolioEntity.toPortfolioPayload()
        val portfolioEntityCaptor = argumentCaptor<PortfolioEntity>()
        val portfolioResponse = portfolioService.createPortfolio(portfolioPayload)

        verify(mockPortfolioRepository).save(portfolioEntityCaptor.capture())

        assertEquals(portfolioPayload.portfolioName, portfolioEntityCaptor.firstValue.portfolioName)
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

        doReturn(dummyPortfolioEntity)
            .whenever(mockPortfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, dummyPortfolioEntity.portfolioId)

        val replacingPortfolioResponse =
            portfolioService.replacePortfolio(
                dummyPortfolioEntity2.toPortfolioPayload(),
                dummyPortfolioEntity.portfolioId.toString(),
            )

        verify(mockPortfolioRepository).save(portfolioEntityCaptor.capture())

        assertEquals(dummyPortfolioEntity.portfolioId.toString(), replacingPortfolioResponse.portfolioId)
        assertEquals(dummyPortfolioEntity.creationTimestamp, replacingPortfolioResponse.creationTimestamp)
        assertTrue(replacingPortfolioResponse.creationTimestamp < replacingPortfolioResponse.lastUpdateTimestamp)
        assertEquals(
            replacingPortfolioResponse,
            portfolioEntityCaptor.firstValue.toPortfolioResponse(),
        )
    }

    @Test
    fun `test that attempting to replace a non existing portfolio throws a PortfolioNotFoundApiException`() {
        doReturn(null)
            .whenever(mockPortfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, dummyPortfolioId)

        assertThrows<PortfolioNotFoundApiException> {
            portfolioService.replacePortfolio(
                dummyPortfolioEntity2.toPortfolioPayload(),
                dummyPortfolioId.toString(),
            )
        }
    }

    @Test
    fun `test that removing a company from a portfolio works as expected`() {
        val portfolioEntityWithTwoCompanies =
            dummyPortfolioEntity.copy(companyIds = mutableSetOf(dummyCompanyId, dummyCompanyId2))

        doReturn(portfolioEntityWithTwoCompanies)
            .whenever(mockPortfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, portfolioEntityWithTwoCompanies.portfolioId)

        portfolioService.removeCompanyFromPortfolio(
            portfolioEntityWithTwoCompanies.portfolioId.toString(),
            dummyCompanyId,
        )

        assertEquals(mutableSetOf(dummyCompanyId2), portfolioEntityWithTwoCompanies.companyIds)
        assertTrue(portfolioEntityWithTwoCompanies.creationTimestamp < portfolioEntityWithTwoCompanies.lastUpdateTimestamp)
    }

    @Test
    fun `test that removing a company from a non existing portfolio throws a PortfolioNotFoundApiException`() {
        doReturn(null)
            .whenever(mockPortfolioRepository)
            .getPortfolioByUserIdAndPortfolioId(dummyUserId, dummyPortfolioId)

        assertThrows<PortfolioNotFoundApiException> {
            portfolioService.removeCompanyFromPortfolio(
                dummyPortfolioId.toString(),
                dummyCompanyId,
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
        companyIds: MutableSet<String>? = null,
        dataTypes: MutableSet<DataTypeEnum>? = null,
    ) = PortfolioEntity(
        portfolioId = portfolioId ?: UUID.randomUUID(),
        portfolioName = portfolioName,
        userId = userId,
        creationTimestamp = Instant.now().toEpochMilli(),
        lastUpdateTimestamp = Instant.now().toEpochMilli(),
        companyIds = companyIds ?: mutableSetOf(dummyCompanyId),
        dataTypes = dataTypes ?: mutableSetOf(DataTypeEnum.sfdr),
    )

    /**
     * Extensions function to easily create a payload for the upload process which resembles a given entity
     */
    private fun PortfolioEntity.toPortfolioPayload() =
        PortfolioPayload(
            portfolioName = this.portfolioName,
            companyIds = this.companyIds,
            dataTypes = this.dataTypes,
        )
}

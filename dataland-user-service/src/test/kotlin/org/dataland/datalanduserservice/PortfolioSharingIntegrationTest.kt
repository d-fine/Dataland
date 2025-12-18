package org.dataland.datalanduserservice

import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.dataland.datalanduserservice.service.PortfolioSharingService
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant
import java.util.UUID

@SpringBootTest(classes = [DatalandUserService::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class PortfolioSharingIntegrationTest
    @Autowired
    constructor(
        private val portfolioRepository: PortfolioRepository,
    ) {
        @Autowired
        private lateinit var portfolioSharingService: PortfolioSharingService

        private val mockSecurityContext = mock<SecurityContext>()
        private lateinit var mockAuthentication: DatalandAuthentication

        private val dummyOwnerUserId = "ownerUserId"
        private val dummySharedUserId = "sharedUserId"
        private val dummySharedOtherUserId = "sharedOtherUserId"
        private val dummyUsername = "dummyUsername"

        private fun createPortfolioEntity(sharedUserIds: Set<String>): PortfolioEntity {
            val timestamp = Instant.now().toEpochMilli()
            val id = UUID.randomUUID().toString()
            return BasePortfolio(
                portfolioId = id,
                portfolioName = "Portfolio-$id",
                userId = dummyOwnerUserId,
                creationTimestamp = timestamp,
                lastUpdateTimestamp = timestamp,
                identifiers = setOf("companyId"),
                isMonitored = false,
                monitoredFrameworks = setOf("sfdr", "eutaxonomy"),
                sharedUserIds = sharedUserIds,
            ).toPortfolioEntity()
        }

        private fun setAuthenticationContext(userId: String = dummySharedUserId) {
            mockAuthentication =
                AuthenticationMock.mockJwtAuthentication(dummyUsername, userId, roles = setOf(DatalandRealmRole.ROLE_USER))
            doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
            SecurityContextHolder.setContext(mockSecurityContext)
        }

        @BeforeEach
        fun clearRepositoryAndAuth() {
            portfolioRepository.deleteAll()
            SecurityContextHolder.clearContext()
        }

        @Test
        fun `getAllSharedPortfoliosForCurrentUser returns shared portfolios for current user`() {
            setAuthenticationContext()
            val entity = createPortfolioEntity(setOf(dummySharedUserId))
            portfolioRepository.save(entity)
            val result = portfolioSharingService.getAllSharedPortfoliosForCurrentUser()
            assertEquals(1, result.size)
            assertEquals(entity.portfolioId.toString(), result[0].portfolioId)
            assertEquals(entity.portfolioName, result[0].portfolioName)
        }

        @Test
        fun `getAllSharedPortfoliosForCurrentUser returns empty list if no portfolios shared with current user`() {
            setAuthenticationContext()
            val entity = createPortfolioEntity(setOf(dummySharedOtherUserId))
            portfolioRepository.save(entity)
            val result = portfolioSharingService.getAllSharedPortfoliosForCurrentUser()
            assertTrue(result.isEmpty())
        }

        @Test
        fun `getAllSharedPortfoliosForCurrentUser handles portfolios with empty sharedUserIds`() {
            setAuthenticationContext()
            val entityEmpty = createPortfolioEntity(sharedUserIds = emptySet())
            portfolioRepository.save(entityEmpty)
            val result = portfolioSharingService.getAllSharedPortfoliosForCurrentUser()
            assertTrue(result.isEmpty())
        }

        @Test
        fun `getAllSharedPortfolioNamesForCurrentUser returns correct names for shared user`() {
            setAuthenticationContext()
            val entity1 = createPortfolioEntity(setOf(dummySharedUserId))
            val entity2 = createPortfolioEntity(setOf(dummySharedOtherUserId))
            portfolioRepository.saveAll(listOf(entity1, entity2))
            val result = portfolioSharingService.getAllSharedPortfolioNamesForCurrentUser()
            assertEquals(1, result.size)
            assertEquals(entity1.portfolioName, result[0].portfolioName)
            assertEquals(entity1.portfolioId.toString(), result[0].portfolioId)
        }

        @Test
        fun `getAllSharedPortfolioNamesForCurrentUser returns empty if no portfolios shared`() {
            setAuthenticationContext()
            val entity = createPortfolioEntity(setOf(dummySharedOtherUserId))
            portfolioRepository.save(entity)
            val result = portfolioSharingService.getAllSharedPortfolioNamesForCurrentUser()
            assertTrue(result.isEmpty())
        }

        @Test
        fun `getAllSharedPortfolioNamesForCurrentUser ignores portfolios with empty sharedUserIds`() {
            setAuthenticationContext()
            val entity = createPortfolioEntity(emptySet())
            portfolioRepository.save(entity)
            val result = portfolioSharingService.getAllSharedPortfolioNamesForCurrentUser()
            assertTrue(result.isEmpty())
        }

        @Test
        fun `getAllSharedPortfolioNamesForCurrentUser returns multiple portfolios if shared with user`() {
            setAuthenticationContext()
            val entity1 = createPortfolioEntity(setOf(dummySharedUserId))
            val entity2 = createPortfolioEntity(setOf(dummySharedUserId, dummySharedOtherUserId))
            val entity3 = createPortfolioEntity(setOf(dummySharedOtherUserId))
            portfolioRepository.saveAll(listOf(entity1, entity2, entity3))
            val result = portfolioSharingService.getAllSharedPortfolioNamesForCurrentUser()
            assertEquals(2, result.size)
            val names = result.map { it.portfolioName }
            assertTrue(entity1.portfolioName in names)
            assertTrue(entity2.portfolioName in names)
        }
    }

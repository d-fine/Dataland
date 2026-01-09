package org.dataland.datalanduserservice.service

import org.dataland.datalanduserservice.DatalandUserService
import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.dataland.datalanduserservice.exceptions.PortfolioNotFoundApiException
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.enums.NotificationFrequency
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
                notificationFrequency = NotificationFrequency.Weekly,
                timeWindowThreshold = null,
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
            Assertions.assertEquals(1, result.size)
            Assertions.assertEquals(entity.portfolioId.toString(), result[0].portfolioId)
            Assertions.assertEquals(entity.portfolioName, result[0].portfolioName)
        }

        @Test
        fun `getAllSharedPortfoliosForCurrentUser returns empty list if no portfolios shared with current user`() {
            setAuthenticationContext()
            val entity = createPortfolioEntity(setOf(dummySharedOtherUserId))
            portfolioRepository.save(entity)
            val result = portfolioSharingService.getAllSharedPortfoliosForCurrentUser()
            Assertions.assertTrue(result.isEmpty())
        }

        @Test
        fun `getAllSharedPortfoliosForCurrentUser handles portfolios with empty sharedUserIds`() {
            setAuthenticationContext()
            val entityEmpty = createPortfolioEntity(sharedUserIds = emptySet())
            portfolioRepository.save(entityEmpty)
            val result = portfolioSharingService.getAllSharedPortfoliosForCurrentUser()
            Assertions.assertTrue(result.isEmpty())
        }

        @Test
        fun `getAllSharedPortfolioNamesForCurrentUser returns correct names for shared user`() {
            setAuthenticationContext()
            val entity1 = createPortfolioEntity(setOf(dummySharedUserId))
            val entity2 = createPortfolioEntity(setOf(dummySharedOtherUserId))
            portfolioRepository.saveAll(listOf(entity1, entity2))
            val result = portfolioSharingService.getAllSharedPortfolioNamesForCurrentUser()
            Assertions.assertEquals(1, result.size)
            Assertions.assertEquals(entity1.portfolioName, result[0].portfolioName)
            Assertions.assertEquals(entity1.portfolioId.toString(), result[0].portfolioId)
        }

        @Test
        fun `getAllSharedPortfolioNamesForCurrentUser returns empty if no portfolios shared`() {
            setAuthenticationContext()
            val entity = createPortfolioEntity(setOf(dummySharedOtherUserId))
            portfolioRepository.save(entity)
            val result = portfolioSharingService.getAllSharedPortfolioNamesForCurrentUser()
            Assertions.assertTrue(result.isEmpty())
        }

        @Test
        fun `getAllSharedPortfolioNamesForCurrentUser ignores portfolios with empty sharedUserIds`() {
            setAuthenticationContext()
            val entity = createPortfolioEntity(emptySet())
            portfolioRepository.save(entity)
            val result = portfolioSharingService.getAllSharedPortfolioNamesForCurrentUser()
            Assertions.assertTrue(result.isEmpty())
        }

        @Test
        fun `getAllSharedPortfolioNamesForCurrentUser returns multiple portfolios if shared with user`() {
            setAuthenticationContext()
            val entity1 = createPortfolioEntity(setOf(dummySharedUserId))
            val entity2 = createPortfolioEntity(setOf(dummySharedUserId, dummySharedOtherUserId))
            val entity3 = createPortfolioEntity(setOf(dummySharedOtherUserId))
            portfolioRepository.saveAll(listOf(entity1, entity2, entity3))
            val result = portfolioSharingService.getAllSharedPortfolioNamesForCurrentUser()
            Assertions.assertEquals(2, result.size)
            val names = result.map { it.portfolioName }
            Assertions.assertTrue(entity1.portfolioName in names)
            Assertions.assertTrue(entity2.portfolioName in names)
        }

        @Test
        fun `patchSharing updates sharedUserIds correctly`() {
            setAuthenticationContext(dummyOwnerUserId)
            val entity = createPortfolioEntity(setOf(dummySharedUserId))
            val saved = portfolioRepository.save(entity)
            val updatedSharedUserIds = setOf(dummySharedUserId, dummySharedOtherUserId)
            val newTimestamp = Instant.now().toEpochMilli()
            val updatedPortfolio =
                saved.toBasePortfolio().copy(
                    sharedUserIds = updatedSharedUserIds,
                    lastUpdateTimestamp = newTimestamp,
                )
            val result =
                portfolioSharingService.patchSharing(
                    saved.portfolioId,
                    updatedPortfolio,
                    correlationId = "corr-1",
                )
            Assertions.assertEquals(updatedSharedUserIds, result.sharedUserIds)
            Assertions.assertEquals(saved.portfolioId.toString(), result.portfolioId)
            Assertions.assertEquals(saved.creationTimestamp, result.creationTimestamp)
            Assertions.assertEquals(newTimestamp, result.lastUpdateTimestamp)
            val reloaded = portfolioRepository.getPortfolioByPortfolioId(saved.portfolioId)
            Assertions.assertEquals(updatedSharedUserIds, reloaded?.sharedUserIds)
        }

        @Test
        fun `patchSharing clears sharedUserIds when given empty set`() {
            setAuthenticationContext(dummyOwnerUserId)
            val entity = createPortfolioEntity(setOf(dummySharedUserId))
            val saved = portfolioRepository.save(entity)
            val updatedPortfolio =
                saved.toBasePortfolio().copy(
                    sharedUserIds = emptySet(),
                    lastUpdateTimestamp = Instant.now().toEpochMilli(),
                )
            val result =
                portfolioSharingService.patchSharing(
                    saved.portfolioId,
                    updatedPortfolio,
                    correlationId = "corr-2",
                )
            Assertions.assertTrue(result.sharedUserIds.isEmpty())
        }

        @Test
        fun `patchSharing throws PortfolioNotFoundApiException for nonexistent portfolio`() {
            setAuthenticationContext(dummyOwnerUserId)
            val nonExistentId = UUID.randomUUID()
            val dummyPortfolio = createPortfolioEntity(emptySet())
            val exception =
                assertThrows<PortfolioNotFoundApiException> {
                    portfolioSharingService.patchSharing(
                        nonExistentId,
                        dummyPortfolio.toBasePortfolio(),
                        correlationId = "corr-3",
                    )
                }
            Assertions.assertTrue(exception.message.contains(nonExistentId.toString()))
        }

        @Test
        fun `deleteCurrentUserFromSharing removes user from sharedUserIds`() {
            setAuthenticationContext(dummySharedUserId)
            val entity = createPortfolioEntity(setOf(dummySharedUserId, dummySharedOtherUserId))
            val saved = portfolioRepository.save(entity)
            portfolioSharingService.deleteCurrentUserFromSharing(saved.portfolioId, correlationId = "corr-del-1")
            val reloaded = portfolioRepository.getPortfolioByPortfolioId(saved.portfolioId)
            Assertions.assertEquals(setOf(dummySharedOtherUserId), reloaded?.sharedUserIds)
            Assertions.assertEquals(saved.portfolioId, reloaded?.portfolioId)
            Assertions.assertEquals(saved.creationTimestamp, reloaded?.creationTimestamp)
        }

        @Test
        fun `deleteCurrentUserFromSharing does nothing if user not in sharedUserIds`() {
            setAuthenticationContext(dummySharedUserId)
            val entity = createPortfolioEntity(setOf(dummySharedOtherUserId))
            val saved = portfolioRepository.save(entity)
            portfolioSharingService.deleteCurrentUserFromSharing(saved.portfolioId, correlationId = "corr-del-2")
            val reloaded = portfolioRepository.getPortfolioByPortfolioId(saved.portfolioId)
            Assertions.assertEquals(setOf(dummySharedOtherUserId), reloaded?.sharedUserIds)
        }

        @Test
        fun `deleteCurrentUserFromSharing throws PortfolioNotFoundApiException for nonexistent portfolio`() {
            setAuthenticationContext(dummySharedUserId)
            val nonExistentId = UUID.randomUUID()
            val exception =
                assertThrows<PortfolioNotFoundApiException> {
                    portfolioSharingService.deleteCurrentUserFromSharing(nonExistentId, correlationId = "corr-del-3")
                }
            Assertions.assertTrue(exception.message.contains(nonExistentId.toString()))
        }

        @Test
        fun `deleteCurrentUserFromSharing makes sharedUserIds empty if user was only one`() {
            setAuthenticationContext(dummySharedUserId)
            val entity = createPortfolioEntity(setOf(dummySharedUserId))
            val saved = portfolioRepository.save(entity)
            portfolioSharingService.deleteCurrentUserFromSharing(saved.portfolioId, correlationId = "corr-del-4")
            val reloaded = portfolioRepository.getPortfolioByPortfolioId(saved.portfolioId)
            Assertions.assertTrue(reloaded?.sharedUserIds?.isEmpty() == true)
        }
    }

package org.dataland.datalanduserservice

import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import java.util.UUID

@SpringBootTest(classes = [DatalandUserService::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class PortfolioSharingIntegrationTest
    @Autowired
    constructor(
        private val portfolioRepository: PortfolioRepository,
    ) {
        private val dummyOwnerUserId = "ownerUserId"
        private val dummySharedUserId = "sharedUserId"
        private val dummySharedOtherUserId = "sharedOtherUserId"

        private fun createPortfolioEntity(sharedUserIds: Set<String>): PortfolioEntity {
            val timestamp = Instant.now().toEpochMilli()
            return BasePortfolio(
                portfolioId = UUID.randomUUID().toString(),
                portfolioName = "Portfolio$timestamp",
                userId = dummyOwnerUserId,
                creationTimestamp = timestamp,
                lastUpdateTimestamp = timestamp,
                identifiers = setOf("companyId"),
                isMonitored = false,
                monitoredFrameworks = setOf("sfdr", "eutaxonomy"),
                sharedUserIds = sharedUserIds,
            ).toPortfolioEntity()
        }

        @BeforeEach
        fun clearRepository() {
            portfolioRepository.deleteAll()
        }

        @Test
        fun `returns shared portfolios for current user`() {
            val entity = createPortfolioEntity(setOf(dummySharedUserId))
            portfolioRepository.save(entity)
            val result = portfolioRepository.findAllBySharedUserIdsContaining(dummySharedUserId)
            Assertions.assertEquals(1, result.size)
        }

        @Test
        fun `returns empty list if no portfolios shared with current user`() {
            val entity = createPortfolioEntity(setOf(dummySharedOtherUserId))
            portfolioRepository.save(entity)
            val result = portfolioRepository.findAllBySharedUserIdsContaining(dummySharedUserId)
            Assertions.assertEquals(0, result.size)
        }

        @Test
        fun `ignores portfolios where sharedUserIds does not contain current user`() {
            val entity = createPortfolioEntity(setOf(dummySharedOtherUserId))
            portfolioRepository.save(entity)
            val result = portfolioRepository.findAllBySharedUserIdsContaining(dummySharedUserId)
            Assertions.assertEquals(0, result.size)
        }

        @Test
        fun `handles portfolios with empty sharedUserIds`() {
            val entityEmpty = createPortfolioEntity(sharedUserIds = emptySet())
            portfolioRepository.save(entityEmpty)
            val result = portfolioRepository.findAllBySharedUserIdsContaining(dummySharedUserId)
            Assertions.assertEquals(0, result.size)
        }
    }

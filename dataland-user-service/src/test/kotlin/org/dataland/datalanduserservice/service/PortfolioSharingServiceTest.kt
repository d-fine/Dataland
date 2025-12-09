package org.dataland.datalanduserservice.service

import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PortfolioSharingServiceTest {
    private val mockPortfolioRepository = mock<PortfolioRepository>()
    private val mockSecurityContext = mock<SecurityContext>()
    private lateinit var portfolioSharingService: PortfolioSharingService

    private val dummyUserId = "userId"
    private val dummySharedUserId = "sharedUserId"
    private val dummyPortfolioId = UUID.randomUUID().toString()

    private val dummyPortfolio =
        BasePortfolio(
            portfolioId = dummyPortfolioId,
            portfolioName = "Portfolio",
            userId = dummyUserId,
            creationTimestamp = Instant.now().toEpochMilli(),
            lastUpdateTimestamp = Instant.now().toEpochMilli(),
            identifiers = setOf("companyId"),
            isMonitored = false,
            monitoredFrameworks = setOf("sfdr", "eutaxonomy"),
            sharedUserIds = setOf(dummySharedUserId),
        )

    @BeforeEach
    fun setup() {
        resetSecurityContext()
        doAnswer { it.arguments[0] }.whenever(mockPortfolioRepository).save(any())
        portfolioSharingService =
            PortfolioSharingService(mockPortfolioRepository)
    }

    private fun resetSecurityContext() {
        val mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                "username",
                dummyUserId,
                setOf(),
            )
        doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }
}

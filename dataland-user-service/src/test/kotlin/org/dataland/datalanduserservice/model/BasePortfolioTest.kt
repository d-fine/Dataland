package org.dataland.datalanduserservice.model

import org.dataland.datalanduserservice.model.enums.NotificationFrequency
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

class BasePortfolioTest {
    private val mockSecurityContext = mock<SecurityContext>()
    private val userId = "user-id"
    private val validCompanyId = "valid-company-id"
    private val proxyCompanyId = "proxy-company-id"

    @BeforeEach
    fun setup() {
        val mockAuthentication: DatalandAuthentication =
            AuthenticationMock.mockJwtAuthentication("username", userId, setOf(DatalandRealmRole.ROLE_USER))
        doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    private val portfolioUploadWithReplacements =
        PortfolioUpload(
            portfolioName = "Test Portfolio",
            identifiers = setOf(validCompanyId, proxyCompanyId),
            isMonitored = true,
            monitoredFrameworks = setOf("sfdr"),
            notificationFrequency = NotificationFrequency.Weekly,
            timeWindowThreshold = null,
            sharedUserIds = emptySet(),
            proxyCompanyReplacements =
                listOf(
                    ProxyCompanyReplacementUpload(proxiedCompanyId = validCompanyId, proxyCompanyId = proxyCompanyId),
                ),
        )

    @Test
    fun `test that constructing a BasePortfolio from a PortfolioUpload with replacements sets fields correctly`() {
        val basePortfolio = BasePortfolio(portfolioUploadWithReplacements)

        assertEquals(portfolioUploadWithReplacements.portfolioName, basePortfolio.portfolioName)
        assertEquals(userId, basePortfolio.userId)
        assertEquals(portfolioUploadWithReplacements.identifiers, basePortfolio.identifiers)
        assertEquals(1, basePortfolio.proxyCompanyReplacements?.size)
        assertEquals(userId, basePortfolio.proxyCompanyReplacements?.first()?.userId)
        assertEquals(validCompanyId, basePortfolio.proxyCompanyReplacements?.first()?.proxiedCompanyId)
        assertEquals(proxyCompanyId, basePortfolio.proxyCompanyReplacements?.first()?.proxyCompanyId)
    }

    @Test
    fun `test that constructing a BasePortfolio from a PortfolioUpload without replacements yields null replacements`() {
        val portfolioUpload = portfolioUploadWithReplacements.copy(proxyCompanyReplacements = null)
        val basePortfolio = BasePortfolio(portfolioUpload)
        assertNull(basePortfolio.proxyCompanyReplacements)
    }

    @Test
    fun `test that constructing a BasePortfolio from a PortfolioUpload with empty replacements list yields null`() {
        val portfolioUpload = portfolioUploadWithReplacements.copy(proxyCompanyReplacements = emptyList())
        val basePortfolio = BasePortfolio(portfolioUpload)
        assertNull(basePortfolio.proxyCompanyReplacements)
    }

    @Test
    fun `test that constructing a BasePortfolio from a PortfolioMonitoringPatch sets fields correctly`() {
        val patch =
            PortfolioMonitoringPatch(
                isMonitored = true,
                monitoredFrameworks = setOf("sfdr"),
                notificationFrequency = NotificationFrequency.Monthly,
                timeWindowThreshold = TimeWindowThreshold.Extended,
            )
        val basePortfolio = BasePortfolio(patch)
        assertEquals(userId, basePortfolio.userId)
        assertEquals(patch.isMonitored, basePortfolio.isMonitored)
        assertEquals(patch.monitoredFrameworks, basePortfolio.monitoredFrameworks)
        assertEquals(patch.notificationFrequency, basePortfolio.notificationFrequency)
        assertEquals(patch.timeWindowThreshold, basePortfolio.timeWindowThreshold)
        assertTrue(basePortfolio.identifiers.isEmpty())
        assertTrue(basePortfolio.sharedUserIds.isEmpty())
    }

    @Test
    fun `test that constructing a BasePortfolio from a PortfolioSharingPatch sets fields correctly`() {
        val patch = PortfolioSharingPatch(sharedUserIds = setOf("shared-user-id"))
        val basePortfolio = BasePortfolio(patch)
        assertEquals(userId, basePortfolio.userId)
        assertEquals(patch.sharedUserIds, basePortfolio.sharedUserIds)
        assertEquals(false, basePortfolio.isMonitored)
        assertEquals(NotificationFrequency.Weekly, basePortfolio.notificationFrequency)
        assertNull(basePortfolio.timeWindowThreshold)
    }

    @Test
    fun `test that toPortfolioEntity converts proxyCompanyReplacements correctly`() {
        val basePortfolio = BasePortfolio(portfolioUploadWithReplacements)
        val entity = basePortfolio.toPortfolioEntity()

        assertEquals(1, entity.proxyCompanyReplacements.size)
        val replacement = entity.proxyCompanyReplacements.first()
        assertEquals(validCompanyId, replacement.proxiedCompanyId)
        assertEquals(proxyCompanyId, replacement.proxyCompanyId)
        assertEquals(userId, replacement.userId)
    }

    @Test
    fun `test that toPortfolioEntity handles null proxyCompanyReplacements`() {
        val basePortfolio = BasePortfolio(portfolioUploadWithReplacements.copy(proxyCompanyReplacements = null))
        val entity = basePortfolio.toPortfolioEntity()
        assertTrue(entity.proxyCompanyReplacements.isEmpty())
    }

    @Test
    fun `test that toPortfolioEntity uses overridden parameters when provided`() {
        val basePortfolio = BasePortfolio(portfolioUploadWithReplacements)
        val overriddenPortfolioId = "11111111-1111-1111-1111-111111111111"
        val entity =
            basePortfolio.toPortfolioEntity(
                portfolioId = overriddenPortfolioId,
                creationTimestamp = 123L,
                lastUpdateTimestamp = 456L,
                isMonitored = false,
                monitoredFrameworks = emptySet(),
                notificationFrequency = NotificationFrequency.Monthly,
                timeWindowThreshold = TimeWindowThreshold.Standard,
                sharedUserIds = setOf("other-user"),
                proxyCompanyReplacements = null,
            )

        assertEquals(overriddenPortfolioId, entity.portfolioId.toString())
        assertEquals(123L, entity.creationTimestamp)
        assertEquals(456L, entity.lastUpdateTimestamp)
        assertEquals(false, entity.isMonitored)
        assertTrue(entity.monitoredFrameworks.isNullOrEmpty())
        assertEquals(NotificationFrequency.Monthly, entity.notificationFrequency)
        assertEquals(TimeWindowThreshold.Standard, entity.timeWindowThreshold)
        assertEquals(setOf("other-user"), entity.sharedUserIds)
        assertTrue(entity.proxyCompanyReplacements.isEmpty())
    }
}

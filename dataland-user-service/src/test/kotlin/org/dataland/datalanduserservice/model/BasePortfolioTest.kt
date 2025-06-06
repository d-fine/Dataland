import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.PortfolioUpload
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class BasePortfolioTest {
    @Test
    fun `check that keepMonitoringInvariant preserves monitoring fields when upload has nulls`() {
        // Original portfolio with monitoring info
        val original =
            BasePortfolio(
                portfolioId = "123e4567-e89b-12d3-a456-426614174000",
                portfolioName = "Original Portfolio",
                userId = "user-1",
                creationTimestamp = 1000L,
                lastUpdateTimestamp = 2000L,
                companyIds = setOf("companyA"),
                isMonitored = true,
                startingMonitoringPeriod = "2023-01",
                monitoredFrameworks = setOf("frameworkA", "frameworkB"),
            )

        // Upload that has some null monitoring fields - should preserve original's values for those fields
        val upload =
            PortfolioUpload(
                portfolioName = "Updated Portfolio",
                companyIds = setOf("companyB"),
                isMonitored = null,
                startingMonitoringPeriod = null,
                monitoredFrameworks = null,
            )

        val result = BasePortfolio.keepMonitoringInvariant(original, upload)

        assertEquals(original.isMonitored, result.isMonitored)
        assertEquals(original.startingMonitoringPeriod, result.startingMonitoringPeriod)
        assertEquals(original.monitoredFrameworks, result.monitoredFrameworks)

        assertEquals(upload.portfolioName, result.portfolioName)
        assertEquals(upload.companyIds, result.companyIds)

        assertEquals(original.portfolioId, result.portfolioId)
        assertEquals(original.userId, result.userId)
        assertNotEquals(original.lastUpdateTimestamp, result.lastUpdateTimestamp) // updated timestamp
    }

    @Test
    fun `check that keepMonitoringInvariant uses upload monitoring fields when present`() {
        val original =
            BasePortfolio(
                portfolioId = "123e4567-e89b-12d3-a456-426614174000",
                portfolioName = "Original Portfolio",
                userId = "user-1",
                creationTimestamp = 1000L,
                lastUpdateTimestamp = 2000L,
                companyIds = setOf("companyA"),
                isMonitored = true,
                startingMonitoringPeriod = "2023-01",
                monitoredFrameworks = setOf("frameworkA"),
            )

        val upload =
            PortfolioUpload(
                portfolioName = "Updated Portfolio",
                companyIds = setOf("companyB"),
                isMonitored = false,
                startingMonitoringPeriod = "2024-01",
                monitoredFrameworks = setOf("frameworkX", "frameworkY"),
            )

        val result = BasePortfolio.keepMonitoringInvariant(original, upload)

        assertEquals(upload.isMonitored, result.isMonitored)
        assertEquals(upload.startingMonitoringPeriod, result.startingMonitoringPeriod)
        assertEquals(upload.monitoredFrameworks, result.monitoredFrameworks)
    }
}

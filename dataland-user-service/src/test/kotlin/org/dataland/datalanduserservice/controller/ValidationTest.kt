package org.dataland.datalanduserservice.controller

import jakarta.validation.Validation
import org.dataland.datalanduserservice.model.PortfolioUpload
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValidationTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `test that validation of PortfolioUpload with empty companyIds works as expected`() {
        val portfolio =
            PortfolioUpload(
                portfolioName = "Invalid Portfolio",
                companyIds = emptySet(),
                isMonitored = true,
                startingMonitoringPeriod = "2023",
                monitoredFrameworks = mutableSetOf("sfdr", "eutaxonomy"),
            )
        val violations = validator.validate(portfolio)
        assertEquals(1, violations.size)
    }
}

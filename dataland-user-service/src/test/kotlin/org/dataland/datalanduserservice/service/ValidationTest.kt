package org.dataland.datalanduserservice.service

import jakarta.validation.Validation
import org.dataland.datalanduserservice.model.PortfolioPayload
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValidationTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `test that validation of PortfolioPayload with empty companyIds and empty dataTypes works as expected`() {
        val portfolio =
            PortfolioPayload(
                portfolioName = "Invalid Portfolio",
                companyIds = emptySet(),
                dataTypes = emptySet(),
            )
        val violations = validator.validate(portfolio)
        assertEquals(2, violations.size)
    }
}

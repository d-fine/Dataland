package org.dataland.datalanduserservice.utils

import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import org.dataland.datalanduserservice.model.PortfolioMonitoringPatch
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PortfolioMonitoringValidatorTest {
    private lateinit var validatorFactory: ValidatorFactory
    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        validatorFactory = Validation.buildDefaultValidatorFactory()
        validator = validatorFactory.validator
    }

    @Test
    fun `valid when monitored and fields are properly set`() {
        val monitoring =
            PortfolioMonitoringPatch(
                isMonitored = true,
                monitoredFrameworks = setOf("sfdr"),
            )

        val violations = validator.validate(monitoring)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `invalid when monitored but missing frameworks`() {
        val monitoring =
            PortfolioMonitoringPatch(
                isMonitored = true,
                monitoredFrameworks = emptySet(),
            )

        val violations = validator.validate(monitoring)
        assertFalse(violations.isEmpty())
    }

    @Test
    fun `invalid when monitored but missing starting period`() {
        val monitoring =
            PortfolioMonitoringPatch(
                isMonitored = true,
                monitoredFrameworks = setOf("eutaxonomy"),
            )

        val violations = validator.validate(monitoring)
        assertFalse(violations.isEmpty())
    }

    @Test
    fun `invalid when not monitored but fields are set`() {
        val monitoring =
            PortfolioMonitoringPatch(
                isMonitored = false,
                monitoredFrameworks = setOf("ESG"),
            )

        val violations = validator.validate(monitoring)
        assertFalse(violations.isEmpty())
    }

    @Test
    fun `valid when not monitored and fields are empty`() {
        val monitoring =
            PortfolioMonitoringPatch(
                isMonitored = false,
                monitoredFrameworks = emptySet(),
            )

        val violations = validator.validate(monitoring)
        assertTrue(violations.isEmpty())
    }
}

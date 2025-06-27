package org.dataland.datalanduserservice.utils

import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import org.dataland.datalanduserservice.model.PortfolioMonitoringPatch
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class PortfolioMonitoringValidatorTest {
    companion object {
        private lateinit var validatorFactory: ValidatorFactory
        private lateinit var validator: Validator

        @BeforeAll
        @JvmStatic
        fun setup() {
            validatorFactory = Validation.buildDefaultValidatorFactory()
            validator = validatorFactory.validator
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            validatorFactory.close()
        }
    }

    @Test
    fun `valid when monitored and fields are properly set`() {
        val monitoring =
            PortfolioMonitoringPatch(
                isMonitored = true,
                monitoredFrameworks = setOf("sfdr"),
                startingMonitoringPeriod = "2022",
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
                startingMonitoringPeriod = "2024",
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
                startingMonitoringPeriod = "",
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
                startingMonitoringPeriod = "2023",
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
                startingMonitoringPeriod = null,
            )

        val violations = validator.validate(monitoring)
        assertTrue(violations.isEmpty())
    }
}

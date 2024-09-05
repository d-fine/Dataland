package org.dataland.datalandbackend.validator

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ValueQualityCrossValidationTest {
    private lateinit var validator: Validator
    private lateinit var violations: Set<ConstraintViolation<ExtendedDataPoint<*>>>

    @BeforeEach
    fun setup() {
        this.validator = Validation.buildDefaultValidatorFactory().validator
        this.violations = mutableSetOf()
    }

    @Test
    fun `test ExtendedDataPoint with null value and null quality`() {
        val dataPoint = ExtendedDataPoint<Number>()
        this.violations = this.validator.validate(dataPoint)
        assertTrue(this.violations.isEmpty())
    }

    @Test
    fun `test ExtendedDataPoint with null value and non-null quality`() {
        lateinit var dataPoint: ExtendedDataPoint<Number>
        for (quality in QualityOptions.entries) {
            dataPoint = ExtendedDataPoint(null, quality)
            this.violations = this.validator.validate(dataPoint)
            when (quality) {
                QualityOptions.NoDataFound -> assertTrue(this.violations.isEmpty())
                else -> {
                    assertTrue(this.violations.isNotEmpty())
                    assertTrue(this.violations.size == 1)
                }
            }
        }
    }

    @Test
    fun `test ExtendedDataPoint with non-null value and null quality`() {
        val dataPoint: ExtendedDataPoint<Number> = ExtendedDataPoint(value = 0, quality = null)
        val violations = validator.validate(dataPoint)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `test ExtendedDataPoint with non-null value and valid quality`() {
        lateinit var dataPoint: ExtendedDataPoint<Number>
        for (quality in QualityOptions.entries) {
            dataPoint = ExtendedDataPoint(0, quality)
            this.violations = this.validator.validate(dataPoint)
            assertTrue(violations.isEmpty())
        }
    }
}

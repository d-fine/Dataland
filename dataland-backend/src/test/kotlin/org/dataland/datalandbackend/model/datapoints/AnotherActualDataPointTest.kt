package org.dataland.datalandbackend.model.datapoints

import jakarta.validation.Validation
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class AnotherActualDataPointTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `Fiddle around`() {
        val actualDatapoint = AnotherActualDataPoint(BigDecimal.ONE, "USD")
        val violations = validator.validate(actualDatapoint)
        println(violations)
    }

    @Test
    fun `Fiddle around more`() {
        val actualDatapoint =
            AnotherActualDataPoint(
                value = BigDecimal.ONE,
                currency = "USD",
                comment = null,
            )
        val violations = validator.validate(actualDatapoint)
        println(violations)
        println(actualDatapoint.toString())
    }
}

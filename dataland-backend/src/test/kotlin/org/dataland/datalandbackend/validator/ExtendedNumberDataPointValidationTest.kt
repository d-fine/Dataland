package org.dataland.datalandbackend.validator

import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ExtendedNumberDataPointValidationTest {
    val numberDataPointValidator = ExtendedNumberDataPointValidator()
    val dataPointWrongType = ExtendedDataPoint<Double>(12.1, QualityOptions.Reported)

    @Test
    fun testIfExceptionIsThrownForWrongDatatype() {
        val errorThrown = assertThrows<Exception> {
            numberDataPointValidator.isValid(dataPointWrongType, null)
        }
        Assertions.assertTrue(
            errorThrown.message!!.contains(
                "inside ExtendedDataPoint is not handled by number validator",
            ),
        )
    }
}

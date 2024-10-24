package org.dataland.datalandbackend.model.datapoints

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.validation.Validation
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.LocalDate
import org.dataland.datalandbackend.frameworks.lksg.model.general.masterData.LksgGeneralMasterData
import org.junit.jupiter.api.Test
import org.dataland.datalandbackend.model.datapoints.StandardPercentageValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class StandartPercentageValueTest {

    @Test
    fun `Check that the validation passes correctly`() {
        assertDoesNotThrow {
            StandardPercentageValue(
                value = BigDecimal.valueOf(0.5),
                applicable = true,
                )
        }
    }

    @Test
    fun `Check that the validation fails correctly`() {
        val validator = Validation.buildDefaultValidatorFactory().validator
        val objectMapper = jacksonObjectMapper().findAndRegisterModules()



        val className = "org.dataland.datalandbackend.model.datapoints.StandardPercentageValue"
        val kotlinClass2 = Class.forName(className).kotlin.java
        assertEquals(kotlinClass2, StandardPercentageValue::class.java)
        //println(kotlinClass2.simpleName)
        //println(kotlinClass2.qualifiedName)
        val kotlinClass = StandardPercentageValue::class.java
        println(kotlinClass.name)
        println(kotlinClass.`package`)


        val testJson = """
            {
                "value": -0.5,
                "applicable": true
            }
        """.trimIndent()
        val test2 = objectMapper.readValue(testJson, kotlinClass2)
        val test = objectMapper.readValue(testJson, kotlinClass)
        assertEquals(test2, test)
        assert(validator.validate(test).size > 0)
    }
}
package org.dataland.csvconverter.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.math.BigDecimal

/**
 * Ensures that all parsed BigDecimals are scaled unambiguously. This is to ensure
 * that BigDecimals can be compared easily
 */
class NoTrailingZerosBigDecimalDeserializer : JsonDeserializer<BigDecimal>() {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext?): BigDecimal {
        return jp.decimalValue.stripTrailingZeros()
    }
}

package org.dataland.csvconverter.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.math.BigDecimal

/**
 * Ensures that BigDecimals get converted to JSON in a human-readable form
 */
class HumanReadableBigDecimalSerializer : JsonSerializer<BigDecimal>() {
    override fun serialize(value: BigDecimal, jgen: JsonGenerator, provider: SerializerProvider?) {
        jgen.writeNumber(value.toPlainString())
    }
}

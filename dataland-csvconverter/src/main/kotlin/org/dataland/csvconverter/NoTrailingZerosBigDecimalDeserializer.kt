package org.dataland.csvconverter

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.math.BigDecimal


class NoTrailingZerosBigDecimalDeserializer : JsonDeserializer<BigDecimal>() {

    companion object {
        val module = SimpleModule("bigdecimal-formatter")
            .addDeserializer(BigDecimal::class.java, NoTrailingZerosBigDecimalDeserializer())
            .addSerializer(BigDecimal::class.java, HumanReadableBigDecimalSerializer())
    }

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext?): BigDecimal {
        return jp.decimalValue.stripTrailingZeros()
    }
}
package org.dataland.csvconverter.json

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.math.BigDecimal

/**
 * This object provides a pre-configured Jackson ObjectMapper
 */
object JsonConfig {

    private val helperModule = SimpleModule("bigdecimal-formatter")
        .addDeserializer(BigDecimal::class.java, NoTrailingZerosBigDecimalDeserializer())
        .addSerializer(BigDecimal::class.java, HumanReadableBigDecimalSerializer())

    val objectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .registerModule(helperModule)

    /**
     * This function exports a given input to a JSON file
     * using the configured jackson ObjectMapper
     */
    fun <T> exportJson(fileName: String, input: T) {
        objectMapper.writerWithDefaultPrettyPrinter()
            .writeValue(
                File(fileName),
                input,
            )
    }
}

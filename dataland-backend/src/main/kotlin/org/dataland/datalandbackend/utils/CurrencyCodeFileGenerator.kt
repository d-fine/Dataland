package org.dataland.datalandbackend.utils

import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import java.io.File
import java.util.Currency
import java.util.Locale

/**
 * Generates the shared ISO 4217 currency code list (currencyCodes.json) from the currencies known to the JDK.
 * The file is used by the backend validator and copied into the frontend.
 * It is triggered via the framework toolbox task "runGenerateCurrencyCodes".
 */
object CurrencyCodeFileGenerator {
    /**
     * Builds the sorted list of currency entries (code and English display name).
     */
    fun buildCurrencyEntries(): List<Map<String, String>> =
        Currency
            .getAvailableCurrencies()
            .sortedBy { it.currencyCode }
            .map { mapOf("code" to it.currencyCode, "name" to it.getDisplayName(Locale.ENGLISH)) }

    /**
     * Serializes the currency entries as JSON with one entry per line.
     */
    fun buildJson(): String =
        buildCurrencyEntries().joinToString(prefix = "[\n", separator = ",\n", postfix = "\n]\n") { entry ->
            val code = defaultObjectMapper.writeValueAsString(entry["code"])
            val name = defaultObjectMapper.writeValueAsString(entry["name"])
            "  { \"code\": $code, \"name\": $name }"
        }

    /**
     * Writes the currency list to the file given as first argument.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        require(args.size == 1) { "Please specify exactly one argument: the output file path." }
        File(args[0]).writeText(buildJson(), Charsets.UTF_8)
    }
}

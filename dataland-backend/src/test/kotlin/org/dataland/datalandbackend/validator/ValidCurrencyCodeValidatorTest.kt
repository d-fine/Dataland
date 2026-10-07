package org.dataland.datalandbackend.validator

import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.validation.Validation
import org.dataland.datalandbackend.utils.CurrencyCodeFileGenerator
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.core.io.ClassPathResource
import java.io.File

class ValidCurrencyCodeValidatorTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    private data class CurrencyHolder(
        @field:ValidCurrencyCode
        val currency: String?,
    )

    private data class CodeEntry(
        val code: String,
        val name: String,
    )

    private fun violations(currency: String?) = validator.validate(CurrencyHolder(currency)).size

    @ParameterizedTest
    @ValueSource(strings = ["EUR", "USD", "JPY"])
    fun `check that known currency codes are accepted`(code: String) {
        assertEquals(0, violations(code))
    }

    @ParameterizedTest
    @ValueSource(strings = ["XYZ", "eur", "", " EUR", "EURO", "US"])
    fun `check that invalid currency codes are rejected`(code: String) {
        assertEquals(1, violations(code))
    }

    @Test
    fun `check that null is accepted`() {
        assertEquals(0, violations(null))
    }

    @Test
    fun `check that the committed currency code file is well formed`() {
        val entries = ClassPathResource("currencyCodes.json").inputStream.use { defaultObjectMapper.readValue<List<CodeEntry>>(it) }
        assertWellFormed(entries)
    }

    @Test
    fun `check that the generator produces a well formed currency code list`() {
        val file = File.createTempFile("currencyCodes", ".json")
        try {
            CurrencyCodeFileGenerator.main(arrayOf(file.absolutePath))
            val entries = defaultObjectMapper.readValue<List<CodeEntry>>(file)
            assertWellFormed(entries)
            assertTrue(entries.any { it.code == "EUR" })
        } finally {
            file.delete()
        }
    }

    private fun assertWellFormed(entries: List<CodeEntry>) {
        val codes = entries.map { it.code }
        assertEquals(codes.toSet().size, codes.size, "Duplicate codes found")
        assertEquals(codes.sorted(), codes, "Codes must be sorted")
        assertTrue(codes.all { Regex("[A-Z]{3}").matches(it) })
        assertTrue(entries.all { it.name.isNotBlank() })
    }
}

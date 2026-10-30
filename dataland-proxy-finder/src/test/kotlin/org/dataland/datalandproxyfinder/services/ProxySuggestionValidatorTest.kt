package org.dataland.datalandproxyfinder.services

import org.dataland.datalandbackendutils.exceptions.InvalidProxySuggestionApiException
import org.dataland.datalandbackendutils.model.proxyfinder.ProxySuggestion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class ProxySuggestionValidatorTest {
    private val mockDatalandCompanyService = mock<DatalandCompanyService>()
    private lateinit var validator: ProxySuggestionValidator

    private val companyId = UUID.randomUUID().toString()
    private val proxyCompanyId = UUID.randomUUID().toString()

    @BeforeEach
    fun setup() {
        reset(mockDatalandCompanyService)
        validator = ProxySuggestionValidator(mockDatalandCompanyService)
    }

    private fun buildSuggestion(
        proxyCompanyId: String = this.proxyCompanyId,
        companyId: String = this.companyId,
        confidence: Double = 0.5,
    ) = ProxySuggestion(
        companyId = companyId,
        proxyCompanyId = proxyCompanyId,
        confidence = confidence,
        reasoning = "reasoning",
        sources = emptyList(),
    )

    private fun assertValidationFailsWith(
        expectedSummary: String,
        suggestion: ProxySuggestion,
    ) {
        val exception = assertThrows<InvalidProxySuggestionApiException> { validator.validate(companyId, suggestion) }
        assertEquals(expectedSummary, exception.summary)
    }

    @Test
    fun `a suggestion of an existing other company is valid`() {
        doReturn(true).whenever(mockDatalandCompanyService).companyExists(proxyCompanyId)
        assertDoesNotThrow { validator.validate(companyId, buildSuggestion()) }
    }

    @Test
    fun `a suggestion of an unknown company is rejected`() {
        doReturn(false).whenever(mockDatalandCompanyService).companyExists(proxyCompanyId)
        assertValidationFailsWith(ProxySuggestionValidator.UNKNOWN_PROXY_COMPANY_SUMMARY, buildSuggestion())
    }

    @Test
    fun `a suggestion of the company itself is rejected without querying the backend`() {
        assertValidationFailsWith(
            ProxySuggestionValidator.SELF_REFERENCE_SUMMARY,
            buildSuggestion(proxyCompanyId = companyId.uppercase()),
        )
        verify(mockDatalandCompanyService, never()).companyExists(any())
    }

    @Test
    fun `a suggestion for a different company is rejected`() {
        assertValidationFailsWith(
            ProxySuggestionValidator.COMPANY_MISMATCH_SUMMARY,
            buildSuggestion(companyId = UUID.randomUUID().toString()),
        )
    }

    @ParameterizedTest
    @ValueSource(doubles = [-0.1, 1.1])
    fun `a suggestion with a confidence outside of 0 and 1 is rejected`(confidence: Double) {
        assertValidationFailsWith(
            ProxySuggestionValidator.INVALID_CONFIDENCE_SUMMARY,
            buildSuggestion(confidence = confidence),
        )
    }
}

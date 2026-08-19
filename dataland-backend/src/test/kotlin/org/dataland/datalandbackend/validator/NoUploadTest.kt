package org.dataland.datalandbackend.validator

import jakarta.validation.Valid
import jakarta.validation.Validation
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class NoUploadTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    private data class DataPointHolder(
        @NoUpload
        val dataPoint: ExtendedDataPoint<BigDecimal>?,
    )

    private data class ExplicitFieldSiteHolder(
        @field:NoUpload
        val dataPoint: ExtendedDataPoint<BigDecimal>?,
    )

    private data class StringHolder(
        @NoUpload
        val value: String?,
    )

    private data class NumberHolder(
        @NoUpload
        val value: Number?,
    )

    private data class ListHolder(
        @NoUpload
        val value: List<String>?,
    )

    private data class NestedHolder(
        @field:Valid
        val inner: DataPointHolder,
    )

    private fun assertNumberOfViolations(
        holder: Any,
        expectedNumberOfViolations: Int,
    ) {
        assertEquals(expectedNumberOfViolations, validator.validate(holder).size)
    }

    @Test
    fun `check that a null data point passes the validation`() {
        assertNumberOfViolations(DataPointHolder(null), 0)
        assertNumberOfViolations(ExplicitFieldSiteHolder(null), 0)
    }

    @Test
    fun `check that null values of arbitrary field types pass the validation`() {
        assertNumberOfViolations(StringHolder(null), 0)
        assertNumberOfViolations(NumberHolder(null), 0)
        assertNumberOfViolations(ListHolder(null), 0)
    }

    @Test
    fun `check that an all null data point object fails the validation`() {
        assertNumberOfViolations(DataPointHolder(ExtendedDataPoint()), 1)
    }

    @Test
    fun `check that a populated data point fails the validation`() {
        assertNumberOfViolations(DataPointHolder(ExtendedDataPoint(value = BigDecimal.ONE)), 1)
        assertNumberOfViolations(DataPointHolder(ExtendedDataPoint(quality = QualityOptions.Reported)), 1)
        assertNumberOfViolations(
            DataPointHolder(
                ExtendedDataPoint(dataSource = ExtendedDocumentReference(fileReference = "someFileReference")),
            ),
            1,
        )
    }

    @Test
    fun `check that non null values of arbitrary field types fail the validation`() {
        assertNumberOfViolations(StringHolder("abc"), 1)
        assertNumberOfViolations(StringHolder(""), 1)
        assertNumberOfViolations(NumberHolder(42), 1)
        assertNumberOfViolations(ListHolder(emptyList()), 1)
        assertNumberOfViolations(ListHolder(listOf("a")), 1)
    }

    @Test
    fun `check that the violation carries the expected message and property path`() {
        val violation = validator.validate(DataPointHolder(ExtendedDataPoint(value = BigDecimal.ONE))).single()
        assertEquals(
            "Input validation failed: This field must not be uploaded and has to be 'null'.",
            violation.message,
        )
        assertEquals("dataPoint", violation.propertyPath.toString())
    }

    @Test
    fun `check that the annotation behaves identically with an explicit field use site target`() {
        assertNumberOfViolations(ExplicitFieldSiteHolder(ExtendedDataPoint(value = BigDecimal.ONE)), 1)
    }

    @Test
    fun `check that the validation is applied to cascaded nested objects`() {
        val violation =
            validator
                .validate(
                    NestedHolder(DataPointHolder(ExtendedDataPoint(value = BigDecimal.ONE))),
                ).single()
        assertEquals("inner.dataPoint", violation.propertyPath.toString())
    }
}

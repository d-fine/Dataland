package org.dataland.datalandbackend.services.dataPoints

import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.services.datapoints.createCommentEuTaxonomyShare
import org.dataland.datalandbackend.utils.SOURCE_FRAMEWORK_NAME
import org.dataland.datalandbackend.utils.createCommentSourceFrameworksByType
import org.dataland.datalandbackend.utils.createCommentSpecs
import org.dataland.datalandbackend.utils.createFrameworkSpecification
import org.dataland.datalandbackend.utils.createUploadedDataPoint
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The following test check that the comment includes the information about the original framework
 */
class DataPointConversionCommentsTest {
    private val inputs =
        listOf(
            createUploadedDataPoint("{}").copy(dataPointType = "type1"),
            createUploadedDataPoint("{}").copy(dataPointType = "type2"),
        )
    private val dataPoints =
        listOf(
            ExtendedDataPoint(value = "value1", quality = QualityOptions.Reported, comment = null),
            ExtendedDataPoint(value = "value2", quality = QualityOptions.Reported, comment = null),
        )

    @Test
    fun `createCommentEuTaxonomyShare uses the single framework name when all sources share one framework`() {
        val comment =
            createCommentEuTaxonomyShare(
                inputs, createCommentSpecs(), dataPoints, createCommentSourceFrameworksByType(),
            )

        val expectedPrefix =
            "This share was derived from the $SOURCE_FRAMEWORK_NAME framework"
        assertTrue(comment.startsWith(expectedPrefix))
    }

    @Test
    fun `createCommentEuTaxonomyShare falls back to multiple when sources belong to different frameworks`() {
        val sourceFrameworksByType =
            mapOf(
                "type1" to listOf(createFrameworkSpecification("fw-a", "Framework A")),
                "type2" to listOf(createFrameworkSpecification("fw-b", "Framework B")),
            )

        val comment =
            createCommentEuTaxonomyShare(
                inputs, createCommentSpecs(), dataPoints, sourceFrameworksByType,
            )

        val expectedPrefix = "This share was derived from the multiple framework"
        assertTrue(comment.startsWith(expectedPrefix))
    }
}

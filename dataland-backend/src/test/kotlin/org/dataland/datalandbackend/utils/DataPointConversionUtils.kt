package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackendutils.model.DataPointType
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import org.junit.jupiter.api.Assertions.assertEquals
import java.math.BigDecimal

const val SOURCE_FRAMEWORK_NAME = "Test Framework"
const val SOURCE_FRAMEWORK_ID = "source-framework"

val dummyRef = IdWithRef(id = "dummy", ref = "dummy")

val dummySpecs =
    mapOf(
        "dummy" to
            DataPointTypeSpecification(
                dataPointType = dummyRef,
                name = "dummy",
                businessDefinition = "dummy",
                dataPointBaseType = dummyRef,
                usedBy = emptyList(),
                calculationRules = emptyList(),
            ),
    )

/**
 * Creates a minimal framework specification fixture for source framework comment rendering tests.
 *
 * @param frameworkId framework identifier to set on the fixture
 * @param frameworkName framework display name to set on the fixture
 * @return a framework specification fixture
 */
fun createFrameworkSpecification(
    frameworkId: String,
    frameworkName: String,
): FrameworkSpecification =
    FrameworkSpecification(
        framework = IdWithRef(id = frameworkId, ref = "dummy"),
        name = frameworkName,
        businessDefinition = "dummy",
        schema = "{}",
        referencedReportJsonPath = null,
    )

val sourceFrameworksByType =
    mapOf("dummy" to listOf(createFrameworkSpecification("test-framework", SOURCE_FRAMEWORK_NAME)))

/**
 * Asserts that the given actual value is numerically equal to the expected value, ignoring scale differences.
 */
fun assertBigDecimalEquals(
    expectedValue: String,
    actualValue: BigDecimal?,
) {
    assertEquals(0, BigDecimal(expectedValue).compareTo(actualValue))
}

/**
 * Creates a dummy uploaded data point wrapping the given raw JSON data point payload.
 */
fun createUploadedDataPoint(dataPoint: String): UploadedDataPoint =
    UploadedDataPoint(
        dataPoint = dataPoint,
        companyId = "dummy",
        reportingPeriod = "dummy",
        dataPointType = "dummy",
    )

/**
 * Creates a dummy uploaded data point with a placeholder payload for the given source data point type.
 */
fun createDummyUploadedDataPoint(dataPointType: String) =
    UploadedDataPoint(
        dataPoint = "dummy",
        companyId = "dummy",
        reportingPeriod = "dummy",
        dataPointType = dataPointType,
    )

private fun createDummySpec(
    dummyRef: IdWithRef,
    name: String,
): DataPointTypeSpecification =
    DataPointTypeSpecification(
        dataPointType = dummyRef,
        name = name,
        businessDefinition = "dummy",
        dataPointBaseType = dummyRef,
        usedBy = emptyList(),
        calculationRules = emptyList(),
    )

/**
 * Creates source data point type specification fixtures used by the comment test cases.
 */
fun createCommentSpecs(): Map<DataPointType, DataPointTypeSpecification> {
    val dummyRef = IdWithRef(id = "dummy", ref = "dummy")
    return mapOf(
        "type1" to createDummySpec(dummyRef, "Input1"),
        "type2" to createDummySpec(dummyRef, "Input2"),
        "type3" to createDummySpec(dummyRef, "Input3"),
    )
}

/**
 * Creates source framework fixtures for the data point types used by the comment test cases.
 *
 * @return source framework specifications grouped by source data point type
 */
fun createCommentSourceFrameworksByType(): Map<DataPointType, List<FrameworkSpecification>> {
    val sourceFramework = createFrameworkSpecification(SOURCE_FRAMEWORK_ID, SOURCE_FRAMEWORK_NAME)
    return mapOf(
        "type1" to listOf(sourceFramework),
        "type2" to listOf(sourceFramework),
        "type3" to listOf(sourceFramework),
    )
}

fun sourceBlock(
    index: Int,
    sourceName: String,
    sourceComment: String? = null,
    frameworkName: String = SOURCE_FRAMEWORK_NAME,
): String =
    "[$index] $sourceName\n" +
        "+ Framework: $frameworkName" +
        (sourceComment?.let { "\n+ Comment: $it" } ?: "")

fun sourcesSection(vararg sourceBlocks: String): String = sourceBlocks.joinToString(separator = "\n\n")

package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.utils.DataExportUtils
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DataExportUtilsTest {
    @Test
    fun `stripFieldNames with known alias and suffixes`() {
        val aliasExportMap =
            mapOf(
                "revenue.nonAlignedActivities" to "REV_NON_ALIGNED_ACTIVITIES",
            )

        val fieldPath = "data.revenue.nonAlignedActivities.value.0.share.absoluteShare.amount"
        val expectedAlias = "REV_NON_ALIGNED_ACTIVITIES_0_ABS"

        val result = DataExportUtils.stripFieldNames(fieldPath, aliasExportMap)
        Assertions.assertEquals(expectedAlias, result)
    }

    @Test
    fun `stripFieldNames with no matching alias returns full path`() {
        val aliasExportMap = mapOf<String, String?>()

        val fieldPath = "data.unspecified.field.value"
        val expectedAlias = fieldPath

        val result = DataExportUtils.stripFieldNames(fieldPath, aliasExportMap)
        Assertions.assertEquals(expectedAlias, result)
    }

    @Test
    fun `stripFieldNames handling with suffix abs transformation`() {
        val aliasExportMap =
            mapOf(
                "finance.alignedActivities" to "FIN_ALIGNED_ACTIVITIES",
            )

        val fieldPath = "data.finance.alignedActivities.value.absoluteShare"
        val expectedAlias = "FIN_ALIGNED_ACTIVITIES_ABS"

        val result = DataExportUtils.stripFieldNames(fieldPath, aliasExportMap)
        Assertions.assertEquals(expectedAlias, result)
    }

    @Test
    fun `stripFieldNames handles unknown suffixes and mapping`() {
        val aliasExportMap =
            mapOf(
                "environmentalImpact" to "ENV_IMPACT",
            )

        val fieldPath = "data.environmentalImpact.value.unknownSuffix"
        val expectedAlias = "ENV_IMPACT_UNKNOWNSUFFIX"

        val result = DataExportUtils.stripFieldNames(fieldPath, aliasExportMap)
        Assertions.assertEquals(expectedAlias, result)
    }

    private fun assuranceNode(withComment: Boolean) =
        defaultObjectMapper.readTree(
            """{"assurance":{"value":"Limited","provider":"Other Auditors"${if (withComment) ""","comment":"Some comment"""" else ""}}}""",
        )

    @Test
    fun `values only export of assurance keeps value and provider but drops comment`() {
        val (data, fields) = DataExportUtils.getCsvDataAndNonEmptyFields(listOf(assuranceNode(true)), true)
        Assertions.assertEquals(
            setOf("assurance.value", "assurance.provider", DataExportUtils.PROXIED_FIELD),
            fields,
        )
        Assertions.assertEquals("Other Auditors", data.first()["assurance.provider"])
    }

    @Test
    fun `full export of assurance has comment column only if comment is set`() {
        val (_, withComment) = DataExportUtils.getCsvDataAndNonEmptyFields(listOf(assuranceNode(true)), false)
        val (_, withoutComment) = DataExportUtils.getCsvDataAndNonEmptyFields(listOf(assuranceNode(false)), false)
        val baseFields = setOf("assurance.value", "assurance.provider", DataExportUtils.PROXIED_FIELD)
        Assertions.assertEquals(baseFields + "assurance.comment", withComment)
        Assertions.assertEquals(baseFields, withoutComment)
    }
}

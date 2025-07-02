package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.utils.DataPointUtils
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class DataExportUtilsTest {
    private val mockDataPointUtils = mock<DataPointUtils>()
    private val mockReferencedReportsUtils = mock<ReferencedReportsUtilities>()
    private val dataExportUtils = DataExportUtils(mockDataPointUtils, mockReferencedReportsUtils)

    @Test
    fun `stripFieldNames with known alias and suffixes`() {
        val aliasExportMap =
            mapOf(
                "revenue.nonAlignedActivities" to "REV_NON_ALIGNED_ACTIVITIES",
            )

        val fieldPath = "data.revenue.nonAlignedActivities.value.0.share.absoluteShare.amount"
        val expectedAlias = "REV_NON_ALIGNED_ACTIVITIES_0_ABS"

        val result = dataExportUtils.stripFieldNames(fieldPath, aliasExportMap)
        Assertions.assertEquals(expectedAlias, result)
    }

    @Test
    fun `stripFieldNames with no matching alias returns full path`() {
        val aliasExportMap = mapOf<String, String?>()

        val fieldPath = "data.unspecified.field.value"
        val expectedAlias = fieldPath

        val result = dataExportUtils.stripFieldNames(fieldPath, aliasExportMap)
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

        val result = dataExportUtils.stripFieldNames(fieldPath, aliasExportMap)
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

        val result = dataExportUtils.stripFieldNames(fieldPath, aliasExportMap)
        Assertions.assertEquals(expectedAlias, result)
    }
}

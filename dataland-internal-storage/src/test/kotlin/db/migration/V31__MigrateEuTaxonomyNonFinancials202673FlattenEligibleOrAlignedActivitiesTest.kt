package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.JsonUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V31__MigrateEuTaxonomyNonFinancials202673FlattenEligibleOrAlignedActivitiesTest {
    private val dataType = "eutaxonomy-non-financials-2026-73"
    private val migration = V31__MigrateEuTaxonomyNonFinancials202673FlattenEligibleOrAlignedActivities()

    @Test
    fun `double-nested eligibleOrAlignedActivities in revenue, capex and opex is flattened correctly`() {
        val mockDataId = "mock-data-id"
        val original =
            DataTableEntity.fromJsonObject(
                mockDataId,
                dataType,
                JsonUtils.readJsonFromResourcesFile("V31/originalDatabaseEntry.json"),
            )
        val expected =
            DataTableEntity.fromJsonObject(
                mockDataId,
                dataType,
                JsonUtils.readJsonFromResourcesFile("V31/expectedDatabaseEntry.json"),
            )
        migration.flattenEligibleOrAlignedActivities(original)
        assertEquals(expected, original)
    }

    @Test
    fun `dataset without eligibleOrAlignedActivities is left unchanged`() {
        val mockDataId = "mock-data-id-null"
        val original =
            DataTableEntity.fromJsonObject(
                mockDataId,
                dataType,
                JsonUtils.readJsonFromResourcesFile("V31/originalDatabaseEntryWithNullActivities.json"),
            )
        val expected =
            DataTableEntity.fromJsonObject(
                mockDataId,
                dataType,
                JsonUtils.readJsonFromResourcesFile("V31/originalDatabaseEntryWithNullActivities.json"),
            )
        migration.flattenEligibleOrAlignedActivities(original)
        assertEquals(expected, original)
    }
}

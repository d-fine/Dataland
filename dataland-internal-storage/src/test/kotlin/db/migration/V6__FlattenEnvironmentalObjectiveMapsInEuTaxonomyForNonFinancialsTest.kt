package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.JsonUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V6__FlattenEnvironmentalObjectiveMapsInEuTaxonomyForNonFinancialsTest {
    private val dataType = "eutaxonomy-non-financials"

    @Test
    fun `test that eu taxonomy for non financials migrate to flattened substantialCriteria and dnsh maps`() {
        val mockDataId = "mock-data-id"
        val originalDatabaseEntry =
            DataTableEntity.fromJsonObject(
                mockDataId,
                dataType,
                JsonUtils.readJsonFromResourcesFile("V6/originalDatabaseEntry.json"),
            )
        val expectedTransformedDatabaseEntry =
            DataTableEntity.fromJsonObject(
                mockDataId,
                dataType,
                JsonUtils.readJsonFromResourcesFile("V6/expectedTransformedDatabaseEntry.json"),
            )
        val migration = V6__FlattenEnvironmentalObjectiveMapsInEuTaxonomyForNonFinancials()
        migration.flattenEnvironmentalObjectiveMaps(originalDatabaseEntry)
        Assertions.assertEquals(originalDatabaseEntry, expectedTransformedDatabaseEntry)
    }
}

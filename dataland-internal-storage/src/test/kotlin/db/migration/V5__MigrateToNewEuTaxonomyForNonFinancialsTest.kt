package db.migration

import db.migration.utils.buildDatabaseEntry
import db.migration.utils.mockAndWhenConfigurationForFrameworkMigration
import db.migration.utils.readJsonFromResourcesFile
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class V5__MigrateToNewEuTaxonomyForNonFinancialsTest {
    val newEuTaxonomyForNonFinancials = "new-eutaxonomy-non-financials"
    val euTaxonomyForNonFinancials = "eutaxonomy-non-financials"

    @Test
    fun `test that eu taxonomy for non financials migration script works as expected for migrating new data`() {
        val mockContext = Mockito.mock(Context::class.java)
        mockAndWhenConfigurationForFrameworkMigration(
            mockContext,
            buildDatabaseEntry(
                JSONObject(
                    "{" +
                        "\"something\": \"something\"" +
                        "}",
                ),
                newEuTaxonomyForNonFinancials,
            ),
            buildDatabaseEntry(
                JSONObject(
                    "{" +
                        "\"something\": \"something\"" +
                        "}",
                ),
                euTaxonomyForNonFinancials,
            ),
        )
        val migration = V5__MigrateToNewEuTaxonomyForNonFinancials()
        migration.migrate(mockContext)
    }

    @Test
    fun `test that eu taxonomy for non financials migration script works as expected for migrating old data`() {
        val mockContext = Mockito.mock(Context::class.java)
        mockAndWhenConfigurationForFrameworkMigration(
            mockContext,
            oldOriginalDatabaseEntry,
            oldExpectedTransformedDataBaseEntry,
        )
        val migration = V5__MigrateToNewEuTaxonomyForNonFinancials()
        migration.migrate(mockContext)
    }

    val oldOriginalDatabaseEntry = buildDatabaseEntry(
        readJsonFromResourcesFile("V5", "oldOriginalDatabaseEntry.json"),
        euTaxonomyForNonFinancials,
    )

    val oldExpectedTransformedDataBaseEntry = buildDatabaseEntry(
        readJsonFromResourcesFile("V5", "oldExpectedTransformedDataBaseEntry.json"),
        euTaxonomyForNonFinancials,
    )
}

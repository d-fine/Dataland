package db.migration

import db.migration.utils.DataTableEntity
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V2__MigrateEuTaxonomyNonFinancialsWithAbsoluteValuesTest {
    @Test
    fun `test that migration writes the expected results into the datatable`() {
        val origDatabaseEntry = buildOriginalDatabaseEntry()
        val expectedDataBaseEntry = buildExpectedTransformedDatabaseEntry()
        val migration = V2__MigrateEuTaxonomyNonFinancialsWithAbsoluteValues()
        migration.migrateEuTaxonomyNonFinancialsData(origDatabaseEntry)

        Assertions.assertEquals(expectedDataBaseEntry, origDatabaseEntry)
    }

    private val affectedFields = listOf("capex", "opex", "revenue")
    private val unaffectedFields = listOf("something")

    private fun buildOriginalDatabaseEntry(): DataTableEntity {
        val dataset = JSONObject()
        (affectedFields + unaffectedFields).forEach {
            dataset.put(it, buildOldDetailsPerCashFlowType())
        }
        return DataTableEntity.fromJsonObject("mock-data-id", "eutaxonomy-non-financials", dataset)
    }

    private fun buildExpectedTransformedDatabaseEntry(): DataTableEntity {
        val dataset = JSONObject()
        (affectedFields).forEach {
            dataset.put(it, buildNewDetailsPerCashFlowType())
        }
        (unaffectedFields).forEach {
            dataset.put(it, buildOldDetailsPerCashFlowType())
        }
        return DataTableEntity.fromJsonObject("mock-data-id", "eutaxonomy-non-financials", dataset)
    }

    private val unaffectedDetail = "totalAmount"
    private val dummyDataPoint =
        JSONObject(
            "{\"value\":0.1,\"dataSource\":{\"report\":\"some report\"},\"quality\":\"Estimated\"}",
        )
    private val dummyDataPointAbsoluteAndPercentage =
        JSONObject(
            "{\"valueAsPercentage\":0.1,\"dataSource\":{\"report\":\"some report\"},\"quality\":\"Estimated\"}",
        )

    private fun buildOldDetailsPerCashFlowType(): JSONObject {
        val cashFlowObject = JSONObject()
        listOf(unaffectedDetail, "alignedPercentage").forEach {
            cashFlowObject.put(it, dummyDataPoint)
        }
        cashFlowObject.put("eligiblePercentage", JSONObject.NULL)
        return cashFlowObject
    }

    private fun buildNewDetailsPerCashFlowType(): JSONObject {
        val cashFlowObject = JSONObject()
        cashFlowObject.put("alignedData", dummyDataPointAbsoluteAndPercentage)
        cashFlowObject.put("eligibleData", JSONObject.NULL)
        cashFlowObject.put(unaffectedDetail, dummyDataPoint)
        return cashFlowObject
    }
}

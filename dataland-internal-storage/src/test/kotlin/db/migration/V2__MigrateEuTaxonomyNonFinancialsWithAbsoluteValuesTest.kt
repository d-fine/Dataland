package db.migration

import db.migration.utils.buildDatabaseEntry
import db.migration.utils.mockAndWhenConfigurationForFrameworkMigration
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class V2__MigrateEuTaxonomyNonFinancialsWithAbsoluteValuesTest {

    @Test
    fun `test that migration writes the expected results into the datatable`() {
        val mockContext = mock(Context::class.java)
        mockAndWhenConfigurationForFrameworkMigration(
            mockContext,
            buildOriginalDatabaseEntry(),
            buildExpectedTransformedDatabaseEntry()
        )
        val migration = V2__MigrateEuTaxonomyNonFinancialsWithAbsoluteValues()
        migration.migrate(mockContext)
    }

    private val affectedFields = listOf("capex", "opex", "revenue")
    private val unaffectedFields = listOf("something")
    private fun buildOriginalDatabaseEntry(): String {
        val dataset = JSONObject()
        (affectedFields + unaffectedFields).forEach {
            dataset.put(it, buildOldDetailsPerCashFlowType())
        }
        return buildDatabaseEntry(dataset, DataTypeEnum.eutaxonomyMinusNonMinusFinancials)
    }
    private fun buildExpectedTransformedDatabaseEntry(): String {
        val dataset = JSONObject()
        (affectedFields).forEach {
            dataset.put(it, buildNewDetailsPerCashFlowType())
        }
        (unaffectedFields).forEach {
            dataset.put(it, buildOldDetailsPerCashFlowType())
        }
        return buildDatabaseEntry(dataset, DataTypeEnum.eutaxonomyMinusNonMinusFinancials)
    }

    private val unaffectedDetail = "totalAmount"
    private val dummyDataPoint = JSONObject(
        "{\"value\":0.1,\"dataSource\":{\"report\":\"some report\"},\"quality\":\"Estimated\"}",
    )
    private val dummyDataPointAbsoluteAndPercentage = JSONObject(
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

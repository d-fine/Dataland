package db.migration

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script updates the EU Taxonomy for non-financials data model switching
 * from a field called value for percentages only to a structure holding the absolute value of a cash flow type as well
 */
@Suppress("ClassNaming")
class V2__MigrateEuTaxonomyNonFinancialsWithAbsoluteValues : BaseJavaMigration() {
    private data class DataTableEntity(
        val dataId: String,
        val companyAssociatedData: JSONObject,
    ) {
        fun getWriteQuery(): String = "UPDATE data_items " +
            "SET data = '${ObjectMapper().writeValueAsString(companyAssociatedData.toString())}' " +
            "WHERE data_id = '$dataId'"
    }

    private val cashFlowTypes = listOf("capex", "opex", "revenue")
    private val fieldsToMigrate = mapOf("alignedPercentage" to "alignedData", "eligiblePercentage" to "eligibleData")

    override fun migrate(context: Context?) {
        val objectMapper = ObjectMapper()
        val getQueryResultSet = context!!.connection.createStatement().executeQuery(
            "SELECT * from data_items " +
                "WHERE data LIKE '%\\\\\\\"dataType\\\\\\\":\\\\\\\"eutaxonomy-non-financials\\\\\\\"%'",
        )
        val companyAssociatedDataSets = mutableListOf<DataTableEntity>()
        while (getQueryResultSet.next()) {
            companyAssociatedDataSets.add(
                DataTableEntity(
                    getQueryResultSet.getString("data_id"),
                    JSONObject(
                        objectMapper.readValue(
                            getQueryResultSet.getString("data"), String::class.java,
                        ),
                    ),
                ),
            )
        }
        companyAssociatedDataSets.filter {
            it.companyAssociatedData.getString("dataType") == DataTypeEnum.eutaxonomyMinusNonMinusFinancials.value
        }
        companyAssociatedDataSets.forEach {
            it.companyAssociatedData.put("data", migrateDataset(it.companyAssociatedData.getString("data")))
            context.connection.createStatement().execute(it.getWriteQuery())
        }
    }

    private fun migrateDataset(datasetString: String): String {
        val dataset = JSONObject(datasetString)
        cashFlowTypes.forEach { cashflowType ->
            val cashFlow = (dataset.opt(cashflowType) ?: return@forEach) as JSONObject
            fieldsToMigrate.keys.forEach { fieldToMigrate ->
                val dataToMigrate = getJsonObjectOrActualNull(cashFlow, fieldToMigrate) ?: return@forEach
                dataToMigrate.put("valueAsPercentage", dataToMigrate.opt("value"))
                dataToMigrate.remove("value")
                cashFlow.put(fieldsToMigrate.getValue(fieldToMigrate), dataToMigrate)
                cashFlow.remove(fieldToMigrate)
            }
        }
        return dataset.toString()
    }

    private fun getJsonObjectOrActualNull(baseObject: JSONObject, fieldName: String): JSONObject? {
        return (
            baseObject.opt(fieldName)?.let {
                if (it == JSONObject.NULL) null else it
            } ?: return null
            ) as JSONObject
    }
}

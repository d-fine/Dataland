package db.migration

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import java.math.BigDecimal

class V2__MigrateEuTaxonomyNonFinancialsWithAbsoluteValues : BaseJavaMigration() {
    private val cashFlowTypes = listOf("capex", "opex", "revenue")
    private val fieldsToMigrate = mapOf("alignedPercentage" to "alignedData", "eligiblePercentage" to "eligibleData")

    override fun migrate(context: Context?) {
        val objectMapper = ObjectMapper()
        val getQueryResultSet = context!!.connection.createStatement().executeQuery("SELECT data from data_items WHERE data LIKE '%\\\\\\\"dataType\\\\\\\":\\\\\\\"eutaxonomy-non-financials\\\\\\\"%'")
        val companyAssociatedDataSets = mutableListOf<JSONObject>()
        while (getQueryResultSet.next()) {
            companyAssociatedDataSets.add(
                JSONObject(
                    objectMapper.readValue(
                        getQueryResultSet.getString("data"), String::class.java,
                    ),
                ),
            )
        }
        companyAssociatedDataSets.filter { it.getString("dataType") == DataTypeEnum.eutaxonomyMinusNonMinusFinancials.value }
        companyAssociatedDataSets.forEach {
            println("Migrating dataset")
            migrateDataset(it.getString("data"))
        }
    }

    private fun migrateDataset(datasetString: String): String {
        val dataset = JSONObject(datasetString)
        println("OLD $dataset")
        cashFlowTypes.forEach { cashflowType ->
            val cashFlow = (dataset.opt(cashflowType) ?: return@forEach) as JSONObject
            fieldsToMigrate.keys.forEach { fieldToMigrate ->
                val dataToMigrate = (cashFlow.opt(fieldToMigrate)?.let { if (it == JSONObject.NULL) null else it } ?: return@forEach) as JSONObject
                dataToMigrate.put("valueAsPercentage", dataToMigrate.opt("value"))
                dataToMigrate.remove("value")
                cashFlow.put(fieldsToMigrate[fieldToMigrate]!!, dataToMigrate)
                cashFlow.remove(fieldToMigrate)
            }
        }
        println("NEW $dataset")
        return dataset.toString()
    }
}

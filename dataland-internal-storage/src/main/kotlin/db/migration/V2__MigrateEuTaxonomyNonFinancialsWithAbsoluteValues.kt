package db.migration

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import java.math.BigDecimal

class V2__MigrateEuTaxonomyNonFinancialsWithAbsoluteValues : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val objectMapper = ObjectMapper()
        val getQueryResultSet = context!!.connection.createStatement().executeQuery("SELECT data from data_items")//" WHERE data ILIKE '\\\"dataType\\\":\\\"eutaxonomy-non-financials\\\"'")
        val companyAssociatedDataSets = mutableListOf<JSONObject>()
        while(getQueryResultSet.next()) {
            companyAssociatedDataSets.add(JSONObject(
                objectMapper.readValue(
                    getQueryResultSet.getString("data"), String::class.java
                )
            ))
        }
        companyAssociatedDataSets.filter { it.getString("dataType") == DataTypeEnum.eutaxonomyMinusNonMinusFinancials.value }
        val cashflowTypes = listOf("capex", "opex", "revenue")
        val fieldsToMigrate = mapOf("alignedPercentage" to "alignedData", "eligiblePercentage" to "eligibleData")
        companyAssociatedDataSets.forEach {
            println("Migrating dataset")
            val data = JSONObject(it.getString("data"))
            cashflowTypes.forEach { cashflowType ->
                val cashFlow = (data.opt(cashflowType) ?: return@forEach) as JSONObject
                fieldsToMigrate.keys.forEach { fieldToMigrate ->
                    val dataToMigrate = (cashFlow.opt(fieldToMigrate) ?: return@forEach) as JSONObject
                    dataToMigrate.append("valueAsPercentage", (cashFlow.opt("value") ?: return@forEach) as BigDecimal)
                    dataToMigrate.remove("value")
                    cashFlow.remove(fieldToMigrate)
                    cashFlow.append(fieldsToMigrate[fieldToMigrate]!!, dataToMigrate)
                }
            }
        }
    }
}

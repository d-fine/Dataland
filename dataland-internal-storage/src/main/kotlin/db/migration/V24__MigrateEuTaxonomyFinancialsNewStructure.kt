package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * This migration script updates the existing EU taxonomy non-financials datasets and migrates all
 * existing BaseDataPoints to ExtendedDataPoints.
 */
class V24__MigrateEuTaxonomyFinancialsNewStructure : BaseJavaMigration() {

    private val fieldsWhichBecomeExtendedDataPoints = listOf(
        "fiscalYearDeviation",
        "fiscalYearEnd",
        "scopeOfEntities",
        "numberOfEmployees",
        "nfrdMandatory",
    )

    private val fieldsWhichMoveFromTToGeneral = listOf(
        "fiscalYearDeviation",
        "fiscalYearEnd",
        "referencedReports",
        "scopeOfEntities",
        "numberOfEmployees",
        "nfrdMandatory",
        "assurance",
    )

    private val fieldsToRemove = listOf(
        "euTaxonomyActivityLevelReporting",
    )

    private val fieldsWhichMoveToCreditInstitutionGeneral = listOf(
        "interbankLoansInPercent",
        "tradingPortfolioInPercent",
        "tradingPortfolioAndInterbankLoansInPercent",
    )

    /**
     * Move key: object to key: { "value": object }
     * @param jsonObject JSON object
     * @param key key corresponding to the JSON object
     */
    private fun updateObjectBehindKeyInJsonObject(jsonObject: JSONObject, key: String) {
        val newValue = JSONObject()
        val oldValue = jsonObject[key]
        if (oldValue != JSONObject.NULL) {
            newValue.put("value", oldValue)
            jsonObject.put(key, newValue)
        }
    }

    /**
     * Check if the keys of a JSON object are relevant fields and, if so, update the object behind these keys.
     * @param jsonObject JSON object
     */
    private fun checkForRelevantFieldsInJsonObjectKeys(jsonObject: JSONObject) {
        fieldsToRemove.forEach {
            jsonObject.remove(it)
        }
        jsonObject.keys().forEach {
            if (it in fieldsWhichBecomeExtendedDataPoints) {
                updateObjectBehindKeyInJsonObject(jsonObject, it)
            }
            checkRecursivelyForRelevantFieldKeysInJsonObject(jsonObject, it)
        }
    }

    /**
     * Check recursively for relevant field keys in a JSON array.
     * @param jsonArray JSON array
     */
    private fun checkRecursivelyForRelevantFieldKeysInJsonArray(jsonArray: JSONArray) {
        jsonArray.forEach {
            if (it != null && it is JSONObject) {
                checkForRelevantFieldsInJsonObjectKeys(it)
            }
        }
    }

    /**
     * Check recursively for relevant field keys in a JSON object.
     * @param jsonObject JSON object
     * @param key key corresponding to the JSON object
     */
    private fun checkRecursivelyForRelevantFieldKeysInJsonObject(jsonObject: JSONObject, key: String) {
        val obj = jsonObject.getOrJavaNull(key)
        if (obj !== null && obj is JSONObject) {
            checkForRelevantFieldsInJsonObjectKeys(obj)
        } else if (obj != null && obj is JSONArray) {
            checkRecursivelyForRelevantFieldKeysInJsonArray(obj)
        } else {
            // Do nothing as no more migration is required
        }
    }

    private fun migrateExtendedDocumentSupport(dataTableEntity: DataTableEntity) {
        val dataTableObject = dataTableEntity.dataJsonObject
        checkForRelevantFieldsInJsonObjectKeys(dataTableObject)
        dataTableEntity.companyAssociatedData.put("data", dataTableObject.toString())
    }

    private fun migrateReportingPeriod(dataTableEntity: DataTableEntity) {
        val jsonObject = dataTableEntity.dataJsonObject
        val tObject = jsonObject["t"] as JSONObject
        val referencedReportsObject = JSONObject()
        referencedReportsObject.put("reportingPeriod", jsonObject["reportingPeriod"])
        tObject.put("general", referencedReportsObject)
        jsonObject.remove("reportingPeriod")
        dataTableEntity.companyAssociatedData.put("data", jsonObject.toString())
    }

    private fun migrateFromTToGeneral(dataTableEntity: DataTableEntity) {
        val jsonObject = dataTableEntity.dataJsonObject
        val tObject = jsonObject["t"] as JSONObject
        val generalObject = tObject["general"] as JSONObject
        fieldsWhichMoveFromTToGeneral.forEach {
            generalObject.put(it, tObject[it])
            tObject.remove(it)
        }
        dataTableEntity.companyAssociatedData.put("data", jsonObject.toString())
    }

    private fun migrateToCreditInstitutionGeneral(dataTableEntity: DataTableEntity) {
        val jsonObject = dataTableEntity.dataJsonObject
        val tObject = jsonObject["t"] as JSONObject
        val creditInstitutionKpisObject = tObject["creditInstitutionKpis"] as JSONObject
        val creditInstitutionGeneralObject = JSONObject()
        val creditInstitutionObject = JSONObject()
        fieldsWhichMoveToCreditInstitutionGeneral.forEach {
            creditInstitutionGeneralObject.put(it, creditInstitutionKpisObject[it])
            creditInstitutionKpisObject.remove(it)
        }
        creditInstitutionObject.put("general", creditInstitutionGeneralObject)
        tObject.put("creditInstitution", creditInstitutionObject)
        dataTableEntity.companyAssociatedData.put("data", jsonObject.toString())
    }

    /**
     * Migrate a DataTableEntity so that the relevant fields are turned into ExtendedDataPoints.
     * @param dataTableEntity DataTableEntity
     */
    fun migrateEuTaxonomyFinancialsData(dataTableEntity: DataTableEntity) {
        migrateExtendedDocumentSupport(dataTableEntity)
        migrateReportingPeriod(dataTableEntity)
        migrateFromTToGeneral(dataTableEntity)
        migrateToCreditInstitutionGeneral(dataTableEntity)
    }

    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context = context,
            dataType = "eutaxonomy-financials",
            migrate = this::migrateEuTaxonomyFinancialsData,
        )
    }
}

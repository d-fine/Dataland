package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
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

    // add JavaDoc
    private fun migrateExtendedDocumentSupport(dataTableEntity: DataTableEntity) {
        val jsonObject = dataTableEntity.dataJsonObject
        val tObject = jsonObject["t"] as JSONObject
        fieldsWhichBecomeExtendedDataPoints.forEach {
            val newValue = JSONObject()
            val oldValue = tObject[it]
            if (oldValue != JSONObject.NULL) {
                newValue.put("value", oldValue)
                tObject.put(it, newValue)
            }
        }
        // put the removal in an extra function
        fieldsToRemove.forEach {
            tObject.remove(it)
        }
        dataTableEntity.companyAssociatedData.put("data", jsonObject.toString())
    }

    // add JavaDoc
    private fun migrateReportingPeriod(dataTableEntity: DataTableEntity) {
        val jsonObject = dataTableEntity.dataJsonObject
        val tObject = jsonObject["t"] as JSONObject
        val generalObject = JSONObject()
        generalObject.put("reportingPeriod", jsonObject["reportingPeriod"])
        tObject.put("general", generalObject)
        jsonObject.remove("reportingPeriod")
        dataTableEntity.companyAssociatedData.put("data", jsonObject.toString())
    }

    // add JavaDoc
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

    // add JavaDoc
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

    // add JavaDoc
    private fun migrateInsuranceReinsurance(dataTableEntity: DataTableEntity) {
        val jsonObject = dataTableEntity.dataJsonObject
        val tObject = jsonObject["t"] as JSONObject
        val insuranceKpisObject = tObject["insuranceKpis"] as JSONObject
        val insuranceReinsuranceGeneralObject = JSONObject()
        val insuranceReinsuranceObject = JSONObject()
        insuranceReinsuranceGeneralObject
            .put(
                "taxonomyEligibleNonLifeInsuranceEconomicActivities",
                insuranceKpisObject["taxonomyEligibleNonLifeInsuranceActivitiesInPercent"],
            )
        insuranceReinsuranceObject.put("general", insuranceReinsuranceGeneralObject)
        tObject.put("insuranceReinsurance", insuranceReinsuranceObject)
        tObject.remove("insuranceKpis")
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
        migrateInsuranceReinsurance(dataTableEntity)
    }

    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context = context,
            dataType = "eutaxonomy-financials",
            migrate = this::migrateEuTaxonomyFinancialsData,
        )
    }
}

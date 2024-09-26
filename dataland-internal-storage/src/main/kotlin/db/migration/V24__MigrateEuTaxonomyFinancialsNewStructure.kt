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

    private val fieldsWhichMoveToGeneral = listOf(
        "reportingPeriod",
        "fiscalYearDeviation",
        "fiscalYearEnd",
        "referencedReports",
        "scopeOfEntities",
        "numberOfEmployees",
        "nfrdMandatory",
        "assurance",
    )

    private val fieldsWhichMoveToCreditInstitutionGeneral = listOf(
        "interbankLoansInPercent",
        "tradingPortfolioInPercent",
        "tradingPortfolioAndInterbankLoansInPercent",
    )

    private val fieldsWhichAreRemoved = listOf(
        "euTaxonomyActivityLevelReporting",
        "financialServicesTypes",
        "eligibilityKpis",
        "creditInstitutionKpis",
        "investmentFirmKpis",
        "insuranceKpis",
    )

    /**
     * Give fields ExtendedDocumentSupport
     * @param tObject JSONObject, represents the data table
     */
    private fun migrateExtendedDocumentSupport(tObject: JSONObject) {
        fieldsWhichBecomeExtendedDataPoints.forEach {
            val newValue = JSONObject()
            val oldValue = tObject[it]
            if (oldValue != JSONObject.NULL) {
                newValue.put("value", oldValue)
                tObject.put(it, newValue)
            }
        }
    }

    /**
     * Migrate data to general
     * @param dataObject JSONObject, represents the full data
     * @param tObject JSONObject, represents the data table
     */
    private fun migrateToGeneral(dataObject: JSONObject, tObject: JSONObject) {
        val generalObject = JSONObject()
        fieldsWhichMoveToGeneral.forEach {
            if (it == "reportingPeriod") {
                generalObject.put(it, dataObject[it])
                dataObject.remove(it)
            } else {
                generalObject.put(it, tObject[it])
                tObject.remove(it)
            }
        }
        tObject.put("general", generalObject)
    }

    /**
     * Migrate Credit Institution data to creditInstitution/general
     * @param tObject JSONObject, represents the data table
     */
    private fun migrateToCreditInstitutionGeneral(tObject: JSONObject) {
        if (tObject["creditInstitutionKpis"] != JSONObject.NULL) {
            val creditInstitutionKpisObject = tObject["creditInstitutionKpis"] as JSONObject
            val creditInstitutionGeneralObject = JSONObject()
            val creditInstitutionObject = JSONObject()
            fieldsWhichMoveToCreditInstitutionGeneral.forEach {
                creditInstitutionGeneralObject.put(it, creditInstitutionKpisObject[it])
                creditInstitutionKpisObject.remove(it)
            }
            creditInstitutionObject.put("general", creditInstitutionGeneralObject)
            tObject.put("creditInstitution", creditInstitutionObject)
        }
    }

    /**
     * Migrate Insurance/Reinsurance data
     * @param tObject JSONObject, represents the data table
     */
    private fun migrateInsuranceReinsurance(tObject: JSONObject) {
        if (tObject["insuranceKpis"] != JSONObject.NULL) {
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
        }
    }

    /**
     * Remove data which no longer fit the new data model.
     * @param tObject JSONObject, represents the data table
     */
    private fun removeDeprecatedData(tObject: JSONObject) {
        // this function must be modified later (at least to account for the green asset ratio)
        fieldsWhichAreRemoved.forEach {
            tObject.remove(it)
        }
    }

    /**
     * Migrate a DataTableEntity for the EuTaxonomyFinancials framework.
     * @param dataTableEntity DataTableEntity
     */
    fun migrateEuTaxonomyFinancialsData(dataTableEntity: DataTableEntity) {
        val dataObject = dataTableEntity.dataJsonObject
        val tObject = dataObject["t"] as JSONObject
        migrateExtendedDocumentSupport(tObject)
        migrateToGeneral(dataObject, tObject)
        migrateToCreditInstitutionGeneral(tObject)
        migrateInsuranceReinsurance(tObject)
        removeDeprecatedData(tObject)
        dataTableEntity.companyAssociatedData.put("data", dataObject.toString())
    }

    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context = context,
            dataType = "eutaxonomy-financials",
            migrate = this::migrateEuTaxonomyFinancialsData,
        )
    }
}

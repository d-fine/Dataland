package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.slf4j.LoggerFactory

/**
 * This migration script updates the existing EU taxonomy non-financials datasets and migrates all
 * existing BaseDataPoints to ExtendedDataPoints.
 */
class V24__MigrateEuTaxonomyFinancialsNewStructure : BaseJavaMigration() {

    private val logger = LoggerFactory.getLogger("Migration V23")

    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context = context,
            dataType = "eutaxonomy-financials",
            migrate = this::migrateEuTaxonomyFinancialsData,
        )
    }

    private val migrations = listOf(
        // general
        Triple(listOf("fiscalYearDeviation"), listOf("general", "general", "fiscalYearDeviation"), this::extend),
        Triple(listOf("fiscalYearEnd"), listOf("general", "general", "fiscalYearEnd"), this::extend),
        Triple(listOf("referencedReports"), listOf("general", "general", "referencedReports"), this::identity),
        Triple(listOf("scopeOfEntities"), listOf("general", "general", "areAllGroupEntitiesCovered"), this::extend),
        Triple(listOf("numberOfEmployees"), listOf("general", "general", "numberOfEmployees"), this::extend),
        Triple(listOf("nfrdMandatory"), listOf("general", "general", "isNfrdMandatory"), this::extend),
        Triple(listOf("assurance"), listOf("general", "general", "assurance"), this::identity),
        // credit institutions
        Triple(
            listOf("eligibilityKpis", "CreditInstitution", "taxonomyEligibleActivityInPercent"),
            listOf(
                "creditInstitution",
                "turnoverBasedGreenAssetRatioStock",
                "substantialContributionToAnyOfTheSixEnvironmentalObjectivesInPercentEligible",
            ),
            this::identity,
        ),
        // asset management: no migration possible
        // insurances and re-insurances
        Triple(
            listOf("eligibilityKpis", "InsuranceOrReinsurance", "taxonomyNonEligibleActivityInPercent"),
            listOf(
                "insuranceReinsurance",
                "underwritingKpi",
                "proportionOfAbsolutePremiumsOfTaxonomyNonEligibleActivities",
            ),
            this::identity,
        ),
        // investment firms: no migration possible
    )

    /**
     * Migrate a DataTableEntity to the structure of the new EU-Taxonomy Financials Framework.
     * @param dataTableEntity DataTableEntity
     */
    fun migrateEuTaxonomyFinancialsData(dataTableEntity: DataTableEntity) {
        val dataObject = dataTableEntity.dataJsonObject
        val newObject = JSONObject()

        for ((from, to, transform) in migrations) {
            getJSONObjectByPath(dataObject, from)?.let {
                insertIntoJSONObjectAtPath(newObject, to, transform(it))
            }
        }

        dataTableEntity.companyAssociatedData.put("data", newObject.toString())
    }

    private fun identity(some: Any): Any = some

    private fun extend(some: Any): Any = JSONObject(mapOf("value" to some))

    private fun getJSONObjectByPath(json: JSONObject, path: List<String>): Any? {
        var currentObject: Any = json

        for (key in path) {
            require(currentObject is JSONObject)
            currentObject = currentObject.opt(key) ?: return null
        }
        return currentObject
    }

    private fun insertIntoJSONObjectAtPath(target: JSONObject, path: List<String>, newObject: Any) {
        require(path.isNotEmpty()) { "Path cannot be empty." }

        var current = target
        for (key in path.dropLast(1)) {
            when (val value = current.opt(key)) {
                is JSONObject -> {
                    current = value
                }
                JSONObject.NULL, null -> {
                    val newChild = JSONObject()
                    current.put(key, newChild)
                    current = newChild
                }
                else -> {
                    logger.warn("Trying to insert into path $path but $key exists and is not a JSONObject.")
                }
            }
        }

        current.put(path.last(), newObject)
    }
}

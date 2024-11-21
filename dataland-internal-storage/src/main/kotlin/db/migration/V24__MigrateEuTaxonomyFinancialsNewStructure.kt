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
@Suppress("ClassName")
class V24__MigrateEuTaxonomyFinancialsNewStructure : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger("Migration V23")

    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context = context,
            dataType = "eutaxonomy-financials",
            migrate = this::migrateEuTaxonomyFinancialsData,
        )
    }

    private val migrations =
        listOf(
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
        val oldData = dataTableEntity.dataJsonObject
        val newData = JSONObject()

        for ((sourcePath, destinationPath, transform) in migrations) {
            getJSONObjectByPath(oldData, sourcePath)?.let {
                insertIntoJSONObjectAtPath(newData, destinationPath, transform(it))
            }
        }

        dataTableEntity.companyAssociatedData.put("data", newData.toString())
    }

    private fun identity(value: Any): Any = value

    private fun extend(value: Any): Any = JSONObject(mapOf("value" to value))

    /**
     * Retrieves a value from the given JSONObject following the specified path.
     * @param sourceObject The JSONObject to retrieve the value from.
     * @param path A list of keys representing the path to the desired value.
     * @return The value found at the specified path, or `null` if no object at the path exists.
     * @throws IllegalArgumentException If an intermediate element in the path is not a JSONObject.
     */
    private fun getJSONObjectByPath(
        sourceObject: JSONObject,
        path: List<String>,
    ): Any? {
        var currentObject: Any = sourceObject

        for (key in path) {
            require(currentObject is JSONObject)
            currentObject = currentObject.opt(key) ?: return null
        }
        return currentObject
    }

    /**
     * Inserts a value into the specified JSONObject at the given path.
     * Creates intermediate JSONObjects on the path if they do not exist.
     * @param destinationObject The JSONObject to modify.
     * @param path A list of keys representing the path where the value will be inserted.
     * @param objectToInsert The value to insert at the path.
     * @throws IllegalArgumentException If the provided path is empty.
     */
    private fun insertIntoJSONObjectAtPath(
        destinationObject: JSONObject,
        path: List<String>,
        objectToInsert: Any,
    ) {
        require(path.isNotEmpty()) { "Path cannot be empty." }

        var current = destinationObject
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

        current.put(path.last(), objectToInsert)
    }
}

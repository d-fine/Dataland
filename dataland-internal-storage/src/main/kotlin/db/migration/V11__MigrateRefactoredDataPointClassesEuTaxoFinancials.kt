package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.MigrationHelper
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * Performs the migration of the refactored data point classes for the eu taxonomy non fiancials framework
 */
@Suppress("ClassName")
class V11__MigrateRefactoredDataPointClassesEuTaxoFinancials : BaseJavaMigration() {
    /**
     * Performs the migration of the refactored data point classes for eu taxonomy financials data
     */
    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "eutaxonomy-financials",
            this::migrateRefactoredDataPointClasses,
        )
    }

    private val oldToNewFieldNamesForAssurance =
        mapOf(
            "assurance" to "value",
        )

    private val oldToNewFieldNamesForReports =
        mapOf(
            "reference" to "fileReference",
        )

    private val oldToNewFieldNamesForDocuments =
        mapOf(
            "report" to "fileName",
        )

    /**
     * Migrates the refactored Data Point Classes for the eu taxonomy non financials framework
     */
    fun migrateRefactoredDataPointClasses(dataTableEntity: DataTableEntity) {
        val migrationHelper = MigrationHelper()
        val dataObject = JSONObject(dataTableEntity.companyAssociatedData.getString("data"))
        migrationHelper.migrateReferencedReports(dataObject, oldToNewFieldNamesForReports)
        migrationHelper.migrateAssurance(
            dataObject, oldToNewFieldNamesForAssurance, oldToNewFieldNamesForDocuments,
            migrationHelper, framework = "euTaxonomyFinancials",
        )
        accessThreeLayeredDataSourceForOneMigration(
            dataObject, "insuranceKpis",
            "taxonomyEligibleNonLifeInsuranceActivitiesInPercent", oldToNewFieldNamesForDocuments,
            migrationHelper,
        )
        accessThreeLayeredDataSourceForOneMigration(
            dataObject, "investmentFirmKpis",
            "greenAssetRatioInPercent", oldToNewFieldNamesForDocuments, migrationHelper,
        )
        accessTwoLayeredDataSourceForMultipleMigrations(dataObject, oldToNewFieldNamesForDocuments, migrationHelper)
        accessThreeLayeredDataSourceForMultipleMigrations(dataObject, oldToNewFieldNamesForDocuments, migrationHelper)
        dataTableEntity.companyAssociatedData.put("data", dataObject.toString())
    }

    private fun accessThreeLayeredDataSourceForOneMigration(
        dataObject: JSONObject,
        firstLayerName: String,
        secondLayerName: String,
        migrationFieldNamesForDocuments: Map<String, String>,
        migrationHelper: MigrationHelper,
    ) {
        val firstLayerObject = dataObject.getOrJavaNull(firstLayerName) ?: return
        firstLayerObject as JSONObject
        val secondLayerObject = firstLayerObject.getOrJavaNull(secondLayerName) ?: return
        secondLayerObject as JSONObject
        migrationHelper.migrateOneSingleObjectOfDataSource(
            secondLayerObject, dataObject,
            migrationFieldNamesForDocuments, "euTaxonomyFinancials",
        )
    }

    private fun accessTwoLayeredDataSourceForMultipleMigrations(
        dataObject: JSONObject,
        migrationFieldNamesForDocuments: Map<String, String>,
        migrationHelper: MigrationHelper,
    ) {
        val firstLayerObject = dataObject.getOrJavaNull("creditInstitutionKpis") ?: return
        firstLayerObject as JSONObject
        val kpiKeyList = firstLayerObject.keys()
        kpiKeyList.forEach { kpiKey ->
            val secondLayerObject = firstLayerObject.getOrJavaNull(kpiKey) ?: return@forEach
            secondLayerObject as JSONObject
            migrationHelper.migrateOneSingleObjectOfDataSource(
                secondLayerObject, dataObject,
                migrationFieldNamesForDocuments, "euTaxonomyFinancials",
            )
        }
    }

    private fun accessThreeLayeredDataSourceForMultipleMigrations(
        dataObject: JSONObject,
        migrationFieldNamesForDocuments: Map<String, String>,
        migrationHelper: MigrationHelper,
    ) {
        val firstLayerObject = dataObject.getOrJavaNull("eligibilityKpis") ?: return
        firstLayerObject as JSONObject
        val secondLayerKeyList = firstLayerObject.keys()
        secondLayerKeyList.forEach { secondLayerKey ->
            val secondLayerObject = firstLayerObject.getOrJavaNull(secondLayerKey) ?: return@forEach
            secondLayerObject as JSONObject
            accessSecondOfThreeLayeredDataSourceForMultipleMigrations(
                secondLayerObject, dataObject,
                migrationFieldNamesForDocuments, migrationHelper,
            )
        }
    }

    private fun accessSecondOfThreeLayeredDataSourceForMultipleMigrations(
        secondLayerObject: JSONObject,
        dataObject: JSONObject,
        migrationFieldNamesForDocuments: Map<String, String>,
        migrationHelper: MigrationHelper,
    ) {
        val thirdLayerKeyList = secondLayerObject.keys()
        thirdLayerKeyList.forEach { thirdLayerKey ->
            val thirdLayerObject = secondLayerObject.getOrJavaNull(thirdLayerKey) ?: return@forEach
            thirdLayerObject as JSONObject
            migrationHelper.migrateOneSingleObjectOfDataSource(
                thirdLayerObject, dataObject,
                migrationFieldNamesForDocuments, "euTaxonomyFinancials",
            )
        }
    }
}

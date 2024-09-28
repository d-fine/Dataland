package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script removes the isGroupLevel and Currency fields of referencedReports
 * and also renames the reportDate field to publicationDate
 */
@Suppress("ClassName")
class V17__MigrateReferencedReports : BaseJavaMigration() {
    private val frameworksToMigrate =
        listOf(
            "eutaxonomy-non-financials",
            "eutaxonomy-financials",
            "sfdr",
        )

    override fun migrate(context: Context?) {
        frameworksToMigrate.forEach { framework ->

            migrateCompanyAssociatedDataOfDatatype(
                context,
                framework,
                { dataTableEntity -> migrateReferencedReports(dataTableEntity, framework) },
            )
        }
    }

    /**
     * Migrates the referencedReports to the new desired structure
     */
    fun migrateReferencedReports(
        dataTableEntity: DataTableEntity,
        framework: String,
    ) {
        val dataset = dataTableEntity.dataJsonObject
        val referencedReports = getReferencedReports(dataset, framework)
        if (referencedReports !== null) {
            referencedReports.keys().forEach {
                removeTwoFieldsAndRenameOne(referencedReports, it)
            }
        }
        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }

    /**
     * Gets the referencedReports based on the framework
     */
    private fun getReferencedReports(
        dataset: JSONObject,
        framework: String,
    ): JSONObject? {
        val referencedReports: JSONObject? =
            when (framework) {
                "eutaxonomy-non-financials" -> {
                    val general = dataset.getOrJavaNull("general") as JSONObject?
                    general?.getOrJavaNull("referencedReports") as JSONObject?
                }
                "eutaxonomy-financials" -> {
                    dataset.getOrJavaNull("referencedReports") as JSONObject?
                }
                "sfdr" -> {
                    val general = dataset.getOrJavaNull("general") as JSONObject?
                    val generalGeneral = general?.getOrJavaNull("general") as JSONObject?
                    generalGeneral?.getOrJavaNull("referencedReports") as JSONObject?
                }
                else -> {
                    null
                }
            }
        return referencedReports
    }

    /**
     * Removes isGroupLevel and Currency fields and rename reportDate field to publicationDate
     */
    private fun removeTwoFieldsAndRenameOne(
        referencedReports: JSONObject,
        reportName: String,
    ) {
        val report = referencedReports.getOrJavaNull(reportName)
        if (report !== null && report is JSONObject) {
            if (report.has("isGroupLevel")) report.remove("isGroupLevel")
            if (report.has("currency")) report.remove("currency")
            report.opt("reportDate")?.let {
                report.put("publicationDate", it)
                report.remove("reportDate")
            }
        }
    }
}

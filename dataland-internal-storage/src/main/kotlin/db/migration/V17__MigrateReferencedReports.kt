package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.slf4j.LoggerFactory

/**
 * This migration script removes the isGroupLevel and Currency fields of referencedReports
 * and also renames the reportDate field to publicationDate
 */
class V17__MigrateReferencedReports : BaseJavaMigration() {

    private val frameworksToMigrate = listOf(
        "eutaxonomy-non-financials",
        "eutaxonomy-financials",
        "sfdr",
        "sme",
        )

    private val logger = LoggerFactory.getLogger("Migration V15")
    override fun migrate(context: Context?) {
        frameworksToMigrate.forEach { framework ->

            migrateCompanyAssociatedDataOfDatatype(
                context,
                framework,
                { dataTableEntity -> migrateReferencedReports(dataTableEntity, framework) }
            )
        }
    }

    fun migrateReferencedReports(dataTableEntity: DataTableEntity, framework: String) {
        val dataset = dataTableEntity.dataJsonObject
        logger.info("reached point a")
        val referencedReports = getReferencedReports(dataset, framework)
        logger.info("reached point b")
        logger.info(referencedReports.toString())
        if (referencedReports !== null ){
            logger.info("wasn't null")
            referencedReports.keys().forEach {
                logger.info(it)
            removeTwoFieldsAndRenameOne(referencedReports, it)
        }}
        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }

    /**
     * Gets the referencedReports based on the framework
     */
    private fun getReferencedReports(dataset: JSONObject, framework: String): JSONObject? {
        return when (framework) {
            "eutaxonomy-non-financials" -> {
                val general = dataset.getOrJavaNull("general") as JSONObject? ?: return null
                general.getOrJavaNull("referencedReports") as JSONObject?
            }
            "eutaxonomy-financials" -> {
                dataset.getOrJavaNull("referencedReports") as JSONObject?
            }
            "sfdr" -> {
                val general = dataset.getOrJavaNull("general") as JSONObject? ?: return null
                val generalGeneral = general.getOrJavaNull("general") as JSONObject? ?: return null
                generalGeneral.getOrJavaNull("referencedReports") as JSONObject?
            }
            "sme" -> {
                val general = dataset.getOrJavaNull("general") as JSONObject? ?: return null
                val basicInformation = general.getOrJavaNull("basicInformation") as JSONObject? ?: return null
                basicInformation.getOrJavaNull("referencedReports") as JSONObject?
            }
            else -> {
                null
            }
        }
    }


    /**
     * Removes isGroupLevel and Currency fields and rename reportDate field to publicationDate
     */
    private fun removeTwoFieldsAndRenameOne(
        referencedReports: JSONObject,
        reportName: String,
    ){
        logger.info("entered function")
        val report = referencedReports.getOrJavaNull(reportName)
        logger.info(report.toString())
        if (report !== null && report is JSONObject) {
            if (report.has("isGroupLevel")) {
                val groupLevelValue = report.getString("isGroupLevel")
                logger.info("Found group level: $groupLevelValue")
                report.remove("isGroupLevel")}
            if (report.has("currency")) report.remove("currency")
            report.opt("reportDate")?.let {
                report.put("publicationDate", it)
                report.remove("reportDate")
            }
        }
    }
}
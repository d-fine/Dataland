package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.slf4j.LoggerFactory

/**
 * This migration script removes all file references that are not hashes and all
 * file references that are hashes but do not exist.
 */
class V15__MigrateGetRidOfFaultyDatasources : BaseJavaMigration() {

    private val regex = "^[a-fA-F0-9]{64}$".toRegex()

    private val dataTypesToMigrate = listOf(
        "eutaxonomy-non-financials",
        "eutaxonomy-financials",
        "sfdr",
        "lksg",
        "esg-questionnaire",
        "heimathafen",
        "sme",

    )

    // todo the actual file references needs to be fed to the list. now there are only dummies.
    var fileReferencesExisting: ArrayList<String> = ArrayList()

    private val logger = LoggerFactory.getLogger("Migration V15")

    /**
     * Get all valid fileReferences from database
     * blob_id in blob_items table is the actual hash value for the document
     */
    fun getExistingFileReferences(context: Context?) {
        val statement = context!!.connection.createStatement()

        if (statement.execute("SELECT blob_id FROM blob_items")) {
            val result = statement.resultSet

            while (result.next()) {
                var temp: String = result.getString("blob_id")
                fileReferencesExisting.add(temp)
                logger.info("Adding to known documents list: " + temp)
            }
        } else {
            throw RuntimeException("Error while accessing blob_items table in database")
        }
    }

    override fun migrate(context: Context?) {
        // get up to date list of file references
        getExistingFileReferences(context)
        // now check datasets
        dataTypesToMigrate.forEach {
            migrateCompanyAssociatedDataOfDatatype(
                context,
                it,
                this::migrateFaultyFileReferences,
            )
        }
    }



    private fun checkForFaultyFileReferenceAndIterateFurther(
        dataset: JSONObject,
        objectName: String,
        targetObjectName: String,
    ) {
        val obj = dataset.getOrJavaNull(objectName)
        if (obj !== null && obj is JSONObject) {
            val dataSourceOrCompanyInfoList = obj.getOrJavaNull(targetObjectName) as JSONObject?
            if (dataSourceOrCompanyInfoList !== null) {
                if (targetObjectName == "referencedReports") {

                    val keys: Iterator<String> = dataSourceOrCompanyInfoList.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val companyInfo = dataSourceOrCompanyInfoList.getOrJavaNull(key) as JSONObject?
                        if (companyInfo !== null) {
                            val fileReference = companyInfo.get("fileReference") as String
                            if ((fileReference !in fileReferencesExisting) || (!isSha256(fileReference))) {
                                logger.info("Remove reference to document from CompanyInformation list. The broken file refrence was " + fileReference)
                                dataSourceOrCompanyInfoList.remove(key)
                            }
                        }
                    }
                    if (dataSourceOrCompanyInfoList.isEmpty) {
                        obj.put(targetObjectName, null as Any?)
                    }
                }
                else {
                    val fileReference = dataSourceOrCompanyInfoList.get("fileReference") as String
                    if ((fileReference !in fileReferencesExisting) || (!isSha256(fileReference))) {
                        logger.info("Replace reference to document with null. The broken file refrence was " + fileReference)
                        obj.put(targetObjectName, null as Any?)
                    }
                }
            } else {
                obj.keys().forEach { checkForFaultyFileReferenceAndIterateFurther(obj, it, targetObjectName) }
            }
        }
    }

    private fun isSha256(fileReference: String): Boolean {
        return fileReference.matches(regex)
    }

    /**
     * Migrate the data points with blank file references to containing null-valued a dataSource instead
     */
    fun migrateFaultyFileReferences(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject
        dataset.keys().forEach {
            checkForFaultyFileReferenceAndIterateFurther(
                dataset, it, "dataSource",
            )
        }
        dataset.keys().forEach {
            checkForFaultyFileReferenceAndIterateFurther(
                dataset, it, "referencedReports",
            )
        }
        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }
}

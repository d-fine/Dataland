package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * This migration script removes all file references that are not hashes and all
 * file references that are hashes but do not exist.
 */
@Suppress("ClassName")
class V15__MigrateGetRidOfFaultyDatasources : BaseJavaMigration() {
    private val regex = "^[a-fA-F0-9]{64}$".toRegex()

    private val dataTypesToMigrate =
        listOf(
            "eutaxonomy-non-financials",
            "eutaxonomy-financials",
            "sfdr",
            "lksg",
            "esg-questionnaire",
            "heimathafen",
            "sme",
        )

    private val logger = LoggerFactory.getLogger("Migration V15")

    // Array containing all valid file references
    var validFileReferences: ArrayList<String> = ArrayList()

    /**
     * Get all valid fileReferences from database and add them to fileReferencesExisting
     *
     * blob_id in the blob_items table is the same as the fileReference, the actual hash value of the document.
     * Entries in blob_items are assumed to be consistent with the document_meta_info table of the DocumentManagerDB.
     */
    private fun getExistingFileReferences(context: Context?) {
        val statement = context!!.connection.createStatement()

        if (statement.execute("SELECT blob_id FROM blob_items")) {
            val result = statement.resultSet

            while (result.next()) {
                val temp: String = result.getString("blob_id")
                validFileReferences.add(temp)
                logger.info("Adding to known documents list: $temp")
            }
        } else {
            throw IOException("Error while accessing blob_items table in database")
        }
    }

    override fun migrate(context: Context?) {
        getExistingFileReferences(context)
        dataTypesToMigrate.forEach {
            migrateCompanyAssociatedDataOfDatatype(
                context,
                it,
                this::migrateFaultyFileReferences,
            )
        }
    }

    private fun isFaultyFileReference(fileReference: String): Boolean =
        (fileReference !in validFileReferences) || (!isSha256(fileReference))

    /**
     * Remove company reports with invalid fileReference from map of company reports.
     * If no company reports are left, replace referencedReports with null.
     */
    private fun replaceFaultyFileReferenceReferencedReports(
        companyReportMap: JSONObject,
        obj: JSONObject,
    ) {
        val keysToBeRemoved: ArrayList<String> = ArrayList()

        companyReportMap.keys().forEach {
            val companyReport = companyReportMap.getOrJavaNull(it) as JSONObject?
            if (companyReport !== null) {
                val fileReference = companyReport["fileReference"] as String
                if (isFaultyFileReference(fileReference)) {
                    logger.info(
                        "Remove reference to document from CompanyReport Map." +
                            " Broken file reference: " + fileReference,
                    )
                    keysToBeRemoved.add(it)
                }
            }
        }
        for (key in keysToBeRemoved) {
            companyReportMap.remove(key)
        }
        if (companyReportMap.isEmpty) {
            obj.put(referencedReportsKey, null as Any?)
        }
    }

    /**
     * Replace dataSource with null if fileReference is invalid
     */
    private fun replaceFaultyFileReferenceDataSource(
        dataSource: JSONObject,
        obj: JSONObject,
    ) {
        val fileReference = dataSource["fileReference"] as String
        if (isFaultyFileReference(fileReference)) {
            logger.info("Replace reference to document with null. Broken file reference: $fileReference")
            obj.put(dataSourceKey, null as Any?)
        }
    }

    /**
     * Recursively iterate over the elements of a dataset and find dataSources or referencedReports
     */
    private fun checkRecursivelyForFaultyFileReferences(
        dataset: JSONObject,
        objectName: String,
    ) {
        val obj = dataset.getOrJavaNull(objectName)
        if (obj !== null && obj is JSONObject) {
            val dataSource = obj.getOrJavaNull(dataSourceKey) as JSONObject?
            if (dataSource !== null) {
                replaceFaultyFileReferenceDataSource(dataSource, obj)
            }

            val companyReportList = obj.getOrJavaNull(referencedReportsKey) as JSONObject?
            if (companyReportList !== null) {
                replaceFaultyFileReferenceReferencedReports(companyReportList, obj)
            }
            obj.keys().forEach {
                checkRecursivelyForFaultyFileReferences(obj, it)
            }
        }
    }

    private fun isSha256(fileReference: String): Boolean = fileReference.matches(regex)

    private val dataSourceKey = "dataSource"
    private val referencedReportsKey = "referencedReports"

    /**
     * Migrate the data points
     *
     * There are three objects storing fileReferences: Base and ExtendedDocumentReference and CompanyReport.
     * ExtendedDocumentReference and BaseDocumentReference are consistently referred to as "dataSource".
     * CompanyReport only occurs list-like as Map<String, CompanyReport>? which is consistently referred to as
     * "referencedReports".
     * Both cases are handled separately.
     *
     */

    fun migrateFaultyFileReferences(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject
        dataset.keys().forEach {
            checkRecursivelyForFaultyFileReferences(dataset, it)
        }
        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }
}

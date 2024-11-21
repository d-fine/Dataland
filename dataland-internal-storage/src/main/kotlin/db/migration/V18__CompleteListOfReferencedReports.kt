package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.slf4j.LoggerFactory

/**
 * This migration script completes the list of referenced reports
 * with all reports referenced in ExtendedDataPoints.
 */
@Suppress("ClassName")
class V18__CompleteListOfReferencedReports : BaseJavaMigration() {
    private val frameworksToMigrate =
        listOf(
            "eutaxonomy-non-financials",
            "eutaxonomy-financials",
            "sfdr",
        )

    private val logger = LoggerFactory.getLogger("Migration V18")

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
        val dataPoints = findAllDataPoints(dataset)

        if (dataPoints.isEmpty()) {
            // There are no additional information for (optional) new referenced reports
            return
        }

        val referencedReports = getOrInsertReferencedReports(dataset, framework)
        updateReferencedReports(referencedReports, dataPoints)
        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }

    private fun findAllDataPoints(jsonObject: JSONObject): List<JSONObject> {
        val dataSource = jsonObject.getOrJavaNull("dataSource") as JSONObject?
        return if (dataSource !== null && dataSource.has("fileName") && dataSource.has("fileReference")) {
            listOf(dataSource)
        } else {
            jsonObject
                .keys()
                .asSequence()
                .mapNotNull { key ->
                    jsonObject.optJSONObject(key)?.let { subJsonObject ->
                        findAllDataPoints(subJsonObject)
                    }
                }.flatten()
                .toList()
        }
    }

    /**
     * Gets the referencedReports based on the framework
     * If the object is null we insert a new object and return this object
     */
    private fun getOrInsertReferencedReports(
        dataset: JSONObject,
        framework: String,
    ): JSONObject {
        fun getOrInsertJSONObject(
            jsonObject: JSONObject,
            key: String,
        ): JSONObject {
            if (jsonObject.optJSONObject(key) == null) {
                jsonObject.put(key, JSONObject())
            }
            return jsonObject.getJSONObject(key)
        }

        val referencedReports: JSONObject =
            when (framework) {
                "eutaxonomy-non-financials" -> {
                    val general = dataset.optJSONObject("general")
                    getOrInsertJSONObject(general, "referencedReports")
                }

                "eutaxonomy-financials" -> {
                    getOrInsertJSONObject(dataset, "referencedReports")
                }

                "sfdr" -> {
                    val general = dataset.optJSONObject("general")
                    val generalGeneral = general.optJSONObject("general")
                    getOrInsertJSONObject(generalGeneral, "referencedReports")
                }
                else -> {
                    JSONObject()
                }
            }
        return referencedReports
    }

    private fun updateReferencedReports(
        referencedReports: JSONObject,
        dataPoints: List<JSONObject>,
    ) {
        val referencedReportsFileReferences =
            referencedReports
                .keys()
                .asSequence()
                .map { key ->
                    val nestedObject = referencedReports.getJSONObject(key)
                    Pair(nestedObject.getString("fileReference"), nestedObject)
                }.toMap()

        val dataPointsFileReferences =
            dataPoints.associate { dataPoint ->
                val fileReference = dataPoint.getString("fileReference")
                val fileName = dataPoint.getString("fileName")
                Pair(fileReference, fileName)
            } // note that in the case of the same key with two names the last key remains

        val allFileReferences = referencedReportsFileReferences.keys.union(dataPointsFileReferences.keys)

        for (fileReference in allFileReferences) {
            val referencedReport = referencedReportsFileReferences[fileReference]
            val dataPointFileName = dataPointsFileReferences[fileReference]
            updateFileReference(fileReference, referencedReport, dataPointFileName, referencedReports)
        }
    }

    private fun updateFileReference(
        fileReference: String?,
        referencedReport: JSONObject?,
        dataPointFileName: String?,
        referencedReports: JSONObject,
    ) {
        if (referencedReport != null) {
            if (!referencedReport.optString("fileName").isNullOrEmpty()) {
                return
            }

            if (dataPointFileName != null) {
                referencedReport.put("fileName", dataPointFileName)
            } else {
                logger.warn(
                    "Found a referenced report with file reference $fileReference without a file name" +
                        "and no data point with a fileName",
                )
            }
        } else if (dataPointFileName != null) {
            // there is no referenced report, hence we add a new json object into the list of referenced reports
            referencedReports.put(
                dataPointFileName,
                JSONObject(mapOf("fileReference" to fileReference, "fileName" to dataPointFileName)),
            )
        } else {
            logger.warn(
                "Found a file reference $fileReference without a file name " +
                    "and no data point with a fileName",
            )
        }
    }
}

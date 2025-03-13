package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getCompanyAssociatedDatasetsForDataType
import db.migration.utils.getFromPath
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.slf4j.LoggerFactory

/**
 * This migration script copies the data fields 'iloCoreLabourStandards' and 'humanRightsDueDiligence' of SFDR
 * data sets to corresponding EU taxonomy non-financial data sets.
 */
@Suppress("ClassName")
class V27__CopyExistingSfdrFieldsToEUTaxonomyNonFinancials : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger("Migration V27")

    override fun migrate(context: Context?) {
        val sfdrData = getCompanyAssociatedDatasetsForDataType(context, "sfdr")
        val availableSfdrDataPointsMapping =
            extractSfdrFieldsAsMapping(
                sfdrData,
                setOf(
                    "social/socialAndEmployeeMatters/iloCoreLabourStandards",
                    "social/socialAndEmployeeMatters/humanRightsDueDiligence",
                ),
            )
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "eutaxonomy-non-financials",
            { (this::augmentTaxonomyNonFinancialsWithSfdrData)(it, availableSfdrDataPointsMapping) },
        )
    }

    /**
     * Retrieve a mapping of companyId and reportingPeriod to a map of the given field names associated with the
     * corresponding data for all SFDR data sets.
     * @param context the Context object
     * @param fieldPaths a set of data field specifiers that include the hierarchy
     */
    fun extractSfdrFieldsAsMapping(
        sfdrData: List<DataTableEntity>,
        fieldPaths: Set<String>,
    ): Map<Pair<String, String>, Map<String, Any?>> =
        sfdrData.associate { dataSet ->
            Pair(
                dataSet.companyAssociatedData.getString("companyId"),
                dataSet.companyAssociatedData.getString("reportingPeriod"),
            ) to
                fieldPaths.associate { path ->
                    path.substringAfterLast('/') to dataSet.dataJsonObject.getFromPath(path)
                }
        }

    /**
     * Migrates the rate of accidents in the sfdr data
     */
    fun augmentTaxonomyNonFinancialsWithSfdrData(
        dataTableEntity: DataTableEntity,
        sfdrData: Map<Pair<String, String>, Map<String, Any?>>,
    ) {
        logger.info(
            "Add SFDR data to EU taxonomy non financials for data id: " + dataTableEntity.dataId,
        )

        val dataset = dataTableEntity.dataJsonObject
        sfdrData[
            Pair(
                dataTableEntity.companyAssociatedData.getString("companyId"),
                dataTableEntity.companyAssociatedData.getString("reportingPeriod"),
            ),
        ]?.let {
            it.forEach { (key, value) ->
                dataset.optJSONObject("general").put(key, value ?: JSONObject.NULL)
            }
        }
        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }
}

package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.sql.Connection

/**
 * This class migrates SFDR QA reports to the new SFDR data model
 */
@Suppress("ClassName")
class V7__UpdateSfdrQaReports : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun migrate(context: Context?) {
        val targetConnection = context!!.connection
        val tableResultSet = targetConnection.metaData.getTables(null, null, "qa_reports", null)
        if (tableResultSet.next()) {
            migrateSfdrModel(targetConnection)
        }
    }

    /**
     * Migration of SFDR QA reports to the new SFDR data model
     */
    private fun migrateSfdrModel(targetConnection: Connection) {
        val queueResultSet =
            targetConnection.createStatement().executeQuery(
                "SELECT qa_report_id, qa_report FROM qa_reports" +
                    " WHERE data_type = 'sfdr'",
            )

        val updateStatement =
            targetConnection.prepareStatement(
                "UPDATE qa_reports SET qa_report = ? WHERE qa_report_id = ?",
            )

        while (queueResultSet.next()) {
            val qaReportId = queueResultSet.getString("qa_report_id")

            logger.info("Migrating the SFDR QA report for QA report ID: $qaReportId")

            val qaReport = JSONObject(queueResultSet.getString("qa_report"))
            val updatedQaReport = migrateQaReport(qaReport)

            updateStatement.setString(1, updatedQaReport.toString())
            updateStatement.setString(2, qaReportId)
            updateStatement.executeUpdate()
        }
        updateStatement.close()
        queueResultSet.close()
    }

    /**
     * Migrate a single SFDR QA report
     * @param qaReport to update as a JSONObject
     * @return the updated qaReport as a JSONObject
     */
    fun migrateQaReport(qaReport: JSONObject): JSONObject {
        val socialAndEmployeeMattersObject =
            qaReport.optJSONObject("social")?.optJSONObject("socialAndEmployeeMatters") ?: return qaReport

        socialAndEmployeeMattersObject.remove("rateOfAccidentsInPercent")?.let {
            socialAndEmployeeMattersObject.put("rateOfAccidents", it)
        }

        val excessiveCeoPayRatioInPercentValue =
            socialAndEmployeeMattersObject.remove("excessiveCeoPayRatioInPercent") as? JSONObject
        val ceoToEmployeePayGapRatioValue =
            socialAndEmployeeMattersObject.remove("ceoToEmployeePayGapRatio") as? JSONObject
        val valueToUse = determineValueToUse(excessiveCeoPayRatioInPercentValue, ceoToEmployeePayGapRatioValue)

        valueToUse?.let { socialAndEmployeeMattersObject.put("excessiveCeoPayRatio", it) }

        qaReport.optJSONObject("social").put("socialAndEmployeeMatters", socialAndEmployeeMattersObject)

        return qaReport
    }

    /**
     * Determine which value to put into the new excessiveCeoPayRatio field in the QA report
     * If one of the two old fields is null, take the other one; if both are null, set to null.
     * If both fields are given, check if the ceoToEmployeePayGapRatioValue has valid corrected data; if so, take this
     * one; if not (e.g. ceoToEmployeePayGapRatioValue is not null but an empty QA report), take the other one.
     */
    private fun determineValueToUse(
        excessiveCeoPayRatioInPercentValue: JSONObject?,
        ceoToEmployeePayGapRatioValue: JSONObject?,
    ): JSONObject? {
        if (excessiveCeoPayRatioInPercentValue == null || ceoToEmployeePayGapRatioValue == null) {
            return excessiveCeoPayRatioInPercentValue ?: ceoToEmployeePayGapRatioValue
        }
        return when {
            isValueOfObjectAValidNumber(ceoToEmployeePayGapRatioValue.getJSONObject("correctedData"))
            -> ceoToEmployeePayGapRatioValue

            isValueOfObjectAValidNumber(excessiveCeoPayRatioInPercentValue.getJSONObject("correctedData"))
            -> excessiveCeoPayRatioInPercentValue

            else -> ceoToEmployeePayGapRatioValue
        }
    }

    /**
     * Checks if the value of an object is a valid number
     */
    private fun isValueOfObjectAValidNumber(jsonObject: JSONObject): Boolean =
        jsonObject.has("value") && jsonObject.get("value").toString().toDoubleOrNull() != null
}

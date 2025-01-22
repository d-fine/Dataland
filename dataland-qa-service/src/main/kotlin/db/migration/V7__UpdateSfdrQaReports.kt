package org.dataland.datalandqaservice.db.migration

import com.fasterxml.jackson.databind.ObjectMapper
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.sql.Connection

/**
 * Change column names to new version
 */
@Suppress("ClassName")
class V7__UpdateSfdrQaReports : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun migrate(context: Context?) {
        val targetConnection = context!!.connection
        migrateSfdrModel(targetConnection)
    }

    /**
     * Migration of sfdr fields in the qa_report
     */
    private fun migrateSfdrModel(targetConnection: Connection) {
        val queueResultSet =
            targetConnection.createStatement().executeQuery(
                "SELECT qa_report_id, qa_report FROM qa_reports" +
                    " WHERE data_type = 'sfdr'",
            )

        val objectMapper = ObjectMapper()

        val updateStatement =
            targetConnection.prepareStatement(
                "UPDATE qa_reports SET qa_report = ? WHERE qa_report_id = ?",
            )

        while (queueResultSet.next()) {
            val qaReportId = queueResultSet.getString("qa_report_id")

            logger.info("Migrating sfdr fields for qa report id: $qaReportId")

            val qaReport =
                JSONObject(
                    objectMapper.readValue(
                        queueResultSet.getString("qa_report"), String::class.java,
                    ),
                )

            val updatedQaReport = migrateQaReport(qaReport)

            val updatedQaReportString = objectMapper.writeValueAsString(updatedQaReport)

            updateStatement.setString(1, updatedQaReportString)
            updateStatement.setString(2, qaReportId)
            updateStatement.executeUpdate()
        }
        updateStatement.close()
        queueResultSet.close()
    }

    /**
     * Migrate a single qa report
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
     * Determine which value to put into the new excessiveCeoPayRatio field
     */
    private fun determineValueToUse(
        excessiveCeoPayRatioInPercentValue: JSONObject?,
        ceoToEmployeePayGapRatioValue: JSONObject?,
    ): JSONObject? {
        if (excessiveCeoPayRatioInPercentValue == null || ceoToEmployeePayGapRatioValue == null) {
            logger.info("Shouldn't happen")
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

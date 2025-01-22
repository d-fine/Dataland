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
class V7__UpdateSfdrModel : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger("Migration V7")

    override fun migrate(context: Context?) {
        val targetConnection = context!!.connection
        migrateSfdrModel(targetConnection)
    }

    /**
     * Migration of qa-queue data into new qa_review table (no data for qa_status or qa_reviewer)
     */
    private fun migrateSfdrModel(targetConnection: Connection) {
        val queueResultSet =
            targetConnection.createStatement().executeQuery(
                "SELECT qa_report_id, qa_report FROM qa_reports" +
                        " WHERE data_type = 'sfdr'"
            )

        val objectMapper = ObjectMapper()

        val updateStatement = targetConnection.prepareStatement(
            "UPDATE qa_reports SET qa_report = ? WHERE qa_report_id = ?"
        )

        while (queueResultSet.next()) {
            val qa_report_id = queueResultSet.getString("qa_report_id")
            val qa_report = JSONObject(
                    objectMapper.readValue(
                        queueResultSet.getString("qa_report"), String::class.java,
                    )
            )

            val socialAndEmployeeMattersObject =
                qa_report.optJSONObject("social")?.optJSONObject("socialAndEmployeeMatters") ?: return

            // migrate rate of accidents
            socialAndEmployeeMattersObject.remove("rateOfAccidentsInPercent")?.let {
                socialAndEmployeeMattersObject.put("rateOfAccidents", it)
            }

            // migrate ceo pay gap
            val excessiveCeoPayRatioInPercentValue =
                socialAndEmployeeMattersObject.remove("excessiveCeoPayRatioInPercent") as? JSONObject
            val ceoToEmployeePayGapRatioValue =
                socialAndEmployeeMattersObject.remove("ceoToEmployeePayGapRatio") as? JSONObject
            val valueToUse = determineValueToUse(excessiveCeoPayRatioInPercentValue, ceoToEmployeePayGapRatioValue)

            valueToUse?.let { socialAndEmployeeMattersObject.put("excessiveCeoPayRatio", it) }

            //put qa_report back into database
            // Update the qa_report in the database
            val updatedQaReportString = objectMapper.writeValueAsString(qa_report)

            updateStatement.setString(1, updatedQaReportString)
            updateStatement.setString(2, qa_report_id)
            updateStatement.executeUpdate()

            updateStatement.close()
            queueResultSet.close()
        }

    }

    /**
     * Determine which value to put into the new excessiveCeoPayRatio field
     */
    private fun determineValueToUse(
        excessiveCeoPayRatioInPercentValue: JSONObject?,
        ceoToEmployeePayGapRatioValue: JSONObject?,
    ): JSONObject? {
        if (excessiveCeoPayRatioInPercentValue == null || ceoToEmployeePayGapRatioValue == null) {
            return excessiveCeoPayRatioInPercentValue ?: ceoToEmployeePayGapRatioValue
        }
        return when {
            isValueOfObjectAValidNumber(ceoToEmployeePayGapRatioValue) -> ceoToEmployeePayGapRatioValue
            isValueOfObjectAValidNumber(excessiveCeoPayRatioInPercentValue) -> excessiveCeoPayRatioInPercentValue
            else -> ceoToEmployeePayGapRatioValue
        }
    }
    /**
     * Checks if the value of an object is a valid number
     */
    private fun isValueOfObjectAValidNumber(jsonObject: JSONObject): Boolean =
        jsonObject.has("value") && jsonObject.get("value").toString().toDoubleOrNull() != null

}


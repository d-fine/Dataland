package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.util.UUID
import javax.sql.DataSource

/**
 * Service to determine if data is available
 */
@Service("DataAvailabilityChecker")
class DataAvailabilityChecker
    @Autowired
    constructor(
        private val dataSource: DataSource,
    ) {
        /**
         * Retrieves metadata of datasets that are currently active for the given data dimensions.
         * @param dataDimensions List of data dimensions to search for.
         * @return List of DataMetaInformation objects that match the provided data dimensions.
         */
        fun getMetaDataOfActiveDatasets(dataDimensions: List<BasicDataDimensions>): List<DataMetaInformation> {
            assertBasicDataDimensionsAreWellFormed(dataDimensions)
            val formattedTuples =
                dataDimensions.joinToString(", ") {
                    "('${it.companyId}', '${it.dataType}', '${it.reportingPeriod}')"
                }

            val preBuildQuery = """
            SELECT *
            FROM data_meta_information
            WHERE (company_id, data_type, reporting_period) IN ($formattedTuples)
            AND currently_active = true
        """

            return JdbcTemplate(dataSource).query(preBuildQuery) { rs, _ ->
                DataMetaInformation(
                    dataId = rs.getString("data_id"),
                    companyId = rs.getString("company_id"),
                    dataType = DataType.valueOf(rs.getString("data_type")),
                    reportingPeriod = rs.getString("reporting_period"),
                    currentlyActive = rs.getBoolean("currently_active"),
                    uploadTime = rs.getLong("upload_time"),
                    qaStatus = QaStatus.Accepted,
                    uploaderUserId = rs.getString("uploader_user_id"),
                )
            }
        }

        private fun assertBasicDataDimensionsAreWellFormed(dataDimensions: List<BasicDataDimensions>) {
            dataDimensions.forEach { dimensions ->
                assertStringRepresentsCompanyId(dimensions.companyId)
                assertStringRepresentsDataType(dimensions.dataType)
                assertStringRepresentsReportingPeriod(dimensions.reportingPeriod)
            }
        }

        private fun assertStringRepresentsReportingPeriod(reportingPeriod: String) {
            if (!reportingPeriod.matches(Regex("\\d{4}"))) {
                throw IllegalArgumentException("Invalid reporting period format: $reportingPeriod. Expected format: YYYY.")
            }
        }

        private fun assertStringRepresentsCompanyId(companyId: String) {
            UUID.fromString(companyId)
        }

        private fun assertStringRepresentsDataType(dataType: String) {
            DataType.valueOf(dataType)
        }
    }

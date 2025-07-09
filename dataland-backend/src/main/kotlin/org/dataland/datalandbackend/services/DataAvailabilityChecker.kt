package org.dataland.datalandbackend.services

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service to determine if data is available
 */
@Service("DataAvailabilityChecker")
class DataAvailabilityChecker(
    @PersistenceContext private val entityManager: EntityManager,
) {
    /**
     * Retrieves metadata of datasets that are currently active for the given data dimensions.
     * @param dataDimensions List of data dimensions to search for.
     * @return List of DataMetaInformation objects that match the provided data dimensions.
     */
    fun getMetaDataOfActiveDatasets(dataDimensions: List<BasicDataDimensions>): List<DataMetaInformation> {
        val dimensionsToProcess = filterOutInvalidDimensions(dataDimensions)
        val formattedTuples =
            dimensionsToProcess.joinToString(", ") {
                "('${it.companyId}', '${it.dataType}', '${it.reportingPeriod}')"
            }

        val queryToExecute =
            "SELECT * FROM data_meta_information" +
                " WHERE (company_id, data_type, reporting_period) IN ($formattedTuples)" +
                " AND currently_active = true"

        return if (dimensionsToProcess.isNotEmpty()) {
            val query = entityManager.createNativeQuery(queryToExecute, DataMetaInformationEntity::class.java)
            return (query.resultList as List<DataMetaInformationEntity>).map { it.toApiModel() }
        } else {
            emptyList()
        }
    }

    private fun filterOutInvalidDimensions(dataDimensions: List<BasicDataDimensions>) =
        dataDimensions.filter { dimensions ->
            isCompanyId(dimensions.companyId) &&
                isDataType(dimensions.dataType) &&
                isReportingPeriod(dimensions.reportingPeriod)
        }

    private fun isReportingPeriod(testString: String) = testString.matches(Regex("\\d{4}"))

    private fun isCompanyId(testString: String): Boolean {
        try {
            UUID.fromString(testString)
            return true
        } catch (ignore: Exception) {
            return false
        }
    }

    private fun isDataType(testString: String) = DataType.values.any { it.toString() == testString }
}

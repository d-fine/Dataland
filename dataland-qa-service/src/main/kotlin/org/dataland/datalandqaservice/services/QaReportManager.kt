package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.QaReportRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.IdUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * A service class for managing QA report meta-information
 */
@Service
class QaReportManager(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val qaReportRepository: QaReportRepository,
) {

    /**
     * Method to make the QA report manager create a new QA report
     * @param report the QA report to be stored
     * @param dataId the ID of the data set the QA report is associated with
     * @param dataType the type of the data set the QA report is associated with
     * @param reporterUserId the ID of the user who uploaded the QA report
     * @param uploadTime the time the QA report was uploaded
     * @return the created QA report
     */
    fun createQaReport(
        report: Any,
        dataId: String,
        dataType: String,
        reporterUserId: String,
        uploadTime: Long,
    ): QaReportEntity {
        val qaReportId = IdUtils.generateUUID()
        return qaReportRepository.save(
            QaReportEntity(
                qaReportId = qaReportId,
                qaReport = objectMapper.writeValueAsString(report),
                dataId = dataId,
                dataType = dataType,
                reporterUserId = reporterUserId,
                uploadTime = uploadTime,
            ),
        )
    }

    /**
     * Method to make the QA report manager get meta info about one specific QA report
     * @param qaReportId filters the requested meta info to one specific QA report ID
     * @return meta info about QA report behind the qaReportId
     */
    fun getQaReportById(dataId: String, dataType: String, qaReportId: String): QaReportEntity {
        val dataEntity = qaReportRepository.findById(qaReportId).orElseThrow {
            ResourceNotFoundApiException(
                "QA report not found",
                "No QA report with the id: $qaReportId could be found.",
            )
        }
        if (dataEntity.dataId != dataId) {
            throw InvalidInputApiException(
                "QA report '$qaReportId' not associated with data '$dataId'",
                "The requested Qa Report '$qaReportId' is not associated with data '$dataId'," +
                    " but with data '${dataEntity.dataId}'.",
            )
        }

        if (dataEntity.dataType != dataType) {
            throw InvalidInputApiException(
                "QA report '$qaReportId' not associated with data type '$dataType'",
                "The requested Qa Report '$qaReportId' is not associated with data type '$dataType'," +
                    " but with data type '${dataEntity.dataType}'.",
            )
        }

        return dataEntity
    }

    /**
     * Method to make the QA report manager get all meta infos associated with a data set
     * @param dataId filters the requested meta info to one specific data ID
     * @return a list of meta info about QA reports associated to the data set
     */
    fun searchQaReportMetaInfo(
        dataId: String,
        dataType: String,
        reporterUserId: String?,
    ): List<QaReportEntity> {
        val searchResults = qaReportRepository.searchQaReportMetaInformation(dataId, reporterUserId)
        val wrongDataType = searchResults.find { it.dataType != dataType }
        if (wrongDataType != null) {
            throw InvalidInputApiException(
                "QA reports not associated with data type '$dataType'",
                "The requested QA reports are not associated with data type '$dataType'," +
                    " but with '${wrongDataType.dataType}'.",
            )
        }
        return searchResults
    }
}

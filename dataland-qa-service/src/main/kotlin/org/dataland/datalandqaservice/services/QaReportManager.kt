package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.QaReportRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.IdUtils
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * A service class for managing QA report meta-information
 */
@Service
class QaReportManager(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val metaDataControllerApi: MetaDataControllerApi,
    @Autowired private val qaReportRepository: QaReportRepository,
    @Autowired private val qaReportSecurityPolicy: QaReportSecurityPolicy,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun ensureDatalandDataExists(
        dataId: String,
        dataType: String,
    ) {
        try {
            val dataMetaInfo = metaDataControllerApi.getDataMetaInfo(dataId)

            if (dataMetaInfo.dataType.value != dataType) {
                throw InvalidInputApiException(
                    "Data type mismatch",
                    "The requested data set '$dataId' is of type '${dataMetaInfo.dataType}'," +
                        " but the expected type is '$dataType'.",
                )
            }
        } catch (ex: ClientException) {
            val exceptionToThrow =
                if (ex.statusCode == HttpStatus.NOT_FOUND.value()) {
                    ResourceNotFoundApiException(
                        "Dataset '$dataId' not found",
                        "No data set with the id: $dataId could be found.",
                        ex,
                    )
                } else {
                    ex
                }
            throw exceptionToThrow
        }
    }

    /**
     * Method to make the QA report manager create a new QA report
     * @param report the QA report to be stored
     * @param dataId the ID of the data set the QA report is associated with
     * @param dataType the type of the data set the QA report is associated with
     * @param reporterUserId the ID of the user who uploaded the QA report
     * @param uploadTime the time the QA report was uploaded
     * @return the created QA report
     */
    @Transactional
    fun <QaReportType> createQaReport(
        report: QaReportType,
        dataId: String,
        dataType: String,
        reporterUserId: String,
        uploadTime: Long,
    ): QaReportEntity {
        ensureDatalandDataExists(dataId, dataType)
        qaReportRepository.markAllReportsInactiveByDataIdAndReportingUserId(dataId, reporterUserId)
        return qaReportRepository.save(
            QaReportEntity(
                qaReportId = IdUtils.generateUUID(),
                qaReport = objectMapper.writeValueAsString(report),
                dataId = dataId,
                dataType = dataType,
                reporterUserId = reporterUserId,
                uploadTime = uploadTime,
                active = true,
            ),
        )
    }

    /**
     * Method to set the status of a QA report
     * @param qaReportId the ID of the QA report to be updated
     * @param dataId the ID of the data set the QA report is associated with
     * @param dataType the type of the data set the QA report is associated with
     * @param statusToSet the new status of the QA report
     * @param requestingUser the user requesting the change
     * @return the updated QA report
     */
    @Transactional
    fun setQaReportStatus(
        qaReportId: String,
        dataId: String,
        dataType: String,
        statusToSet: Boolean,
        requestingUser: DatalandAuthentication,
    ): QaReportEntity {
        val storedQaReportEntity = getQaReportById(dataId, dataType, qaReportId)
        if (!qaReportSecurityPolicy.canUserSetQaReportStatus(storedQaReportEntity, requestingUser)) {
            throw InsufficientRightsApiException(
                "Required access rights missing",
                "You do not have the required access rights to update QA report with the id: $qaReportId",
            )
        }
        logger.info("Setting report with ID $qaReportId to active=$statusToSet")
        storedQaReportEntity.active = statusToSet

        return qaReportRepository.save(storedQaReportEntity)
    }

    /**
     * Method to make the QA report manager get meta info about one specific QA report
     * @param qaReportId filters the requested meta info to one specific QA report ID
     * @return meta info about QA report behind the qaReportId
     */
    @Transactional(readOnly = true)
    fun getQaReportById(
        dataId: String,
        dataType: String,
        qaReportId: String,
    ): QaReportEntity {
        val dataEntity =
            qaReportRepository.findById(qaReportId).orElseThrow {
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
     * Deletes all QA reports for a specific dataId.
     */
    @Transactional
    fun deleteAllQaReportsForDataId(
        dataId: String,
        correlationId: String,
    ) {
        logger.info("Deleting all QA reports associated with dataId $dataId (correlationId: $correlationId)")
        qaReportRepository.deleteAllByDataId(dataId)
    }

    /**
     * Method to make the QA report manager get all meta infos associated with a data set
     * @param dataId filters the requested meta info to one specific data ID
     * @return a list of meta info about QA reports associated to the data set
     */
    @Transactional(readOnly = true)
    fun searchQaReportMetaInfo(
        dataId: String,
        dataType: String,
        showInactive: Boolean,
        reporterUserId: String?,
    ): List<QaReportEntity> {
        val searchResults =
            qaReportRepository.searchQaReportMetaInformation(
                dataId = dataId,
                reporterUserId = reporterUserId,
                showInactive = showInactive,
            )
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

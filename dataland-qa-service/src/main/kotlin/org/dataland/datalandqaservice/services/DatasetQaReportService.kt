package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportWithMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.QaReportRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory

/**
 * Abstract class for managing QA reports for datasets
 */
abstract class DatasetQaReportService(
    open val qaReportRepository: QaReportRepository,
    open val qaReportSecurityPolicy: QaReportSecurityPolicy,
) {
    open val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method to make the QA report manager create a new QA report
     * @param report the QA report to be stored
     * @param dataId the ID of the data set the QA report is associated with
     * @param dataType the type of the data set the QA report is associated with
     * @param reporterUserId the ID of the user who uploaded the QA report
     * @param uploadTime the time the QA report was uploaded
     * @return the created QA report
     */
    abstract fun <QaReportType> createQaReport(
        report: QaReportType,
        dataId: String,
        dataType: String,
        reporterUserId: String,
        uploadTime: Long,
    ): QaReportMetaInformation

    protected fun getQaReportEntityById(
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
     * Method to set the status of a QA report
     * @param qaReportId the ID of the QA report to be updated
     * @param dataId the ID of the data set the QA report is associated with
     * @param dataType the type of the data set the QA report is associated with
     * @param statusToSet the new status of the QA report
     * @param requestingUser the user requesting the change
     * @return the updated QA report
     */
    fun setQaReportStatus(
        qaReportId: String,
        dataId: String,
        dataType: String,
        statusToSet: Boolean,
        requestingUser: DatalandAuthentication,
    ): QaReportMetaInformation {
        val storedQaReportEntity = getQaReportEntityById(dataId, dataType, qaReportId)
        if (!qaReportSecurityPolicy.canUserSetQaReportStatus(storedQaReportEntity, requestingUser)) {
            throw InsufficientRightsApiException(
                "Missing required access rights",
                "You do not have the required access rights to update QA report with the id: $qaReportId",
            )
        }
        logger.info("Setting report with ID $qaReportId to active=$statusToSet")
        setQaReportStatusInt(storedQaReportEntity, statusToSet)

        return qaReportRepository.save(storedQaReportEntity).toMetaInformationApiModel()
    }

    protected abstract fun setQaReportStatusInt(
        qaReportEntity: QaReportEntity,
        statusToSet: Boolean,
    ): QaReportEntity

    protected abstract fun <ReportType> qaReportEntityToModel(
        qaReportEntity: QaReportEntity,
        objectMapper: ObjectMapper,
        clazz: Class<ReportType>,
    ): QaReportWithMetaInformation<ReportType>

    /**
     * Method to make the QA report manager get meta info about one specific QA report
     * @param qaReportId filters the requested meta info to one specific QA report ID
     * @return meta info about QA report behind the qaReportId
     */
    fun <ReportType> getQaReportById(
        dataId: String,
        dataType: String,
        qaReportId: String,
        objectMapper: ObjectMapper,
        clazz: Class<ReportType>,
    ): QaReportWithMetaInformation<ReportType> =
        qaReportEntityToModel(
            getQaReportEntityById(dataId, dataType, qaReportId),
            objectMapper,
            clazz,
        )

    /**
     * Method to make the QA report manager get all meta infos associated with a data set
     * @param dataId filters the requested meta info to one specific data ID
     * @return a list of meta info about QA reports associated to the data set
     */
    fun <ReportType> searchQaReportMetaInfo(
        dataId: String,
        dataType: String,
        showInactive: Boolean,
        reporterUserId: String?,
        objectMapper: ObjectMapper,
        clazz: Class<ReportType>,
    ): List<QaReportWithMetaInformation<ReportType>> {
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

        return searchResults.map { qaReportEntityToModel(it, objectMapper, clazz) }
    }
}

package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.DataPointMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataPointToValidate
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandqaservice.model.reports.QaReportDataPoint
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.DataPointQaReport
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointQaReportRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DataPointQaReviewManager.ReviewDataPointTask
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.IdUtils
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * A service class for managing QA reports for data points
 */
@Service
class DataPointQaReportManager(
    @Autowired private val dataPointControllerApi: DataPointControllerApi,
    @Autowired private val qaReportRepository: DataPointQaReportRepository,
    @Autowired private val dataPointQaReviewManager: DataPointQaReviewManager,
    @Autowired private val qaReportSecurityPolicy: QaReportSecurityPolicy,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun ensureDatalandDataPointExists(dataPointId: String): DataPointMetaInformation {
        try {
            return dataPointControllerApi.getDataPointMetaInfo(dataPointId)
        } catch (ex: ClientException) {
            val exceptionToThrow =
                if (ex.statusCode == HttpStatus.NOT_FOUND.value()) {
                    ResourceNotFoundApiException(
                        "Data Point '$dataPointId' not found",
                        "No data point with the id: $dataPointId could be found.",
                        ex,
                    )
                } else {
                    ex
                }
            throw exceptionToThrow
        }
    }

    private fun ensureQaReportConformsToSpecification(
        report: QaReportDataPoint<String?>,
        dataPointMetaInfo: DataPointMetaInformation,
    ) {
        if (report.correctedData == null) {
            return
        }

        try {
            dataPointControllerApi.validateDataPoint(
                DataPointToValidate(
                    dataPoint = report.correctedData,
                    dataPointType = dataPointMetaInfo.dataPointType,
                ),
            )
        } catch (ex: ClientException) {
            val exceptionToThrow =
                if (ex.statusCode == HttpStatus.BAD_REQUEST.value()) {
                    InvalidInputApiException(
                        "Provided data point verdict does not conform to data point specification",
                        "The provided data point verdict does not conform to the data point specification for " +
                            "'${dataPointMetaInfo.dataPointType}'",
                        ex,
                    )
                } else {
                    ex
                }
            throw exceptionToThrow
        }
    }

    /**
     * Creates a new QA report. If the verdict is ACCEPTED or REJECTED, the data point will be reviewed as well.
     * @param report the QA report to be stored
     * @param dataPointId the ID of the data point the QA report is associated with
     * @param reporterUserId the ID of the user who uploaded the QA report
     * @param uploadTime the time the QA report was uploaded
     * @return the created QA report
     */
    @Transactional
    fun createQaReport(
        report: QaReportDataPoint<String?>,
        dataPointId: String,
        reporterUserId: String,
        uploadTime: Long,
        correlationId: String,
    ): DataPointQaReport {
        val dataPointMetaInfo = ensureDatalandDataPointExists(dataPointId)
        ensureQaReportConformsToSpecification(report, dataPointMetaInfo)
        qaReportRepository.markAllReportsInactiveByDataPointIdAndReportingUserId(dataPointId, reporterUserId)

        val mappedQaStatus = report.verdict.toQaStatus()
        if (mappedQaStatus != null) {
            dataPointQaReviewManager.reviewDataPoints(
                listOf(
                    ReviewDataPointTask(
                        dataPointId = dataPointId,
                        qaStatus = mappedQaStatus,
                        triggeringUserId = reporterUserId,
                        comment = report.comment,
                        correlationId = correlationId,
                        timestamp = uploadTime,
                    ),
                ),
            )
        }

        return qaReportRepository
            .save(
                DataPointQaReportEntity(
                    qaReportId = IdUtils.generateUUID(),
                    dataPointId = dataPointId,
                    dataPointType = dataPointMetaInfo.dataPointType,
                    reporterUserId = reporterUserId,
                    uploadTime = uploadTime,
                    active = true,
                    comment = report.comment,
                    verdict = report.verdict,
                    correctedData = report.correctedData,
                ),
            ).toApiModel()
    }

    /**
     * Method to set the status of a QA report
     * @param qaReportId the ID of the QA report to be updated
     * @param dataPointId the ID of the data set the QA report is associated with
     * @param statusToSet the new status of the QA report
     * @param requestingUser the user requesting the change
     * @return the updated QA report
     */
    fun setQaReportStatus(
        qaReportId: String,
        dataPointId: String,
        statusToSet: Boolean,
        requestingUser: DatalandAuthentication,
    ): DataPointQaReport {
        val storedQaReportEntity = getQaReportEntity(dataPointId, qaReportId)
        if (!qaReportSecurityPolicy.canUserSetQaReportStatus(storedQaReportEntity, requestingUser)) {
            throw InsufficientRightsApiException(
                "Required access rights missing",
                "You do not have the required access rights to update QA report with the id: $qaReportId",
            )
        }
        logger.info("Setting report with ID $qaReportId to active=$statusToSet")
        storedQaReportEntity.active = statusToSet

        return qaReportRepository.save(storedQaReportEntity).toApiModel()
    }

    private fun getQaReportEntity(
        dataPointId: String,
        qaReportId: String,
    ): DataPointQaReportEntity {
        val dataEntity =
            qaReportRepository.findById(qaReportId).orElseThrow {
                ResourceNotFoundApiException(
                    "QA report not found",
                    "No QA report with the id: $qaReportId could be found.",
                )
            }
        if (dataEntity.dataPointId != dataPointId) {
            throw InvalidInputApiException(
                "QA report '$qaReportId' not associated with data '$dataPointId'",
                "The requested Qa Report '$qaReportId' is not associated with data '$dataPointId'," +
                    " but with data '${dataEntity.dataPointId}'.",
            )
        }

        return dataEntity
    }

    /**
     * Method to make the QA report manager get meta info about one specific QA report
     * @param qaReportId filters the requested meta info to one specific QA report ID
     * @return meta info about QA report behind the qaReportId
     */
    fun getQaReportById(
        dataPointId: String,
        qaReportId: String,
    ): DataPointQaReport = getQaReportEntity(dataPointId, qaReportId).toApiModel()

    /**
     * Method to make the QA report manager get all meta infos associated with a data set
     * @param dataPointId filters the requested meta info to one specific data ID
     * @return a list of meta info about QA reports associated to the data set
     */
    fun searchQaReportMetaInfo(
        dataPointId: String,
        showInactive: Boolean,
        reporterUserId: String?,
    ): List<DataPointQaReport> =
        qaReportRepository
            .searchQaReportMetaInformation(
                dataPointId = dataPointId,
                reporterUserId = reporterUserId,
                showInactive = showInactive,
            ).map { it.toApiModel() }
}

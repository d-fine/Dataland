package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportWithMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.QaReportRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.IdUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * A service class for managing QA report meta-information
 */
@Service
class QaReportManager(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired qaReportRepository: QaReportRepository,
    @Autowired qaReportSecurityPolicy: QaReportSecurityPolicy,
    @Autowired private val datalandBackendAccessor: DatalandBackendAccessor,
) : DatasetQaReportService(qaReportRepository, qaReportSecurityPolicy) {
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
    override fun <QaReportType> createQaReport(
        report: QaReportType,
        dataId: String,
        dataType: String,
        reporterUserId: String,
        uploadTime: Long,
    ): QaReportMetaInformation {
        datalandBackendAccessor.ensureDatalandDataExists(dataId, dataType)
        qaReportRepository.markAllReportsInactiveByDataIdAndReportingUserId(dataId, reporterUserId)
        return qaReportRepository
            .save(
                QaReportEntity(
                    qaReportId = IdUtils.generateUUID(),
                    qaReport = objectMapper.writeValueAsString(report),
                    dataId = dataId,
                    dataType = dataType,
                    reporterUserId = reporterUserId,
                    uploadTime = uploadTime,
                    active = true,
                ),
            ).toMetaInformationApiModel()
    }

    override fun setQaReportStatusInt(
        qaReportEntity: QaReportEntity,
        statusToSet: Boolean,
    ): QaReportEntity {
        qaReportEntity.active = statusToSet
        return qaReportEntity
    }

    override fun <ReportType> qaReportEntityToModel(
        qaReportEntity: QaReportEntity,
        objectMapper: ObjectMapper,
        clazz: Class<ReportType>,
    ): QaReportWithMetaInformation<ReportType> {
        val report = objectMapper.readValue(qaReportEntity.qaReport, clazz)
        return QaReportWithMetaInformation(
            metaInfo = qaReportEntity.toMetaInformationApiModel(),
            report = report,
        )
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
}

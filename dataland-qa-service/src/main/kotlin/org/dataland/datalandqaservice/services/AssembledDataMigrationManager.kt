package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import org.dataland.datalandqaservice.model.reports.QaReportDataPoint
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointQaReportRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.QaReportRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.IdUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * A service class for managing the migration of stored QA reports to assembled QA reports.
 */
@Service
class AssembledDataMigrationManager
    @Autowired
    constructor(
        private val assembledQaReportManager: AssembledDatasetQaReportManager,
        private val qaReportRepository: QaReportRepository,
        private val dataPointQaReportRepository: DataPointQaReportRepository,
        private val objectMapper: ObjectMapper,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Migrates all stored QA reports for a data set to assembled QA reports.
         */
        @Transactional
        fun migrateStoredDatasetToAssembledDataset(
            dataId: String,
            correlationId: String,
        ) {
            logger.info("Migrating QA Reports for dataId: $dataId to assembled dataset (correlationId: $correlationId)")
            val allQaReports =
                qaReportRepository.searchQaReportMetaInformation(
                    dataId = dataId,
                    showInactive = true,
                )
            allQaReports.forEach {
                // The qaReport string contains a json array when it has been migrated (i.e., is assembled)
                val isQaReportJsonArray = it.qaReport.startsWith("[")
                val isQaReportStored = !isQaReportJsonArray
                if (isQaReportStored) {
                    logger.info("Migrating stored QA Report with id: ${it.qaReportId} to assembled QA Report")
                    migrateStoredQaReportToAssembledQaReport(it, correlationId)
                } else {
                    logger.info(
                        "Not migrating stored QA Report with id: ${it.qaReportId} to assembled" +
                            "QA Report as it is already in the correct format",
                    )
                }
            }
        }

        private fun migrateStoredQaReportToAssembledQaReport(
            qaReport: QaReportEntity,
            correlationId: String,
        ) {
            val datapointReportIds = dehydrateAndSaveDataPointQaReports(qaReport, correlationId)
            qaReport.qaReport = objectMapper.writeValueAsString(datapointReportIds)
        }

        private fun dehydrateAndSaveDataPointQaReports(
            qaReport: QaReportEntity,
            correlationId: String,
        ): MutableList<String> {
            val report = objectMapper.readTree(qaReport.qaReport) as ObjectNode
            val (associatedDataPoints, decomposedQaReport) =
                assembledQaReportManager.splitQaReportIntoDataPoints(
                    qaReport.dataType, qaReport.dataId, report,
                )

            val unwantedAdditionalQaFeatures = decomposedQaReport.keys - associatedDataPoints.keys
            if (unwantedAdditionalQaFeatures.isNotEmpty()) {
                logger.warn(
                    "The QA report (${qaReport.qaReportId}) contains the following datapoints," +
                        " that were not part of the original data set: $unwantedAdditionalQaFeatures " +
                        " they will be ignored during the migration (correlationId: $correlationId)",
                )
            }

            val dataPointQaReportIds = mutableListOf<String>()
            for ((dataPointType, dataPointReport) in decomposedQaReport) {
                val dataPointId = associatedDataPoints[dataPointType] ?: continue
                val parsedQaReport = objectMapper.treeToValue<QaReportDataPoint<Any?>>(dataPointReport.content)
                val translatedQaReport =
                    QaReportDataPoint<String?>(
                        comment = parsedQaReport.comment,
                        verdict = parsedQaReport.verdict,
                        correctedData = objectMapper.writeValueAsString(parsedQaReport.correctedData),
                    )
                val savedQaReport =
                    dataPointQaReportRepository
                        .save(
                            DataPointQaReportEntity(
                                qaReportId = IdUtils.generateUUID(),
                                dataPointId = dataPointId,
                                dataPointType = dataPointType,
                                reporterUserId = qaReport.reporterUserId,
                                uploadTime = qaReport.uploadTime,
                                active = qaReport.active,
                                comment = translatedQaReport.comment,
                                verdict = translatedQaReport.verdict,
                                correctedData = translatedQaReport.correctedData,
                            ),
                        )
                dataPointQaReportIds.add(savedQaReport.qaReportId)
            }
            return dataPointQaReportIds
        }
    }

package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.treeToValue
import jakarta.transaction.Transactional
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.JsonSpecificationUtils
import org.dataland.datalandqaservice.model.reports.QaReportDataPoint
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportWithMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointQaReportRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.QaReportRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.IdUtils.generateUUID
import org.dataland.datalandspecificationservice.openApiClient.api.SpecificationControllerApi
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * A service class for managing QA reports for assembled datasets
 */
@Service
@Suppress("LongParameterList")
class AssembledDatasetQaReportManager
    @Autowired
    constructor(
        private val objectMapper: ObjectMapper,
        override val qaReportRepository: QaReportRepository,
        override val qaReportSecurityPolicy: QaReportSecurityPolicy,
        private val datalandBackendAccessor: DatalandBackendAccessor,
        private val specificationControllerApi: SpecificationControllerApi,
        private val dataPointCompositionService: DataPointCompositionService,
        private val dataPointQaReportManager: DataPointQaReportManager,
        private val dataPointQaReportRepository: DataPointQaReportRepository,
    ) : DatasetQaReportService(qaReportRepository = qaReportRepository, qaReportSecurityPolicy = qaReportSecurityPolicy) {
        override val logger = LoggerFactory.getLogger(javaClass)

        @Transactional
        override fun <QaReportType> createQaReport(
            report: QaReportType,
            dataId: String,
            dataType: String,
            reporterUserId: String,
            uploadTime: Long,
        ): QaReportMetaInformation {
            val correlationId = generateUUID()

            datalandBackendAccessor.ensureDatalandDataExists(dataId, dataType)
            val dataPointQaReportIds =
                dehydrateAndSaveDataPointQaReports(dataType, dataId, report, reporterUserId, uploadTime, correlationId)

            qaReportRepository.markAllReportsInactiveByDataIdAndReportingUserId(dataId, reporterUserId)
            val savedEntity =
                qaReportRepository.save(
                    QaReportEntity(
                        qaReportId = generateUUID(),
                        qaReport = objectMapper.writeValueAsString(dataPointQaReportIds),
                        dataId = dataId,
                        dataType = dataType,
                        reporterUserId = reporterUserId,
                        uploadTime = uploadTime,
                        active = true,
                    ),
                )
            return savedEntity.toMetaInformationApiModel()
        }

        private fun <QaReportType> dehydrateAndSaveDataPointQaReports(
            dataType: String,
            dataId: String,
            report: QaReportType,
            reporterUserId: String,
            uploadTime: Long,
            correlationId: String,
        ): MutableList<String> {
            val specification = getFrameworkSpecification(dataType)

            val associatedDataPoints =
                dataPointCompositionService.getCompositionOfDataset(dataId) ?: throw
                    IllegalStateException("The dataset with id $dataId is not a composition of data points")
            val decomposedQaReport =
                JsonSpecificationUtils.dehydrateJsonSpecification(
                    specification, objectMapper.valueToTree(report),
                )

            val unwantedAdditionalQaFeatures = decomposedQaReport.keys - associatedDataPoints.keys
            if (unwantedAdditionalQaFeatures.isNotEmpty()) {
                throw InvalidInputApiException(
                    "The QA report contains to many datapoints",
                    "The QA report contains the following datapoints," +
                        " that were not part of the original data set: $unwantedAdditionalQaFeatures",
                )
            }

            val dataPointQaReportIds = mutableListOf<String>()
            for ((dataPointId, dataPointReport) in decomposedQaReport) {
                val dataPointDataId = requireNotNull(associatedDataPoints[dataPointId])
                val parsedQaReport = objectMapper.treeToValue<QaReportDataPoint<Any?>>(dataPointReport.content)
                val translatedQaReport =
                    QaReportDataPoint<String?>(
                        comment = parsedQaReport.comment,
                        verdict = parsedQaReport.verdict,
                        correctedData = objectMapper.writeValueAsString(parsedQaReport.correctedData),
                    )

                dataPointQaReportIds.add(
                    dataPointQaReportManager
                        .createQaReport(
                            report = translatedQaReport,
                            dataId = dataPointDataId,
                            reporterUserId = reporterUserId,
                            uploadTime = uploadTime,
                            correlationId = correlationId,
                        ).qaReportId,
                )
            }
            return dataPointQaReportIds
        }

        private fun getFrameworkSpecification(dataType: String): ObjectNode {
            val specification =
                objectMapper.readTree(
                    specificationControllerApi.getFrameworkSpecification(dataType).schema,
                ) as ObjectNode
            return specification
        }

        @Transactional
        override fun setQaReportStatusInt(
            qaReportEntity: QaReportEntity,
            statusToSet: Boolean,
        ): QaReportEntity {
            qaReportEntity.active = statusToSet
            val dataPointQaReportIdList = objectMapper.readValue<List<String>>(qaReportEntity.qaReport)
            dataPointQaReportRepository.findAllById(dataPointQaReportIdList).forEach {
                it.active = statusToSet
            }
            return qaReportEntity
        }

        override fun <ReportType> qaReportEntityToModel(
            qaReportEntity: QaReportEntity,
            objectMapper: ObjectMapper,
            clazz: Class<ReportType>,
        ): QaReportWithMetaInformation<ReportType> {
            val dataPointQaReportIdList = objectMapper.readValue<List<String>>(qaReportEntity.qaReport)
            val dataPointReports =
                dataPointQaReportRepository.findAllById(dataPointQaReportIdList).associate {
                    it.dataPointType to objectMapper.valueToTree<JsonNode>(it.toDatasetApiModel(objectMapper))
                }
            val hydratedQaReport =
                JsonSpecificationUtils.hydrateJsonSpecification(
                    getFrameworkSpecification(qaReportEntity.dataType),
                ) { dataPointReports[it] }

            return QaReportWithMetaInformation(
                report = objectMapper.treeToValue(hydratedQaReport, clazz),
                metaInfo = qaReportEntity.toMetaInformationApiModel(),
            )
        }
    }

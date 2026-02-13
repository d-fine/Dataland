package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataPointToValidate
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointQaReportRepository
import org.dataland.datalandspecificationservice.openApiClient.api.SpecificationControllerApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service to support data point review operations.
 */
@Service
class DatasetReviewSupportService
    @Autowired
    constructor(
        private val dataPointControllerApi: DataPointControllerApi,
        private val metaDataControllerApi: MetaDataControllerApi,
        private val specificationControllerApi: SpecificationControllerApi,
        private val dataPointQaReportRepository: DataPointQaReportRepository,
    ) {
        /**
         * Retrieves meta data of a dataset
         */
        fun getDataMetaInfo(datasetId: String): DataMetaInformation = metaDataControllerApi.getDataMetaInfo(datasetId)

        /**  Retrieves the data points contained in a dataset.
         */
        fun getContainedDataPoints(datasetId: String): Map<String, String> = metaDataControllerApi.getContainedDataPoints(datasetId)

        /**  Retrieves the data point type for a given data point.
         */
        fun getDataPointType(dataPointId: UUID): String = dataPointControllerApi.getDataPointMetaInfo(dataPointId.toString()).dataPointType

        /**  Validates a custom data point.
         */
        fun validateCustomDataPoint(
            dataPoint: String,
            dataPointType: String,
        ) {
            dataPointControllerApi.validateDataPoint(DataPointToValidate(dataPoint, dataPointType))
        }

        /**  Retrieves the frameworks that use a given data point type.
         */
        fun getFrameworksForDataPointType(dataPointType: String): List<String> =
            specificationControllerApi.getDataPointTypeSpecification(dataPointType).usedBy.map { it.id }

        /** Finds QA report IDs for given data point IDs.
         */
        fun findQaReportIdsForDataPoints(dataPointIds: List<String>): List<String> =
            dataPointQaReportRepository
                .searchQaReportMetaInformation(
                    dataPointIds = dataPointIds,
                    showInactive = false,
                    reporterUserId = null,
                ).map { it.qaReportId }

        /** Finds the data point type using a QA report ID.
         */
        fun findDataPointTypeUsingQaReportId(qaReportId: UUID): String =
            dataPointQaReportRepository.findDataPointTypeUsingId(qaReportId.toString())
    }

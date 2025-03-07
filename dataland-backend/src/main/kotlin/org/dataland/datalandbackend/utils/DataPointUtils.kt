package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.utils.JsonSpecificationUtils
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.infrastructure.ClientException
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * A utility class for working with data point specifications and data point metadata
 */

@Service("DataPointUtils")
class DataPointUtils
    @Autowired
    constructor(
        private val objectMapper: ObjectMapper,
        private val specificationClient: SpecificationControllerApi,
        private val metaDataManager: DataPointMetaInformationManager,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Retrieve a framework specification from the specification service
         * @param framework the name of the framework to retrieve the specification for
         * @return the FrameworkSpecification object
         * @throws InvalidInputApiException if the framework is not found
         */
        fun getFrameworkSpecification(framework: String): FrameworkSpecification =
            try {
                specificationClient.getFrameworkSpecification(framework)
            } catch (clientException: ClientException) {
                logger.error("Expected framework specification for $framework not found: ${clientException.message}.")
                throw InvalidInputApiException(
                    "Framework $framework not found.",
                    "The specified framework $framework is not known to the specification service.",
                )
            }

        /**
         * Retrieve a framework specification from the specification service
         * @param framework the name of the framework to retrieve the specification for
         * @return the FrameworkSpecification object or null if the framework is not found
         */
        fun getFrameworkSpecificationOrNull(framework: String): FrameworkSpecification? =
            try {
                specificationClient.getFrameworkSpecification(framework)
            } catch (ignore: ClientException) {
                null
            }

        /**
         * Retrieves the relevant data point types for a specific framework
         * @param framework the name of the framework
         * @return a set of all relevant data point types
         */
        fun getRelevantDataPointTypes(framework: String): Set<String> {
            val frameworkSpecification = getFrameworkSpecification(framework)
            val frameworkTemplate = objectMapper.readTree(frameworkSpecification.schema) as ObjectNode
            return JsonSpecificationUtils.dehydrateJsonSpecification(frameworkTemplate, frameworkTemplate).keys
        }

        /**
         * Retrieves the latest upload time of an active data point belonging to a given framework and a specific company
         * @param dataPointDimensions the data point dimensions to get the latest upload time for
         * @return the latest upload time of an active data point as a long
         */
        fun getLatestUploadTime(dataPointDimensions: BasicDataDimensions): Long =
            metaDataManager.getLatestUploadTimeOfActiveDataPoints(
                dataPointTypes = getRelevantDataPointTypes(dataPointDimensions.dataType),
                companyId = dataPointDimensions.companyId,
                reportingPeriod = dataPointDimensions.reportingPeriod,
            )

        /**
         * Retrieves all reporting periods with at least on active data point for a specific company and framework
         * @param companyId the ID of the company
         * @param framework the name of the framework
         * @return a set of all reporting periods with active data points or an empty set if none exist
         */
        fun getAllReportingPeriodsWithActiveDataPoints(
            companyId: String,
            framework: String,
        ): Set<String> =
            if (getFrameworkSpecificationOrNull(framework) == null) {
                emptySet()
            } else {
                metaDataManager.getReportingPeriodsWithActiveDataPoints(
                    dataPointTypes = getRelevantDataPointTypes(framework),
                    companyId = companyId,
                )
            }
    }

package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackend.model.DataDimensionFilter
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities.Companion.REFERENCED_REPORTS_ID
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
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
        private val referencedReportsUtilities: ReferencedReportsUtilities,
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

        /**
         * Retrieves all active data dimensions in regard to data points given the filter parameters
         * @param dataDimensionFilter the filter parameters for the data dimensions
         * @return a list of all active data dimensions
         */
        fun getActiveDataDimensionsFromDataPoints(dataDimensionFilter: DataDimensionFilter): List<BasicDataDimensions> {
            val dataPointMetaInformationEntities =
                metaDataManager.getActiveDataPointMetaInformationList(dataDimensionFilter)
            val dataPointBasedDimensions = dataPointMetaInformationEntities.map { it.toBasicDataDimensions() }
            val frameworkBasedDimensions = getAllActiveDataDimensionsForFrameworks(dataDimensionFilter)
            return (dataPointBasedDimensions + frameworkBasedDimensions).distinct()
        }

        private fun getAllActiveDataDimensionsForFrameworks(dataDimensionFilter: DataDimensionFilter): List<BasicDataDimensions> {
            val allRelevantDimensions = mutableListOf<BasicDataDimensions>()
            val allAssembledFrameworks = specificationClient.listFrameworkSpecifications().map { it.framework.id }
            val frameworks =
                dataDimensionFilter.dataTypesOrDataPointTypes?.filter { allAssembledFrameworks.contains(it) } ?: emptyList()
            for (framework in frameworks) {
                val activeDataPointMetaInformation =
                    metaDataManager.getActiveDataPointMetaInformationList(
                        DataDimensionFilter(
                            companyIds = dataDimensionFilter.companyIds,
                            dataTypesOrDataPointTypes = getRelevantDataPointTypes(framework).toList(),
                            reportingPeriods = dataDimensionFilter.reportingPeriods,
                        ),
                    )
                allRelevantDimensions.addAll(activeDataPointMetaInformation.map { it.toBasicDataDimensions(framework) })
            }
            return allRelevantDimensions.distinct()
        }

        /**
         * Return the template of a specific framework
         * @param framework the framework for which the template shall be returned
         */
        fun getFrameworkTemplate(framework: String): JsonNode {
            val frameworkSpecification = getFrameworkSpecification(framework)
            val frameworkTemplate = objectMapper.readTree(frameworkSpecification.schema)
            referencedReportsUtilities
                .insertReferencedReportsIntoFrameworkSchema(
                    frameworkTemplate,
                    frameworkSpecification.referencedReportJsonPath,
                )
            return frameworkTemplate
        }

        /**
         * Get basic data point dimensions (companyId, dataPointType, reportingPeriod) for given
         * data dimensions (companyId, framework, reportingPeriod).
         * @param dataDimensionsSet a set of data dimensions
         * @return
         */
        fun getBasicDataPointDimensionsForDataDimensions(
            dataDimensionsSet: Set<BasicDataDimensions>,
            correlationId: String,
        ): Map<BasicDataDimensions, List<BasicDataPointDimensions>> {
            logger.info("Request data point dimensions for a set of data dimension objects. Correlation ID: $correlationId")
            val dataDimensionsByFramework = dataDimensionsSet.groupBy { it.dataType }
            val result = mutableMapOf<BasicDataDimensions, List<BasicDataPointDimensions>>()

            dataDimensionsByFramework.entries.forEach { (framework, frameworkSpecificDataDimensions) ->
                val relevantDataPointTypes = getRelevantDataPointTypes(framework)
                result.putAll(
                    frameworkSpecificDataDimensions.associateWith { dataDimension ->
                        relevantDataPointTypes.map { dataPointType -> dataDimension.toBasicDataPointDimensions(dataPointType) }
                    },
                )
            }

            return result
        }

        /**
         * Assemble a single data set using the provided list of uploaded data points and the framework template
         * @param dataPoints the data points of the data set as retrieved from the internal storage
         * @param frameworkTemplate the framework template to be used as a basis
         */
        fun assembleSingleDataSet(
            dataPoints: Collection<UploadedDataPoint>,
            frameworkTemplate: JsonNode,
        ): String {
            val referencedReports = mutableMapOf<String, CompanyReport>()
            val allDataPoints =
                dataPoints
                    .associate {
                        it.dataPointType to objectMapper.readTree(it.dataPoint)
                    }.toMutableMap()

            dataPoints.forEach {
                val companyReports = mutableListOf<CompanyReport>()
                referencedReportsUtilities.getAllCompanyReportsFromDataSource(it.dataPoint, companyReports)
                companyReports.forEach { companyReport ->
                    referencedReports[companyReport.fileName ?: companyReport.fileReference] = companyReport
                }
            }
            allDataPoints[REFERENCED_REPORTS_ID] = objectMapper.valueToTree(referencedReports)

            val datasetAsJsonNode =
                JsonSpecificationUtils.hydrateJsonSpecification(frameworkTemplate as ObjectNode) {
                    allDataPoints[it]
                }

            return datasetAsJsonNode.toString()
        }
    }

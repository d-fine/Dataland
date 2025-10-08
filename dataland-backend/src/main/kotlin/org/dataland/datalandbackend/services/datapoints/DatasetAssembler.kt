package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities.Companion.REFERENCED_REPORTS_ID
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.JsonSpecificationUtils
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.infrastructure.ClientException
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper as objectMapper

/**
 * A utility class for assembling datasets from data points
 */

@Service("DatasetAssembler")
class DatasetAssembler
    @Autowired
    constructor(
        private val specificationClient: SpecificationControllerApi,
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
         * Assemble a single dataset of type [framework] using the provided list of uploaded [dataPoints]
         * @param dataPoints the data points of the dataset as retrieved from the internal storage
         * @param framework the framework the data points are to be assembled into
         */
        fun assembleSingleDataset(
            dataPoints: Collection<UploadedDataPoint>,
            framework: String,
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
                JsonSpecificationUtils.hydrateJsonSpecification(getFrameworkTemplate(framework) as ObjectNode) {
                    allDataPoints[it]
                }

            return datasetAsJsonNode.toString()
        }
    }

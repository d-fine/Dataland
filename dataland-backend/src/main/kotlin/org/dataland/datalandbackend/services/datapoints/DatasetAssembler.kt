package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.services.SpecificationService
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities.Companion.REFERENCED_REPORTS_ID
import org.dataland.datalandbackendutils.utils.JsonSpecificationUtils
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
        private val specificationService: SpecificationService,
        private val referencedReportsUtilities: ReferencedReportsUtilities,
    ) {
        /**
         * Return the template of a specific framework
         * @param framework the framework for which the template shall be returned
         */
        fun getFrameworkTemplate(framework: String): JsonNode {
            val frameworkSpecification = specificationService.getFrameworkSpecification(framework)
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

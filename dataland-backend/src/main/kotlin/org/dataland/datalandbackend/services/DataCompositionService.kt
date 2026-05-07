package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.utils.JsonSpecificationUtils
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.specificationservice.openApiClient.model.CalculationRule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper as objectMapper

// ToDo find better way of sharing typealiases
typealias DataPointType = String
typealias DataPointId = String

/**
 * Service to determine the category of a given data type string, relevant constituents of datasets and similar tasks
 */
@Service("DataCompositionService")
class DataCompositionService
    @Autowired
    constructor(
        private val specificationService: SpecificationService,
    ) {
        /**
         * Retrieves all relevant data point types for a given data type
         * @param dataType the name of the data type (either a framework or a data point)
         * @return a set of all relevant data point types
         * @throws InvalidInputApiException if the data type is not known
         */
        fun getRelevantDataPointTypes(dataType: String): Collection<DataPointType> =
            when {
                specificationService.isDataPointType(dataType) -> setOf(dataType)
                specificationService.isAssembledFramework(dataType) -> getContainedDataPointTypes(dataType)
                else -> {
                    throw InvalidInputApiException(
                        "DataType $dataType not found.",
                        "The specified dataType $dataType is not known to the specification service.",
                    )
                }
            }

        /**
         * Retrieves all data point types contained in an assembled framework
         * @param framework the name of the framework for which the data point type composition is requested
         * @return a set of all relevant data point types
         */
        private fun getContainedDataPointTypes(framework: String): Collection<DataPointType> {
            val frameworkSpecification = specificationService.getFrameworkSpecification(framework)
            val frameworkTemplate = objectMapper.readTree(frameworkSpecification.schema) as ObjectNode
            return JsonSpecificationUtils.dehydrateJsonSpecification(frameworkTemplate, frameworkTemplate).keys
        }

        /**
         * Filters out invalid data dimensions by checking if the company ID, framework, and reporting period are valid.
         * @param datasetDimensions the list of data dimensions to filter
         * @return the list of all valid data dimensions from the original input
         */
        fun filterOutInvalidDatasetDimensions(datasetDimensions: List<BasicDatasetDimensions>) =
            datasetDimensions.filter { dimensions ->
                ValidationUtils.isBaseDimensions(dimensions.toBaseDimensions()) &&
                    specificationService.isFramework(dimensions.framework)
            }

        /**
         * Filters out invalid data point dimensions by checking if the company ID, data point type, and reporting period are valid.
         * @param dataDimensions the list of data point dimensions to filter
         * @return the list of all valid data point dimensions from the original input
         */
        fun filterOutInvalidDataPointDimensions(dataDimensions: List<BasicDataPointDimensions>) =
            dataDimensions.filter { dimensions ->
                ValidationUtils.isBaseDimensions(dimensions.toBaseDimensions()) &&
                    specificationService.isDataPointType(dimensions.dataPointType)
            }

        fun getAvailableCalculationRules(dataPointTypes: Collection<DataPointType>): Map<DataPointType, Collection<CalculationRule>> {
            val availableRules = mutableMapOf<DataPointType, Collection<CalculationRule>>()
            specificationService.getDataPointSpecifications(dataPointTypes.toList()).forEach { (dataPointType, specification) ->
                if (specification.calculationRules != null) {
                    availableRules[dataPointType] = specification.calculationRules
                }
            }
            return availableRules
        }
    }

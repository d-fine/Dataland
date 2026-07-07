package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackend.model.DataDimensionQuery
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.interfaces.DataPointDimensions
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.DataPointType
import org.dataland.datalandbackendutils.model.DatasetType
import org.dataland.datalandbackendutils.utils.JsonSpecificationUtils
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.specificationservice.openApiClient.model.CalculationRule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper as objectMapper

/**
 * Service to determine the category of a given data type string, relevant constituents of datasets and similar tasks
 */
@Service("DataCompositionService")
class DataCompositionService
    @Autowired
    constructor(
        private val specificationService: SpecificationService,
    ) {
        private val cachedRelevantDataPointTypes = ConcurrentHashMap<String, Set<DataPointType>>()

        /**
         * Retrieves all relevant data point types for a given data type
         *
         * @param dataType the name of the data type (either a framework or a data point)
         * @return a set of all relevant data point types
         * @throws InvalidInputApiException if the data type is not known
         */
        fun getRelevantDataPointTypes(dataType: String): Collection<DataPointType> =
            cachedRelevantDataPointTypes.computeIfAbsent(dataType) {
                when {
                    specificationService.isDataPointType(dataType) -> {
                        setOf(dataType)
                    }

                    specificationService.isAssembledFramework(dataType) -> {
                        getContainedDataPointTypes(dataType).toSet()
                    }

                    else -> {
                        throw InvalidInputApiException(
                            "DataType $dataType not found.",
                            "The specified dataType $dataType is not known to the specification service.",
                        )
                    }
                }
            }

        /**
         * Retrieves all data point types contained in an assembled framework
         *
         * @param framework the name of the framework for which the data point type composition is requested
         * @return a set of all relevant data point types
         */
        private fun getContainedDataPointTypes(framework: DatasetType): Collection<DataPointType> {
            val frameworkSpecification = specificationService.getFrameworkSpecification(framework)
            val frameworkTemplate = objectMapper.readTree(frameworkSpecification.schema) as ObjectNode
            return JsonSpecificationUtils.dehydrateJsonSpecification(frameworkTemplate, frameworkTemplate).keys
        }

        /**
         * Filters out invalid data dimensions by checking if the company ID, framework, and reporting period are valid.
         *
         * @param datasetDimensions the list of data dimensions to filter
         * @return the list of all valid data dimensions from the original input
         */
        fun filterOutInvalidDatasetDimensions(datasetDimensions: List<BasicDataDimensions>) =
            datasetDimensions
                .asSequence()
                .filter { dimensions ->
                    ValidationUtils.isBaseDimensions(dimensions) &&
                        specificationService.isFramework(dimensions.dataType)
                }.map { it.toBasicDatasetDimensions() }
                .toList()

        /**
         * Filters out invalid entries from the filter by checking if company IDs, frameworks, and reporting periods are valid.
         *
         * @param dataDimensionQuery the filter to remove invalid data from
         * @return a new filter without any invalid entries
         */
        fun filterOutInvalidDatasetEntries(dataDimensionQuery: DataDimensionQuery) =
            filterDimensionQuery(dataDimensionQuery) { specificationService.isFramework(it) }

        /**
         * Filters out invalid data point dimensions by checking if the company ID, data point type, and reporting period are valid.
         *
         * @param dataDimensions the list of data point dimensions to filter
         * @return the list of all valid data point dimensions from the original input
         */
        fun filterOutInvalidDataPointDimensions(dataDimensions: List<DataPointDimensions>) =
            dataDimensions.filter { dimensions ->
                ValidationUtils.isBaseDimensions(dimensions) &&
                    specificationService.isDataPointType(dimensions.dataPointType)
            }

        /**
         * Filters out invalid entries from the filter by checking if company IDs, data point types, and reporting periods are valid.
         *
         * @param dataDimensionQuery the filter to remove invalid data from
         * @return a new filter without any invalid entries
         */
        fun filterOutInvalidDataPointEntries(dataDimensionQuery: DataDimensionQuery) =
            filterDimensionQuery(dataDimensionQuery) { specificationService.isDataPointType(it) }

        /**
         * Filters a [DataDimensionQuery] by removing entries failing validation.
         *
         * The provided [dataTypeValidator] is used for the dataTypes field. If any field becomes empty after filtering
         * while it was non-empty originally, an empty query is returned to avoid unintended wildcard matches.
         *
         * @param dataDimensionQuery the query to filter
         * @param dataTypeValidator predicate to determine whether a data type string is valid
         * @return a filtered query, or an all-empty query if any dimension was entirely invalidated
         */
        private fun filterDimensionQuery(
            dataDimensionQuery: DataDimensionQuery,
            dataTypeValidator: (String) -> Boolean,
        ): DataDimensionQuery =
            DataDimensionQuery(
                companyIds = dataDimensionQuery.companyIds.filter { ValidationUtils.isUuid(it) },
                dataTypes = dataDimensionQuery.dataTypes.filter { dataTypeValidator(it) },
                reportingPeriods = dataDimensionQuery.reportingPeriods.filter { ValidationUtils.isReportingPeriod(it) },
            )

        /**
         * Checks whether filtering invalid values emptied at least one originally constrained query dimension.
         *
         * This is used to detect cases where filtering removed all values of a field that was explicitly provided,
         * which would otherwise broaden matching semantics if treated as an unconstrained filter.
         *
         * @param originalQuery the unfiltered query as provided by the caller
         * @param filteredQuery the query after invalid entries were removed
         * @return true if any non-empty original dimension became empty after filtering
         */
        public fun checkIfFilteringForValidFiltersCausedEmptyDataDimensionQuery(
            originalQuery: DataDimensionQuery,
            filteredQuery: DataDimensionQuery,
        ): Boolean {
            val companyIdsTurnedEmpty =
                originalQuery.companyIds.isNotEmpty() && filteredQuery.companyIds.isEmpty()
            val dataTypesTurnedEmpty =
                originalQuery.dataTypes.isNotEmpty() && filteredQuery.dataTypes.isEmpty()
            val reportingPeriodsTurnedEmpty =
                originalQuery.reportingPeriods.isNotEmpty() && filteredQuery.reportingPeriods.isEmpty()

            return (companyIdsTurnedEmpty || dataTypesTurnedEmpty || reportingPeriodsTurnedEmpty)
        }

        /**
         * Returns the calculation rules available for each of the given data point types.
         *
         * Types whose specification declares no calculation rules are omitted from the result.
         *
         * @param dataPointTypes the data point types to look up
         * @return a map from data point type to its declared calculation rules
         */
        fun getAvailableCalculationRules(dataPointTypes: List<DataPointType>): Map<DataPointType, List<CalculationRule>> =
            specificationService
                .getDataPointSpecifications(dataPointTypes)
                .mapValues { (_, specification) ->
                    specification.calculationRules
                }.filterValues { it.isNotEmpty() }
    }

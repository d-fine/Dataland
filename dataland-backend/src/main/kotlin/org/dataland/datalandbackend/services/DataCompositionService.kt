package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.repositories.DatasetDatapointRepository
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.BasicDataSetDimensions
import org.dataland.datalandbackendutils.utils.JsonSpecificationUtils
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.infrastructure.ClientException
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set
import kotlin.jvm.optionals.getOrNull

/**
 * Service to determine the category of a given data type string, relevant constituents of datasets and similar tasks
 */
@Service("DataCompositionService")
class DataCompositionService
    @Autowired
    constructor(
        private val objectMapper: ObjectMapper,
        private val datasetDatapointRepository: DatasetDatapointRepository,
        private val specificationControllerApi: SpecificationControllerApi,
    ) {
        // Variables to store known classifications since specifications do not change during runtime
        private val cachedDatapointTypes = ConcurrentHashMap<String, Boolean>()
        private val assembledFrameworks = mutableSetOf<String>()
        private val nonAssembledFrameworks = mutableSetOf<String>()

        companion object {
            const val TRIES_TO_WAIT_FOR_SPECIFICATION_SERVICE = 10
            const val WAIT_TIME_FOR_SPECIFICATION_SERVICE = 10_000L
        }

        private val logger = LoggerFactory.getLogger(javaClass)

        @EventListener
        @Suppress("unused") // This function is triggered via the event listener upon startup, detekt does not recognize this
        private fun initiateSpecifications(ignored: ContextRefreshedEvent?) {
            waitUntilSpecificationServiceReady()
            initializeSpecificationCache()
        }

        // ToDo: move this into a backend utils class and look into making the start up dependent
        private fun waitUntilSpecificationServiceReady() {
            Thread.sleep(WAIT_TIME_FOR_SPECIFICATION_SERVICE)
            for (attempt in 1..TRIES_TO_WAIT_FOR_SPECIFICATION_SERVICE) {
                try {
                    specificationControllerApi.listFrameworkSpecifications()
                    return
                } catch (ignore: Exception) {
                    logger.info(
                        "Specification service not ready yet. Attempt $attempt/$TRIES_TO_WAIT_FOR_SPECIFICATION_SERVICE. " +
                            "Waiting $WAIT_TIME_FOR_SPECIFICATION_SERVICE seconds.",
                    )
                    Thread.sleep(WAIT_TIME_FOR_SPECIFICATION_SERVICE)
                }
            }
            throw IllegalStateException("Specification service not ready after $TRIES_TO_WAIT_FOR_SPECIFICATION_SERVICE tries.")
        }

        /**
         * Set up cache of frameworks to reduce communication overhead since specifications do not change during runtime
         */
        private fun initializeSpecificationCache() {
            assembledFrameworks.addAll(specificationControllerApi.listFrameworkSpecifications().map { it.framework.id })
            nonAssembledFrameworks.addAll(DataType.values.map { it.toString() } - assembledFrameworks)
        }

        /**
         * Check if any given string represents an assembled framework
         * @param framework string to be checked
         */
        fun isAssembledFramework(framework: String): Boolean = assembledFrameworks.contains(framework)

        /**
         * Assert that the given framework is an assembled framework
         * @param framework the name of the framework to be checked
         * @throws InvalidInputApiException if the framework is not an assembled framework
         */
        fun assertFrameworkIsAssembled(framework: String) {
            if (!isAssembledFramework(framework)) {
                throw InvalidInputApiException(
                    "Framework $framework is not an assembled framework.",
                    "The specified framework $framework is not known to the specification service as an assembled framework.",
                )
            }
        }

        /**
         * Check if any given string represents a non-assembled framework
         * @param framework string to be checked
         */
        fun isNonAssembledFramework(framework: String): Boolean = nonAssembledFrameworks.contains(framework)

        fun isFramework(framework: String): Boolean = isAssembledFramework(framework) || isNonAssembledFramework(framework)

        /**
         * Checks if the given string is indeed a valid data point type
         * @param dataPointType the string to be checked
         */
        fun isDataPointType(dataPointType: String): Boolean {
            if (!cachedDatapointTypes.containsKey(dataPointType)) {
                try {
                    specificationControllerApi.getDataPointTypeSpecification(dataPointType)
                    cachedDatapointTypes[dataPointType] = true
                } catch (ignore: ClientException) {
                    cachedDatapointTypes[dataPointType] = false
                }
            }
            return cachedDatapointTypes[dataPointType]!!
        }

        /**
         * Retrieve a framework specification from the specification service
         * @param framework the name of the framework to retrieve the specification for
         * @return the FrameworkSpecification object
         * @throws InvalidInputApiException if the framework is not found
         */
        fun getFrameworkSpecification(framework: String): FrameworkSpecification =
            try {
                specificationControllerApi.getFrameworkSpecification(framework)
            } catch (clientException: ClientException) {
                logger.error("Expected framework specification for $framework not found: ${clientException.message}.")
                throw InvalidInputApiException(
                    "Framework $framework not found.",
                    "The specified framework $framework is not known to the specification service.",
                )
            }

        /**
         * Retrieves all relevant data point types for a given data type
         * @param dataType the name of the data type (either a framework or a data point)
         * @return a set of all relevant data point types
         * @throws InvalidInputApiException if the data type is not known
         */
        fun getRelevantDataPointTypes(dataType: String): Collection<String> =
            when {
                isDataPointType(dataType) -> setOf(dataType)
                isAssembledFramework(dataType) -> getContainedDataPointTypes(dataType)
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
        private fun getContainedDataPointTypes(framework: String): Collection<String> {
            val frameworkSpecification = getFrameworkSpecification(framework)
            val frameworkTemplate = objectMapper.readTree(frameworkSpecification.schema) as ObjectNode
            return JsonSpecificationUtils.dehydrateJsonSpecification(frameworkTemplate, frameworkTemplate).keys
        }

        /**
         * Retrieves the data point IDs of the constituents of a dataset given by a dataset ID
         * @param datasetId the ID of the dataset to retrieve the constituents for
         */
        fun getDataPointConstituents(datasetId: String): Collection<String> {
            val consistingDataPoints = datasetDatapointRepository.findById(datasetId).getOrNull()
            return consistingDataPoints?.dataPoints?.keys ?: emptyList()
        }

        /**
         * figure out if a data type is actually a known data typy
         */
        fun isDataType(input: String): Boolean = isDataPointType(input) || isFramework(input)

        /**
         * Filters out invalid data dimensions by checking if the company ID, data type, and reporting period are correctly formatted.
         * @param dataDimensions the list of data dimensions to filter
         * @return the list of all properly formatted data dimensions from the original list
         */
        fun filterOutInvalidDimensions(dataDimensions: List<BasicDataSetDimensions>) =
            dataDimensions.filter { dimensions ->
                ValidationUtils.isBaseDimensions(dimensions.toBaseDimensions()) && isFramework(dimensions.framework)
            }

        fun filterOutInvalidDimensions(dataDimensions: List<BasicDataPointDimensions>) =
            dataDimensions.filter { dimensions ->
                ValidationUtils.isBaseDimensions(dimensions.toBaseDimensions()) && isDataPointType(dimensions.dataPointType)
            }

        fun filterOutInvalidDimensions(dataDimensions: List<BasicDataDimensions>) =
            dataDimensions.filter { dimensions ->
                ValidationUtils.isBaseDimensions(dimensions.toBaseDimensions()) && isDataType(dimensions.dataType)
            }
    }

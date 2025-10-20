package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
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

/**
 * Service to make information of the dataland specification service available and evaluate related tasks e.g. determine the category of
 * a given data type string and retrieval of framework specifications
 */
@Service("SpecificationService")
class SpecificationService
    @Autowired
    constructor(
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

        /**
         * Initiates the specification cache after application start up. Waits until the specification service is reachable.
         */
        @EventListener
        fun initiateSpecifications(ignored: ContextRefreshedEvent?) {
            waitUntilSpecificationServiceReady()
            initializeSpecificationCache()
        }

        private fun waitUntilSpecificationServiceReady() {
            for (attempt in 1..TRIES_TO_WAIT_FOR_SPECIFICATION_SERVICE) {
                try {
                    specificationControllerApi.listFrameworkSpecifications()
                    return
                } catch (ignore: Exception) {
                    logger.info(
                        "Specification service not ready yet. Attempt $attempt/$TRIES_TO_WAIT_FOR_SPECIFICATION_SERVICE. " +
                            "Waiting $WAIT_TIME_FOR_SPECIFICATION_SERVICE milliseconds.",
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
         * Check if any given string represents a non-assembled framework
         * @param framework string to be checked
         */
        fun isNonAssembledFramework(framework: String): Boolean = nonAssembledFrameworks.contains(framework)

        /**
         * Check if any given string represents a framework (either assembled or non-assembled)
         * @param framework string to be checked
         */
        fun isFramework(framework: String): Boolean = isAssembledFramework(framework) || isNonAssembledFramework(framework)

        /**
         * Checks if a given string represents a data point type
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
            return cachedDatapointTypes.getOrDefault(dataPointType, false)
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
    }

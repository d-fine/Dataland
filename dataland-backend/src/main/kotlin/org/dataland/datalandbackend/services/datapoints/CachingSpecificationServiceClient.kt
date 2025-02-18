package org.dataland.datalandbackend.services.datapoints

import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.model.DataPointBaseTypeSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

/**
 * Service class for caching the specification service client
 */
@Service
class CachingSpecificationServiceClient
    @Autowired
    constructor(
        private val specificationClient: SpecificationControllerApi,
    ) {
        /**
         * Retrieve the framework specification for a given framework id
         */
        @Cacheable("frameworkSpecification")
        fun getFrameworkSpecification(frameworkId: String) = specificationClient.getFrameworkSpecification(frameworkId)

        /**
         * Retrieve the data point base type specification for a given data point base type id
         */
        @Cacheable("dataPointBaseTypeSpecification")
        fun getDataPointBaseType(dataPointBaseTypeId: String): DataPointBaseTypeSpecification =
            specificationClient.getDataPointBaseType(dataPointBaseTypeId)

        /**
         * Retrieve the data point specification for a given data point id
         */
        @Cacheable("dataPointSpecification")
        fun getDataPointTypeSpecification(dataPointId: String) = specificationClient.getDataPointTypeSpecification(dataPointId)
    }

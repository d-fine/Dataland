package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.services.DataCompositionService
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.DatasetType
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.infrastructure.ClientException
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * A utility class for working with data point specifications and data point metadata
 */

@Service("DataPointUtils")
class DataPointUtils
    @Autowired
    constructor(
        private val specificationClient: SpecificationControllerApi,
        private val metaDataManager: DataPointMetaInformationManager,
        private val dataCompositionService: DataCompositionService,
    ) {
        /**
         * Retrieve a framework specification from the specification service
         *
         * @param framework the name of the framework to retrieve the specification for
         * @return the FrameworkSpecification object or null if the framework is not found
         */
        fun getFrameworkSpecificationOrNull(framework: DatasetType): FrameworkSpecification? =
            try {
                specificationClient.getFrameworkSpecification(framework)
            } catch (ignore: ClientException) {
                null
            }

        /**
         * Retrieves the latest upload time of an active data point belonging to a given framework and a specific company
         *
         * @param dataPointDimensions the data point dimensions to get the latest upload time for
         * @return the latest upload time of an active data point as a long
         */
        fun getLatestUploadTime(dataPointDimensions: BasicDataDimensions): Long =
            metaDataManager.getLatestUploadTimeOfActiveDataPoints(
                dataPointTypes = dataCompositionService.getRelevantDataPointTypes(dataPointDimensions.dataType).toSet(),
                companyId = dataPointDimensions.companyId,
                reportingPeriod = dataPointDimensions.reportingPeriod,
            )
    }

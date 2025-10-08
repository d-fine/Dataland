package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackend.model.DataDimensionFilter
import org.dataland.datalandbackend.services.SpecificationService
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.utils.JsonSpecificationUtils
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
        private val objectMapper: ObjectMapper,
        private val specificationClient: SpecificationControllerApi,
        private val metaDataManager: DataPointMetaInformationManager,
        private val specificationService: SpecificationService,
    ) {
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
            val frameworkSpecification = specificationService.getFrameworkSpecification(framework)
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
         *
         * Generic data points (company "meta" information like "Number of Employees") are excluded to return only
         * reporting periods with actual data.
         *
         * @param companyId the ID of the company
         * @param framework the name of the framework
         * @return a set of all reporting periods with active data points or an empty set if none exist
         */
        fun getAllReportingPeriodsWithActiveDataPoints(
            companyId: String,
            framework: String,
        ): Set<String> {
            if (getFrameworkSpecificationOrNull(framework) == null) {
                return emptySet()
            }

            val relevantDataPoints = getRelevantDataPointTypes(framework).subtract(DataAvailabilityIgnoredFieldsUtils.getIgnoredFields())

            return metaDataManager
                .getActiveDataPointMetaInformation(
                    dataPointTypes = relevantDataPoints,
                    companyId = companyId,
                ).map { it.reportingPeriod }
                .toSet()
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

        /**
         * Retrieve all active framework-based data dimensions using the given DataDimensionFilter. If no framework is specified,
         * all frameworks are taken into account.
         * @param dataDimensionFilter the filter to use when searching for active data dimensions
         * @return a list of active framework data dimensions
         */
        private fun getAllActiveDataDimensionsForFrameworks(dataDimensionFilter: DataDimensionFilter): List<BasicDataDimensions> {
            val allRelevantDimensions = mutableListOf<BasicDataDimensions>()
            val allAssembledFrameworks = specificationClient.listFrameworkSpecifications().map { it.framework.id }
            val frameworks =
                if (dataDimensionFilter.dataTypes.isNullOrEmpty()) {
                    allAssembledFrameworks
                } else {
                    dataDimensionFilter.dataTypes.filter { allAssembledFrameworks.contains(it) }
                }

            for (framework in frameworks) {
                val activeDataPointMetaInformation =
                    metaDataManager.getActiveDataPointMetaInformationList(
                        DataDimensionFilter(
                            companyIds = dataDimensionFilter.companyIds,
                            dataTypes = getRelevantDataPointTypes(framework).toList(),
                            reportingPeriods = dataDimensionFilter.reportingPeriods,
                        ),
                    )

                activeDataPointMetaInformation
                    .groupBy {
                        Pair(it.companyId, it.reportingPeriod)
                    }.values
                    .forEach { metaInformationEntities ->
                        if (DataAvailabilityIgnoredFieldsUtils
                                .containsNonIgnoredDataPoints(metaInformationEntities.map { it.dataPointType })
                        ) {
                            allRelevantDimensions.addAll(
                                metaInformationEntities.map {
                                    it.toBasicDataDimensions(framework)
                                },
                            )
                        }
                    }
            }
            return allRelevantDimensions.distinct()
        }
    }

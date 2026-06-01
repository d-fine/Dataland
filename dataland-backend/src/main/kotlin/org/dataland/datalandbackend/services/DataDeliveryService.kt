package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.PlainDataAndDimensions
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.services.datapoints.DataPointCalculator
import org.dataland.datalandbackend.services.datapoints.DatasetAssembler
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Service to deliver data based on inputs like data dimensions. Performs assembly of datasets from data points.
 */
@Service("DataDeliveryService")
class DataDeliveryService
    @Autowired
    constructor(
        private val dataCompositionService: DataCompositionService,
        private val dataAvailabilityChecker: DataAvailabilityChecker,
        private val internalStorageAdapter: InternalStorageAdapter,
        private val datasetAssembler: DatasetAssembler,
        private val dataPointCalculator: DataPointCalculator,
    ) {
        /**
         * Delivers the datasets for the data dimensions provided in [dataDimensions] and returns a map of data dimension to
         * the string representation of the corresponding dataset. Only data points visible to the calling user are used. If
         * there are no deliverable data points for a given dimension it is skipped.
         *
         * @param dataDimensions the data dimensions for which datasets are to be assembled
         * @param correlationId the correlation ID for the operation
         */
        fun getAssembledDatasets(
            dataDimensions: Collection<BasicDatasetDimensions>,
            correlationId: String,
        ): Map<BasicDatasetDimensions, String> {
            val relevantDataPointTypes =
                dataDimensions
                    .map { it.framework }
                    .toSet()
                    .associateWith { framework ->
                        dataCompositionService.getRelevantDataPointTypes(framework)
                    }
            val relevantDimensionsByDataDimension =
                dataDimensions.associateWith { dataDimension ->
                    dataDimension.toBasicDataPointDimensions(
                        relevantDataPointTypes.getValue(dataDimension.framework),
                    )
                val deliverableDataPointMetaData = dataAvailabilityChecker.getViewableDataPointMetaData(relevantDimensions)
                if (deliverableDataPointMetaData.isNotEmpty()) {
                    requiredData[dataDimension] = deliverableDataPointMetaData.map { it.dataPointId }
                    deliverableDataPointDimensions[dataDimension] =
                        deliverableDataPointMetaData.map {
                            BasicDataPointDimensions(
                                companyId = it.companyId,
                                dataPointType = it.dataPointType,
                                reportingPeriod = it.reportingPeriod,
                            )
                        }
                }
            val calculatedData =
                dataPointCalculator.getCalculatedData(
                    datasetDimensions = dataDimensions,
                    correlationId = correlationId,
                    deliverableDataPointDimensions = deliverableDataPointDimensions,
                )
            return assembleDatasetsFromDataPointIds(requiredData, calculatedData, correlationId)
        }

        /**
         * Assembles datasets for every data dimension provided by retrieving the data points behind the provided IDs from
         * the internal storage and using the dataset assembler to create the corresponding datasets. This class does not check
         * for visibility or existence of the provided data point IDs.
         *
         * @param dataPointIds data point IDs to assemble to data sets grouped by data dimensions
         * @param correlationId the correlation ID for the operation
         * @return a map of data dimensions to the dataset in the form of a JSON string
         */
        private fun assembleDatasetsFromDataPointIds(
            dataPointIds: Map<BasicDatasetDimensions, List<DataPointId>>,
            calculatedData: Map<BasicDatasetDimensions, List<UploadedDataPoint>>,
            correlationId: String,
        ): Map<BasicDatasetDimensions, String> {
            val results = mutableMapOf<BasicDatasetDimensions, String>()

            val allRequiredIds = dataPointIds.values.flatten().toSet()
            val allStoredDataPoints =
                internalStorageAdapter
                    .retrieveDataPointsFromInternalStorage(dataPointIds = allRequiredIds, correlationId = correlationId)

            dataPointIds.forEach { (dataDimensions, dataIds) ->
                val datasetInput = dataIds.mapNotNull { allStoredDataPoints[it] } + calculatedData.getOrDefault(dataDimensions, emptyList())

                results[dataDimensions] =
                    datasetAssembler.assembleSingleDataset(datasetInput, dataDimensions.framework)
            }
            return results
        }

        /**
         * Retrieves the latest available assembled datasets for a particular data type and a collection of companíes
         *
         * If no data is available for a company, it is omitted from the result.
         *
         * @param companyIds the ids of the companies
         * @param framework the type of dataset
         * @param correlationId the correlation id for the operation
         * @return the latest available data for each company
         */
        fun getLatestAvailableAssembledDatasets(
            companyIds: Collection<String>,
            framework: String,
            correlationId: String,
        ): List<PlainDataAndDimensions> {
            // TODO Clean this up
            val dataPointTypes = dataCompositionService.getRelevantDataPointTypes(framework).toSet()

            val deliverableDataPointMetaData =
                dataAvailabilityChecker.getLatestAvailableDataPointIds(companyIds, dataPointTypes).mapKeys { (dim, _) ->
                    BasicDatasetDimensions(
                        companyId = dim.companyId,
                        framework = framework,
                        reportingPeriod = dim.reportingPeriod,
                    )
                }
            val deliverableDataPointDimensions =
                deliverableDataPointMetaData.mapValues { (_, metaData) ->
                    metaData.map {
                        BasicDataPointDimensions(
                            companyId = it.companyId,
                            dataPointType = it.dataPointType,
                            reportingPeriod = it.reportingPeriod,
                        )
                    }
                }

            val calculatedData =
                dataPointCalculator.getCalculatedData(
                    datasetDimensions = deliverableDataPointMetaData.keys,
                    correlationId = correlationId,
                    deliverableDataPointDimensions = deliverableDataPointDimensions,
                )

            return assembleDatasetsFromDataPointIds(
                dataPointIds =
                    deliverableDataPointMetaData
                        .map { (datasetDimension, metaData) ->
                            datasetDimension to metaData.map { it.dataPointId }
                        }.toMap(),
                calculatedData = calculatedData,
                correlationId = correlationId,
            ).map {
                PlainDataAndDimensions(
                    dimensions = it.key,
                    data = it.value,
                )
            }
        }
    }

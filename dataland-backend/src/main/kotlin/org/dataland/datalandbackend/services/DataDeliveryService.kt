package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.services.datapoints.DatasetAssembler
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
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
        private val storageClient: StorageControllerApi,
        private val datasetAssembler: DatasetAssembler,
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
            val requiredData = mutableMapOf<BasicDatasetDimensions, List<String>>()
            for (dataDimension in dataDimensions) {
                val relevantDataPointTypes = dataCompositionService.getRelevantDataPointTypes(dataDimension.framework)
                val relevantDimensions = dataDimension.toBasicDataPointDimensions(relevantDataPointTypes)
                val deliverableDataPointIds = dataAvailabilityChecker.getViewableDataPointIds(relevantDimensions)
                if (deliverableDataPointIds.isNotEmpty()) {
                    requiredData[dataDimension] = deliverableDataPointIds
                }
            }
            return assembleDatasetsFromDataPointIds(requiredData, correlationId)
        }

        /**
         * Assembles datasets for every data dimension provided by retrieving the data points behind the provided IDs from
         * the internal storage using the dataset assembler to create the corresponding datasets. This class does not check
         * for visibility or existence of the provide data point IDs.
         *
         * @param dataDimensionsToDataPointIdMap a map of all required data point IDs grouped by data set
         * @param correlationId the correlation ID for the operation
         * @return a map of data dimensions to the dataset in the form of a JSON string
         */
        private fun assembleDatasetsFromDataPointIds(
            dataDimensionsToDataPointIdMap: Map<BasicDatasetDimensions, List<String>>,
            correlationId: String,
        ): Map<BasicDatasetDimensions, String> {
            val results = mutableMapOf<BasicDatasetDimensions, String>()
            val allRequiredIds = dataDimensionsToDataPointIdMap.values.flatten().toSet()
            val allStoredDataPoints = retrieveDataPointsFromInternalStorage(allRequiredIds, correlationId)

            dataDimensionsToDataPointIdMap.forEach { (dataDimensions, dataIds) ->
                val datasetInput = allStoredDataPoints.filter { dataIds.contains(it.key) }
                results[dataDimensions] = datasetAssembler.assembleSingleDataset(datasetInput.values, dataDimensions.framework)
            }
            return results
        }

        /**
         * Retrieves a batch of data points from the internal storage identified by their IDs. IDs unknown to the internal storage are
         * ignored.
         *
         * @param dataPointIds a list of data point IDs to be retrieved
         * @param correlationId the correlation ID associated to the operation
         * @return a map of data point IDs to the respective content
         */
        fun retrieveDataPointsFromInternalStorage(
            dataPointIds: Collection<String>,
            correlationId: String,
        ): Map<String, UploadedDataPoint> {
            val dataPoints = mutableMapOf<String, UploadedDataPoint>()
            val dataPointsFromInternalStorage = storageClient.selectBatchDataPointsByIds(correlationId, dataPointIds.toList())
            dataPointsFromInternalStorage.forEach { (dataPointId, storedDataPoint) ->
                dataPoints[dataPointId] =
                    UploadedDataPoint(
                        dataPoint = storedDataPoint.dataPoint,
                        dataPointType = storedDataPoint.dataPointType,
                        companyId = storedDataPoint.companyId,
                        reportingPeriod = storedDataPoint.reportingPeriod,
                    )
            }
            return dataPoints
        }
    }

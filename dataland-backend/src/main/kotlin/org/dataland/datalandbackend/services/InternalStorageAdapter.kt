package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * Adapter to wrap the interaction with the internal storage for data retrieval.
 */
@Service("InternalStorageAdapter")
class InternalStorageAdapter
    @Autowired
    constructor(
        private val storageClient: StorageControllerApi,
    ) {
        /**
         * Retrieves a batch of data points from the internal storage identified by their IDs. IDs unknown to the internal storage are
         * ignored.
         *
         * @param dataPointIds a list of data point IDs to be retrieved
         * @param correlationId the correlation ID associated to the operation
         * @return a map of data point IDs to the respective content
         */
        fun retrieveDataPointsFromInternalStorage(
            dataPointIds: Collection<DataPointId>,
            correlationId: String,
        ): Map<DataPointId, UploadedDataPoint> {
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

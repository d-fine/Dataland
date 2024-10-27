package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.DataPointApi
import org.dataland.datalandbackend.model.datapoints.StorableDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadableDataPoint
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.DataPointManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller for the company metadata endpoints
 * @param dataMetaInformationManager service for handling data meta information
 */

@RestController
class DataPointController(
    @Autowired var dataMetaInformationManager: DataMetaInformationManager,
    @Autowired val dataPointManager: DataPointManager,
    @Autowired val logMessageBuilder: LogMessageBuilder,
) : DataPointApi {
    override fun postDataPoint(
        uploadedDataPoint: UploadableDataPoint,
        bypassQa: Boolean,
    ): ResponseEntity<String> {
        // val currentUser = DatalandAuthentication.fromContext()
        // val dataId = dataPointManager.storeDataPoint(uploadedDataPoint, currentUser, bypassQa)
        return ResponseEntity.ok(dataPointManager.storeDataPoint(uploadedDataPoint))
        /*return ResponseEntity.ok(
            DataPointMetaData(
                dataPointId = UUID.randomUUID(),
                dataType = "dataType",
                companyId = UUID.randomUUID(),
                reportingPeriod = "reportingPeriod",
                uploaderUserId = UUID.randomUUID(),
                uploadTime = 0,
                currentlyActive = false,
            ),
        )*/
    }

    override fun getDataPoint(dataId: String): ResponseEntity<StorableDataPoint> {
        // val currentUser = DatalandAuthentication.fromContextOrNull()
        // val metaInfo = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
        // if (!metaInfo.isDatasetViewableByUser(DatalandAuthentication.fromContextOrNull())) {
        //    throw AccessDeniedException(logMessageBuilder.generateAccessDeniedExceptionMessage(metaInfo.qaStatus))
        // }
        return ResponseEntity.ok(dataPointManager.retrieveDataPoint(UUID.fromString(dataId)))
        /*return ResponseEntity.ok(
            StorableDataPoint(
                dataPointId = UUID.randomUUID(),
                dataType = "dataType",
                companyId = UUID.randomUUID(),
                reportingPeriod = "reportingPeriod",
                data = "data",
            ),
        )*/
    }
}

package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.DataPointApi
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.datapoints.UploadableDataPoint
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.DataPointManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
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
        val currentUser = DatalandAuthentication.fromContext()
        val correlationId = UUID.randomUUID().toString()
        logMessageBuilder.postDataPointMessage(currentUser.userId, uploadedDataPoint, bypassQa, correlationId)
        return ResponseEntity.ok(dataPointManager.storeDataPoint(uploadedDataPoint, currentUser.userId, bypassQa, correlationId))
    }

    override fun getDataPoint(dataId: String): ResponseEntity<StorableDataSet> {
        // Todo: Implement access control
        val metaInfo = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
        val correlationId = UUID.randomUUID().toString()
        return ResponseEntity.ok(dataPointManager.retrieveDataPoint(UUID.fromString(dataId), metaInfo.dataType, correlationId))
    }
}

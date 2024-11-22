package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.DataPointApi
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.metainformation.DataPointMetaInformation
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.DataPointManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for data points
 * @param dataPointMetaInformationManager service for handling data point meta information
 * @param dataPointManager service for handling data points
 * @param logMessageBuilder service for building log messages
 */

@RestController
class DataPointController(
    @Autowired var dataPointMetaInformationManager: DataMetaInformationManager,
    @Autowired val dataPointManager: DataPointManager,
    @Autowired val logMessageBuilder: LogMessageBuilder,
) : DataPointApi {
    override fun postDataPoint(
        uploadedDataPoint: UploadedDataPoint,
        bypassQa: Boolean,
    ): ResponseEntity<DataPointMetaInformation> {
        val uploaderId = DatalandAuthentication.fromContext().userId
        val correlationId = IdUtils.generateCorrelationId(uploadedDataPoint.companyId, null)
        logMessageBuilder.postDataPointMessage(uploaderId, uploadedDataPoint, bypassQa, correlationId)
        return ResponseEntity.ok(dataPointManager.processDataPoint(uploadedDataPoint, uploaderId, bypassQa, correlationId))
    }

    override fun getDataPoint(dataId: String): ResponseEntity<UploadedDataPoint> {
        val correlationId = IdUtils.generateCorrelationId(null, dataId)
        return ResponseEntity.ok(dataPointManager.retrieveDataPoint(dataId, correlationId))
    }
}

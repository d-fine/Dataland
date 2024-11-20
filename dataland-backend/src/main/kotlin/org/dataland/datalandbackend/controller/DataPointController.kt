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
import java.util.UUID

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
        val correlationId = IdUtils.generateCorrelationId(uploadedDataPoint.companyId.toString(), null)
        logMessageBuilder.postDataPointMessage(uploaderId, uploadedDataPoint, bypassQa, correlationId)
        return ResponseEntity.ok(dataPointManager.processDataPoint(uploadedDataPoint, uploaderId, bypassQa, correlationId))
    }

    override fun getDataPoint(dataId: UUID): ResponseEntity<UploadedDataPoint> {
        val metaInfo = dataPointMetaInformationManager.getDataPointMetaInformationByDataId(dataId)
        val correlationId = IdUtils.generateCorrelationId(metaInfo.companyId.toString(), dataId.toString())
        return ResponseEntity.ok(dataPointManager.retrieveDataPoint(dataId, metaInfo.dataPointIdentifier, correlationId))
    }
}

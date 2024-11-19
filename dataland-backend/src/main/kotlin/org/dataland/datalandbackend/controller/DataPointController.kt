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
        uploadedDataPoint: UploadedDataPoint,
        bypassQa: Boolean,
    ): ResponseEntity<DataPointMetaInformation> {
        val uploaderId = DatalandAuthentication.fromContext().userId
        val correlationId = IdUtils.generateCorrelationId(uploadedDataPoint.companyId.toString(), null)
        logMessageBuilder.postDataPointMessage(uploaderId, uploadedDataPoint, bypassQa, correlationId)
        return ResponseEntity.ok(dataPointManager.processDataPoint(uploadedDataPoint, uploaderId, bypassQa, correlationId))
    }

    override fun getDataPoint(dataId: String): ResponseEntity<UploadedDataPoint> {
        val metaInfo = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
        val correlationId = IdUtils.generateCorrelationId(metaInfo.company.companyId, dataId)
        return ResponseEntity.ok(dataPointManager.retrieveDataPoint(dataId, metaInfo.dataType, correlationId))
    }
}

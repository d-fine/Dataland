package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.DataPointApi
import org.dataland.datalandbackend.model.datapoints.DataPointToValidate
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.metainformation.DataPointMetaInformation
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.datapoints.DataPointManager
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for data points
 * @param dataPointMetaInformationManager service for handling data point meta information
 * @param dataPointManager service for handling data points
 * @param logMessageBuilder service for building log messages
 */

@RestController
class DataPointController(
    @Autowired private val dataPointMetaInformationManager: DataPointMetaInformationManager,
    @Autowired private val dataPointManager: DataPointManager,
    @Autowired private val dataPointValidator: DataPointValidator,
    @Autowired private val logMessageBuilder: LogMessageBuilder,
) : DataPointApi {
    override fun validateDataPoint(dataPoint: DataPointToValidate): ResponseEntity<Void> {
        val correlationId = IdUtils.generateCorrelationId(null, null)
        dataPointValidator.validateDataPoint(dataPoint.dataPointType, dataPoint.dataPoint, correlationId)
        return ResponseEntity.noContent().build()
    }

    override fun postDataPoint(
        uploadedDataPoint: UploadedDataPoint,
        bypassQa: Boolean,
    ): ResponseEntity<DataPointMetaInformation> {
        val uploaderId = DatalandAuthentication.fromContext().userId
        val correlationId = IdUtils.generateCorrelationId(uploadedDataPoint.companyId, null)
        logMessageBuilder.postDataPointMessage(uploaderId, uploadedDataPoint, bypassQa, correlationId)
        return ResponseEntity.ok(dataPointManager.processDataPoint(uploadedDataPoint, uploaderId, bypassQa, correlationId))
    }

    override fun getDataPoint(dataPointId: String): ResponseEntity<UploadedDataPoint> {
        val correlationId = IdUtils.generateCorrelationId(null, dataPointId)
        val metaInfo = dataPointMetaInformationManager.getDataPointMetaInformationById(dataPointId)
        if (!metaInfo.isDatasetViewableByUser(DatalandAuthentication.fromContextOrNull())) {
            throw AccessDeniedException(logMessageBuilder.generateAccessDeniedExceptionMessage(metaInfo.qaStatus))
        }
        return ResponseEntity.ok(dataPointManager.retrieveDataPoint(dataPointId, correlationId))
    }

    override fun getDataPointMetaInfo(dataPointId: String): ResponseEntity<DataPointMetaInformation> {
        val metaInfo = dataPointMetaInformationManager.getDataPointMetaInformationById(dataPointId)
        if (!metaInfo.isDatasetViewableByUser(DatalandAuthentication.fromContextOrNull())) {
            throw AccessDeniedException(logMessageBuilder.generateAccessDeniedExceptionMessage(metaInfo.qaStatus))
        }
        return ResponseEntity.ok(metaInfo.toApiModel())
    }
}

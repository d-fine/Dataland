package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.MetaDataApi
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.model.metainformation.NonSourceableData
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.NonSourceableDataManager
import org.dataland.datalandbackend.utils.IdUtils.generateCorrelationId
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller for the company metadata endpoints
 * @param dataMetaInformationManager service for handling data meta information
 */

@RestController
class MetaDataController(
    @Autowired var dataMetaInformationManager: DataMetaInformationManager,
    @Autowired val logMessageBuilder: LogMessageBuilder,
    @Autowired val nonSourceableDataManager: NonSourceableDataManager,
) : MetaDataApi {
    override fun getListOfDataMetaInfo(
        companyId: String?,
        dataType: DataType?,
        showOnlyActive: Boolean,
        reportingPeriod: String?,
        uploaderUserIds: Set<UUID>?,
        qaStatus: QaStatus?,
    ): ResponseEntity<List<DataMetaInformation>> {
        val currentUser = DatalandAuthentication.fromContextOrNull()
        return ResponseEntity.ok(
            dataMetaInformationManager
                .searchDataMetaInfo(
                    companyId,
                    dataType,
                    showOnlyActive,
                    reportingPeriod,
                    uploaderUserIds,
                    qaStatus,
                ).filter { it.isDatasetViewableByUser(currentUser) }
                .map { it.toApiModel(currentUser) },
        )
    }

    override fun getDataMetaInfo(dataId: String): ResponseEntity<DataMetaInformation> {
        val currentUser = DatalandAuthentication.fromContextOrNull()
        val metaInfo = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
        if (!metaInfo.isDatasetViewableByUser(DatalandAuthentication.fromContextOrNull())) {
            throw AccessDeniedException(logMessageBuilder.generateAccessDeniedExceptionMessage(metaInfo.qaStatus))
        }
        return ResponseEntity.ok(metaInfo.toApiModel(currentUser))
    }

    override fun getNonSourceableDatasets(
        companyId: String,
        dataType: DataType,
        reportingPeriod: String,
        nonSourceable: Boolean,
    ): ResponseEntity<List<NonSourceableData>?> {
        val currentUser = DatalandAuthentication.fromContextOrNull()
        val nonSourceableData =
            nonSourceableDataManager.getNonSourceableDataByTriple(
                companyId,
                dataType,
                reportingPeriod,
            )
        return ResponseEntity.ok(
            nonSourceableData
                .filter { it }
                .map { it.toApiModel(currentUser) },
        )
    }

    override fun postNonSourceableDataSet(nonSourceableData: NonSourceableData) {
        // implement functionality
        val correlationId = generateCorrelationId(nonSourceableData.companyId, null)
        // set creationTime
        nonSourceableDataManager.createEventDatasetNonSourceable(correlationId, nonSourceableData)
    }

    override fun isDataNonSourceable(
        companyId: String,
        dataType: DataType,
        reportingPeriod: String,
    ) {
        // implement functionality
    }
}

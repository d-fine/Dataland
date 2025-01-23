package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.MetaDataApi
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformationPatch
import org.dataland.datalandbackend.model.metainformation.NonSourceableInfo
import org.dataland.datalandbackend.model.metainformation.NonSourceableInfoResponse
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.NonSourceableDataManager
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
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
 * @param logMessageBuilder a helper for building log messages
 * @param nonSourceableDataManager service for handling information on data sets and their sourceability
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
        if (!metaInfo.isDatasetViewableByUser(currentUser)) {
            throw AccessDeniedException(
                logMessageBuilder.generateAccessDeniedExceptionMessage(
                    metaInfo.qaStatus,
                ),
            )
        }
        return ResponseEntity.ok(metaInfo.toApiModel(currentUser))
    }

    override fun patchDataMetaInfo(
        dataId: String,
        dataMetaInformationPatch: DataMetaInformationPatch,
    ): ResponseEntity<DataMetaInformation> {
        val currentUser = DatalandAuthentication.fromContextOrNull()
        val metaInfo = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
        val companyId = metaInfo.company.companyId
        val correlationId = IdUtils.generateCorrelationId(companyId, dataId)
        logMessageBuilder.patchDataMetaInformationMessage(
            userId = currentUser?.userId,
            dataId = dataId,
            dataType = metaInfo.dataType,
            companyId = companyId,
            reportingPeriod = metaInfo.reportingPeriod,
            correlationId = correlationId,
        )
        if (!metaInfo.isDatasetViewableByUser(currentUser)) {
            throw AccessDeniedException(
                logMessageBuilder.generateAccessDeniedExceptionMessage(
                    metaInfo.qaStatus,
                ),
            )
        }
        val patchedMetaInfo = dataMetaInformationManager.patchDataMetaInformation(dataId, dataMetaInformationPatch, correlationId)
        return ResponseEntity.ok(patchedMetaInfo.toApiModel(currentUser))
    }

    override fun getInfoOnNonSourceabilityOfDataSets(
        companyId: String?,
        dataType: DataType?,
        reportingPeriod: String?,
        nonSourceable: Boolean?,
    ): ResponseEntity<List<NonSourceableInfoResponse>> =
        ResponseEntity.ok(
            nonSourceableDataManager
                .getNonSourceableDataByFilters(
                    companyId,
                    dataType,
                    reportingPeriod,
                    nonSourceable,
                ),
        )

    override fun postNonSourceabilityOfADataset(nonSourceableInfo: NonSourceableInfo) {
        nonSourceableDataManager.processSourceabilityDataStorageRequest(nonSourceableInfo)
    }

    override fun isDataNonSourceable(
        companyId: String,
        dataType: DataType,
        reportingPeriod: String,
    ) {
        val latestNonSourceableInfo =
            nonSourceableDataManager.getLatestNonSourceableInfoForDataset(companyId, dataType, reportingPeriod)

        if (latestNonSourceableInfo?.isNonSourceable != true) {
            throw ResourceNotFoundApiException(
                summary = "Dataset is sourceable or not found.",
                message =
                    "No non-sourceable dataset found for company $companyId, dataType $dataType, " +
                        "and reportingPeriod $reportingPeriod.",
            )
        }
    }
}

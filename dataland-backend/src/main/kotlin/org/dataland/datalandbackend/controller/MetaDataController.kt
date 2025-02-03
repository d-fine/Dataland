package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.MetaDataApi
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformationRequest
import org.dataland.datalandbackend.model.metainformation.NonSourceableInfo
import org.dataland.datalandbackend.model.metainformation.NonSourceableInfoResponse
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.NonSourceableDataManager
import org.dataland.datalandbackend.services.datapoints.AssembledDataManager
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller for the company metadata endpoints
 * @param dataMetaInformationManager service for handling data meta information
 * @param logMessageBuilder a helper for building log messages
 * @param nonSourceableDataManager service for handling information on datasets and their sourceability
 */

@RestController
class MetaDataController(
    @Autowired var dataMetaInformationManager: DataMetaInformationManager,
    @Autowired val logMessageBuilder: LogMessageBuilder,
    @Autowired val nonSourceableDataManager: NonSourceableDataManager,
    @Autowired val assembledDataManager: AssembledDataManager,
    @Value("\${dataland.backend.proxy-primary-url}") private val proxyPrimaryUrl: String,
) : MetaDataApi {
    private fun getListOfDataMetaInfoForUser(
        user: DatalandAuthentication?,
        dataMetaInformationRequest: DataMetaInformationRequest,
    ): List<DataMetaInformation> {
        val listDataMetaInformation =
            dataMetaInformationManager
                .searchDataMetaInfo(
                    dataMetaInformationRequest.companyId,
                    dataMetaInformationRequest.dataType,
                    dataMetaInformationRequest.showOnlyActive,
                    dataMetaInformationRequest.reportingPeriod,
                    dataMetaInformationRequest.uploaderUserIds,
                    dataMetaInformationRequest.qaStatus,
                ).filter { it.isDatasetViewableByUser(user) }
                .map { it.toApiModel(user) }
        listDataMetaInformation.forEach {
            it.url = "https://$proxyPrimaryUrl/companies/${it.companyId}/frameworks/${it.dataType}/${it.dataId}"
        }
        return listDataMetaInformation
    }

    override fun getListOfDataMetaInfo(
        companyId: String?,
        dataType: DataType?,
        showOnlyActive: Boolean,
        reportingPeriod: String?,
        uploaderUserIds: Set<UUID>?,
        qaStatus: QaStatus?,
    ): ResponseEntity<List<DataMetaInformation>> =
        ResponseEntity.ok(
            this.getListOfDataMetaInfoForUser(
                DatalandAuthentication.fromContextOrNull(),
                DataMetaInformationRequest(companyId, dataType, showOnlyActive, reportingPeriod, uploaderUserIds, qaStatus),
            ),
        )

    override fun postListOfDataMetaInfoFilters(
        dataMetaInformationRequests: List<DataMetaInformationRequest>,
    ): ResponseEntity<List<DataMetaInformation>> {
        val currentUser = DatalandAuthentication.fromContextOrNull()
        return ResponseEntity.ok(
            dataMetaInformationRequests
                .distinct()
                .map { this.getListOfDataMetaInfoForUser(currentUser, it) }
                .flatten()
                .distinct(),
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

    override fun getInfoOnNonSourceabilityOfDatasets(
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
        val latestNonSourceableInfo = nonSourceableDataManager.getLatestNonSourceableInfoForDataset(companyId, dataType, reportingPeriod)

        if (latestNonSourceableInfo?.isNonSourceable != true) {
            throw ResourceNotFoundApiException(
                summary = "Dataset is sourceable or not found.",
                message =
                    "No non-sourceable dataset found for company $companyId, dataType $dataType, " +
                        "and reportingPeriod $reportingPeriod.",
            )
        }
    }

    override fun getContainedDataPoints(dataId: String): ResponseEntity<Map<String, String>> {
        val dataPoints = assembledDataManager.getDataPointIdsForDataset(dataId)
        if (dataPoints.isEmpty()) {
            throw ResourceNotFoundApiException(
                summary = "No data point mapping found for dataset.",
                message = "Either the provided dataset ID $dataId is invalid or the corresponding framework does not support data points.",
            )
        }
        return ResponseEntity.ok(dataPoints)
    }
}

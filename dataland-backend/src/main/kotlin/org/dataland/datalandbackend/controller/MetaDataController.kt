package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.MetaDataApi
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformationPatch
import org.dataland.datalandbackend.model.metainformation.NonSourceableInfo
import org.dataland.datalandbackend.model.metainformation.NonSourceableInfoResponse
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackend.services.DataMetaInfoAlterationManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.NonSourceableDataManager
import org.dataland.datalandbackend.services.datapoints.AssembledDataManager
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
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
    @Autowired val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired val dataMetaInfoAlterationManager: DataMetaInfoAlterationManager,
    @Autowired val logMessageBuilder: LogMessageBuilder,
    @Autowired val nonSourceableDataManager: NonSourceableDataManager,
    @Autowired val assembledDataManager: AssembledDataManager,
    @Value("\${dataland.backend.proxy-primary-url}") private val proxyPrimaryUrl: String,
) : MetaDataApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun getListOfDataMetaInfoForUser(
        user: DatalandAuthentication?,
        dataMetaInformationSearchFilter: DataMetaInformationSearchFilter,
    ): List<DataMetaInformation> {
        var foundDataMetaInformation = emptyList<DataMetaInformation>()
        if (!dataMetaInformationSearchFilter.isNullOrEmpty()) {
            foundDataMetaInformation =
                dataMetaInformationManager
                    .searchDataMetaInfo(
                        dataMetaInformationSearchFilter,
                    ).filter { it.isDatasetViewableByUser(user) }
                    .map { it.toApiModel(proxyPrimaryUrl) }
        }
        return foundDataMetaInformation
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
                DataMetaInformationSearchFilter(
                    companyId = companyId,
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    onlyActive = showOnlyActive,
                    uploaderUserIds = uploaderUserIds,
                    qaStatus = qaStatus,
                ),
            ),
        )

    override fun postListOfDataMetaInfoFilters(
        dataMetaInformationSearchFilters: List<DataMetaInformationSearchFilter>,
    ): ResponseEntity<List<DataMetaInformation>> {
        val currentUser = DatalandAuthentication.fromContextOrNull()
        return ResponseEntity.ok(
            dataMetaInformationSearchFilters
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
        return ResponseEntity.ok(metaInfo.toApiModel(proxyPrimaryUrl))
    }

    override fun patchDataMetaInfo(
        dataId: String,
        dataMetaInformationPatch: DataMetaInformationPatch,
    ): ResponseEntity<DataMetaInformation> {
        val currentUser = DatalandAuthentication.fromContextOrNull()
        val metaInfo = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
        val companyId = metaInfo.company.companyId
        val correlationId = IdUtils.generateCorrelationId(companyId, dataId)
        if (dataMetaInformationPatch.uploaderUserId.isEmpty()) {
            throw InvalidInputApiException(
                summary = "Empty Request Body",
                message = "Request body must not be null nor empty",
            )
        }
        if (metaInfo.dataType == "vsme") {
            throw InvalidInputApiException(
                summary = "Data of dataType ${metaInfo.dataType} cannot be patched.",
                message = "Patching metadata is only permitted for public datasets.",
            )
        }
        logger.info(
            logMessageBuilder.patchDataMetaInformationMessage(
                userId = currentUser?.userId,
                dataId = dataId,
                dataType = metaInfo.dataType,
                companyId = companyId,
                reportingPeriod = metaInfo.reportingPeriod,
                correlationId = correlationId,
            ),
        )
        val patchedMetaInfo =
            dataMetaInfoAlterationManager.patchDataMetaInformation(dataId, dataMetaInformationPatch, correlationId)
        return ResponseEntity.ok(patchedMetaInfo.toApiModel())
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

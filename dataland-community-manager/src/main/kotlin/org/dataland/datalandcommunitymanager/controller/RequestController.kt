package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.api.RequestApi
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.ExtendedStoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequestResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.services.BulkDataRequestManager
import org.dataland.datalandcommunitymanager.services.DataRequestAlterationManager
import org.dataland.datalandcommunitymanager.services.DataRequestQueryManager
import org.dataland.datalandcommunitymanager.services.SingleDataRequestManager
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller for the requests endpoint
 * @param bulkDataRequestManager service for all operations concerning the processing of data requests
 */

@RestController
class RequestController(
    @Autowired private val bulkDataRequestManager: BulkDataRequestManager,
    @Autowired private val singleDataRequestManager: SingleDataRequestManager,
    @Autowired private val dataRequestQueryManager: DataRequestQueryManager,
    @Autowired private val dataRequestAlterationManager: DataRequestAlterationManager,
    @Autowired private val dataRequestProcessingUtils: DataRequestProcessingUtils,
) : RequestApi {

    override fun postBulkDataRequest(bulkDataRequest: BulkDataRequest): ResponseEntity<BulkDataRequestResponse> {
        return ResponseEntity.ok(
            bulkDataRequestManager.processBulkDataRequest(bulkDataRequest),
        )
    }

    override fun getDataRequestsForRequestingUser(): ResponseEntity<List<ExtendedStoredDataRequest>> {
        return ResponseEntity.ok(dataRequestQueryManager.getDataRequestsForRequestingUser())
    }

    override fun getAggregatedDataRequests(
        identifierValue: String?,
        dataTypes: Set<DataTypeEnum>?,
        reportingPeriod: String?,
        status: RequestStatus?,
    ): ResponseEntity<List<AggregatedDataRequest>> {
        return ResponseEntity.ok(
            dataRequestQueryManager.getAggregatedDataRequests(
                identifierValue,
                dataTypes,
                reportingPeriod,
                status,
            ),
        )
    }

    override fun postSingleDataRequest(
        singleDataRequest: SingleDataRequest,
    ): ResponseEntity<SingleDataRequestResponse> {
        return ResponseEntity.ok(
            singleDataRequestManager.processSingleDataRequest(singleDataRequest),
        )
    }

    override fun getDataRequestById(dataRequestId: UUID): ResponseEntity<StoredDataRequest> {
        return ResponseEntity.ok(dataRequestQueryManager.getDataRequestById(dataRequestId.toString()))
    }

    override fun getDataRequests(
        dataType: DataTypeEnum?,
        userId: String?,
        requestStatus: RequestStatus?,
        reportingPeriod: String?,
        datalandCompanyId: String?,
    ): ResponseEntity<List<StoredDataRequest>> {
        return ResponseEntity.ok(
            dataRequestQueryManager.getDataRequests(
                dataType,
                userId,
                requestStatus,
                reportingPeriod,
                datalandCompanyId,
            ),
        )
    }

    override fun hasAccessToDataset(companyId: UUID, dataType: DataTypeEnum, reportingPeriod: String) {
        // only the vsme framework is private
        if (dataType != DataTypeEnum.vsme) {
            return
        }

        val userId = DatalandAuthentication.fromContext().userId

        val hasAccess = dataRequestProcessingUtils.hasAccessToPrivateDataset(
            companyId.toString(), reportingPeriod, dataType, userId,
        )

        if (!hasAccess) {
            throw ResourceNotFoundApiException(
                "The user has no access to the dataset or the dataset does not exists.",
                "The user $userId cannot access the dataset for the company $companyId, for the data type $dataType " +
                    "and the reporting period $reportingPeriod. The dataset may not exists.",
            )
        }
    }

    override fun patchDataRequest(
        dataRequestId: UUID,
        requestStatus: RequestStatus?,
        accessStatus: AccessStatus?,
        contacts: Set<String>?,
        message: String?,
    ): ResponseEntity<StoredDataRequest> {
        return ResponseEntity.ok(
            dataRequestAlterationManager.patchDataRequest(
                dataRequestId.toString(),
                requestStatus,
                accessStatus,
                contacts,
                message,
            ),
        )
    }
}

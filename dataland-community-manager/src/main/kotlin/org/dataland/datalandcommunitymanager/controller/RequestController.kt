package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.api.RequestApi
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.ExtendedStoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequestResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.services.BulkDataRequestManager
import org.dataland.datalandcommunitymanager.services.CompanyRolesManager
import org.dataland.datalandcommunitymanager.services.DataAccessManager
import org.dataland.datalandcommunitymanager.services.DataRequestAlterationManager
import org.dataland.datalandcommunitymanager.services.DataRequestQueryManager
import org.dataland.datalandcommunitymanager.services.SingleDataRequestManager
import org.dataland.datalandcommunitymanager.utils.DataRequestsFilter
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
@Suppress("LongParameterList")
class RequestController(
    @Autowired private val bulkDataRequestManager: BulkDataRequestManager,
    @Autowired private val singleDataRequestManager: SingleDataRequestManager,
    @Autowired private val dataRequestQueryManager: DataRequestQueryManager,
    @Autowired private val dataRequestAlterationManager: DataRequestAlterationManager,
    @Autowired private val dataAccessManager: DataAccessManager,
    @Autowired private val companyRolesManager: CompanyRolesManager,
) : RequestApi {
    override fun postBulkDataRequest(bulkDataRequest: BulkDataRequest): ResponseEntity<BulkDataRequestResponse> =
        ResponseEntity.ok(
            bulkDataRequestManager.processBulkDataRequest(bulkDataRequest),
        )

    override fun getDataRequestsForRequestingUser(): ResponseEntity<List<ExtendedStoredDataRequest>> =
        ResponseEntity.ok(
            dataRequestQueryManager.getDataRequestsForRequestingUser(),
        )

    override fun getAggregatedDataRequests(
        identifierValue: String?,
        dataTypes: Set<DataTypeEnum>?,
        reportingPeriod: String?,
        status: RequestStatus?,
    ): ResponseEntity<List<AggregatedDataRequest>> =
        ResponseEntity.ok(
            dataRequestQueryManager.getAggregatedDataRequests(
                identifierValue,
                dataTypes,
                reportingPeriod,
                status,
            ),
        )

    override fun postSingleDataRequest(singleDataRequest: SingleDataRequest): ResponseEntity<SingleDataRequestResponse> =
        ResponseEntity.ok(
            singleDataRequestManager.processSingleDataRequest(singleDataRequest),
        )

    override fun getDataRequestById(dataRequestId: UUID): ResponseEntity<StoredDataRequest> =
        ResponseEntity.ok(
            dataRequestQueryManager.getDataRequestById(dataRequestId.toString()),
        )

    override fun getDataRequests(
        dataType: Set<DataTypeEnum>?,
        userId: String?,
        emailAddress: String?,
        adminComment: String?,
        requestStatus: Set<RequestStatus>?,
        accessStatus: Set<AccessStatus>?,
        requestPriority: Set<RequestPriority>?,
        reportingPeriod: String?,
        datalandCompanyId: String?,
        chunkSize: Int,
        chunkIndex: Int,
    ): ResponseEntity<List<ExtendedStoredDataRequest>> {
        val filter =
            DataRequestsFilter(
                dataType,
                userId,
                emailAddress,
                datalandCompanyId,
                reportingPeriod,
                requestStatus,
                accessStatus,
                adminComment,
                requestPriority,
            )

        val authenticationContext = DatalandAuthentication.fromContext()

        val ownedCompanyIdsByUser =
            companyRolesManager
                .getCompanyRoleAssignmentsByParameters(CompanyRole.CompanyOwner, null, authenticationContext.userId)
                .map { it.companyId }

        return ResponseEntity.ok(
            dataRequestQueryManager.getDataRequests(
                ownedCompanyIdsByUser,
                filter,
                chunkIndex,
                chunkSize,
            ),
        )
    }

    override fun getNumberOfRequests(
        dataType: Set<DataTypeEnum>?,
        userId: String?,
        emailAddress: String?,
        adminComment: String?,
        requestStatus: Set<RequestStatus>?,
        accessStatus: Set<AccessStatus>?,
        requestPriority: Set<RequestPriority>?,
        reportingPeriod: String?,
        datalandCompanyId: String?,
    ): ResponseEntity<Int> {
        val filter =
            DataRequestsFilter(
                dataType,
                userId,
                emailAddress,
                datalandCompanyId,
                reportingPeriod,
                requestStatus,
                accessStatus,
                adminComment,
                requestPriority,
            )

        return ResponseEntity.ok(dataRequestQueryManager.getNumberOfDataRequests(filter))
    }

    override fun hasAccessToDataset(
        companyId: UUID,
        dataType: String,
        reportingPeriod: String,
        userId: UUID,
    ) {
        dataAccessManager.hasAccessToDataset(companyId.toString(), reportingPeriod, dataType, userId.toString())
    }

    override fun patchDataRequest(
        dataRequestId: UUID,
        requestStatus: RequestStatus?,
        accessStatus: AccessStatus?,
        contacts: Set<String>?,
        message: String?,
        requestPriority: RequestPriority?,
        adminComment: String?,
        requestStatusChangeReason: String?,
    ): ResponseEntity<StoredDataRequest> =
        ResponseEntity.ok(
            dataRequestAlterationManager.patchDataRequest(
                dataRequestId.toString(),
                requestStatus,
                accessStatus,
                contacts,
                message,
                correlationId = null,
                requestPriority,
                adminComment,
                requestStatusChangeReason,
            ),
        )
}

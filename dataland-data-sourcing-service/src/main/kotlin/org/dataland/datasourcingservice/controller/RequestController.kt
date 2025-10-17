package org.dataland.datasourcingservice.controller

import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datasourcingservice.api.RequestApi
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.BulkDataRequest
import org.dataland.datasourcingservice.model.request.BulkDataRequestResponse
import org.dataland.datasourcingservice.model.request.RequestSearchFilter
import org.dataland.datasourcingservice.model.request.SingleRequest
import org.dataland.datasourcingservice.model.request.SingleRequestResponse
import org.dataland.datasourcingservice.model.request.StoredRequest
import org.dataland.datasourcingservice.services.BulkRequestManager
import org.dataland.datasourcingservice.services.ExistingRequestsManager
import org.dataland.datasourcingservice.services.RequestCreationService
import org.dataland.datasourcingservice.services.RequestQueryManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller for the requests endpoint
 */
@RestController
class RequestController
    @Autowired
    constructor(
        private val existingRequestsManager: ExistingRequestsManager,
        private val bulkDataRequestManager: BulkRequestManager,
        private val requestCreationService: RequestCreationService,
        private val requestQueryManager: RequestQueryManager,
    ) : RequestApi {
        override fun postBulkDataRequest(
            bulkDataRequest: BulkDataRequest,
            userId: String?,
        ): ResponseEntity<BulkDataRequestResponse> =
            ResponseEntity.ok(
                bulkDataRequestManager.processBulkDataRequest(
                    bulkDataRequest,
                    userId?.let {
                        ValidationUtils.convertToUUID(it)
                    },
                ),
            )

        override fun createRequest(
            singleRequest: SingleRequest,
            userId: String?,
        ): ResponseEntity<SingleRequestResponse> =
            ResponseEntity.ok(
                requestCreationService.createRequest(
                    singleRequest,
                    userId?.let { ValidationUtils.convertToUUID(it) },
                ),
            )

        override fun getRequest(dataRequestId: String): ResponseEntity<StoredRequest> =
            ResponseEntity.ok(
                existingRequestsManager.getRequest(
                    ValidationUtils.convertToUUID(
                        dataRequestId,
                    ),
                ),
            )

        override fun patchRequestState(
            dataRequestId: String,
            requestState: RequestState,
            adminComment: String?,
        ): ResponseEntity<StoredRequest> =
            ResponseEntity.ok(
                existingRequestsManager.patchRequestState(
                    ValidationUtils.convertToUUID(
                        dataRequestId,
                    ),
                    requestState, adminComment,
                ),
            )

        override fun patchRequestPriority(
            dataRequestId: String,
            requestPriority: RequestPriority,
            adminComment: String?,
        ): ResponseEntity<StoredRequest> =
            ResponseEntity.ok(
                existingRequestsManager.patchRequestPriority(
                    ValidationUtils.convertToUUID(dataRequestId),
                    requestPriority,
                    adminComment,
                ),
            )

        override fun getRequestHistoryById(dataRequestId: String): ResponseEntity<List<StoredRequest>> =
            ResponseEntity
                .ok(
                    existingRequestsManager.retrieveRequestHistory(
                        ValidationUtils.convertToUUID(
                            dataRequestId,
                        ),
                    ),
                )

        private fun convertToSearchFilterWithUUIDs(requestSearchFilterWithStrings: RequestSearchFilter<String>): RequestSearchFilter<UUID> =
            RequestSearchFilter<UUID>(
                companyId =
                    requestSearchFilterWithStrings.companyId?.let {
                        ValidationUtils.convertToUUID(it)
                    },
                dataTypes = requestSearchFilterWithStrings.dataTypes,
                reportingPeriods = requestSearchFilterWithStrings.reportingPeriods,
                userId =
                    requestSearchFilterWithStrings.userId?.let {
                        ValidationUtils.convertToUUID(
                            it,
                        )
                    },
                requestStates = requestSearchFilterWithStrings.requestStates,
                requestPriorities = requestSearchFilterWithStrings.requestPriorities,
            )

        override fun postRequestSearch(
            requestSearchFilter: RequestSearchFilter<String>,
            chunkSize: Int,
            chunkIndex: Int,
        ): ResponseEntity<List<StoredRequest>> =
            ResponseEntity.ok(
                requestQueryManager.searchRequests(
                    convertToSearchFilterWithUUIDs(requestSearchFilter), chunkSize, chunkIndex,
                ),
            )

        override fun postRequestCountQuery(requestSearchFilter: RequestSearchFilter<String>): ResponseEntity<Int> =
            ResponseEntity.ok(
                requestQueryManager.getNumberOfRequests(convertToSearchFilterWithUUIDs(requestSearchFilter)),
            )
    }

package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.NonSourceableInfo
import org.dataland.datalandcommunitymanager.model.dataRequest.DataRequestPatch
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Manages requests corresponding to a non-sourceable dataset
 */
@Service
class NonSourceableDataManager(
    @Autowired private val dataRequestAlterationManager: DataRequestAlterationManager,
    @Autowired private val dataRequestRepository: DataRequestRepository,
) {
    /**
     * Method to patch all data request corresponding to a dataset
     * @param nonSourceableInfo the info on the non-sourceable dataset
     * @param correlationId correlationId
     */
    fun patchAllRequestsForThisDatasetToStatusNonSourceable(
        nonSourceableInfo: NonSourceableInfo,
        correlationId: String,
    ) {
        if (nonSourceableInfo.isNonSourceable) {
            val dataRequestEntities =
                dataRequestRepository.findAllByDatalandCompanyIdAndDataTypeAndReportingPeriod(
                    datalandCompanyId = nonSourceableInfo.companyId,
                    dataType = nonSourceableInfo.dataType.toString(),
                    reportingPeriod = nonSourceableInfo.reportingPeriod,
                )

            dataRequestEntities?.forEach {
                dataRequestAlterationManager.patchDataRequest(
                    dataRequestId = it.dataRequestId,
                    dataRequestPatch =
                        DataRequestPatch(
                            requestStatus = RequestStatus.NonSourceable,
                            requestStatusChangeReason = nonSourceableInfo.reason,
                        ),
                    correlationId = correlationId,
                )
            }
        } else {
            throw IllegalArgumentException(
                "Expected information about a non-sourceable dataset but received information " +
                    "about a sourceable dataset. No requests are patched if a dataset is reported as " +
                    "sourceable until the dataset is uplaoded.",
            )
        }
    }
}

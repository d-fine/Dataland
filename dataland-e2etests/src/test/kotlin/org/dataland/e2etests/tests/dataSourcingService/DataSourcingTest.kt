package org.dataland.e2etests.tests.dataSourcingService

import org.dataland.dataSourcingService.openApiClient.model.RequestState
import org.dataland.dataSourcingService.openApiClient.model.SingleRequest
import org.dataland.dataSourcingService.openApiClient.model.StoredDataSourcing
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor

/**
 * Common superclass for DataSourcingControllerTest and DataSourcingServiceListenerTest. Provides functionality used
 * by both.
 */
open class DataSourcingTest {
    val apiAccessor = ApiAccessor()
    lateinit var storedDataSourcing: StoredDataSourcing
    val testDataType = "sfdr"
    val testReportingPeriod = "2023"

    /**
     * Common setup function to be run before each test in each subclass.
     */
    fun initializeDataSourcing() {
        val (companyId, requestId) = createNewCompanyAndRequestAndReturnTheirIds()
        apiAccessor.dataSourcingRequestControllerApi.patchRequestState(requestId, RequestState.Processing)
        storedDataSourcing =
            apiAccessor.dataSourcingControllerApi
                .searchDataSourcings(
                    companyId = companyId,
                    dataType = testDataType,
                    reportingPeriod = testReportingPeriod,
                ).first()
    }

    /**
     * Uploads a new company and returns its ID.
     */
    fun createNewCompanyAndReturnId(): String =
        GlobalAuth.withTechnicalUser(TechnicalUser.Uploader) {
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        }

    /**
     * Creates a new request (in the sense of the data sourcing service) for the given company and returns the
     * ID of the created request.
     */
    fun createRequest(
        companyId: String,
        dataType: String = testDataType,
        reportingPeriod: String = testReportingPeriod,
        comment: String = "test request",
        user: TechnicalUser = TechnicalUser.Reader,
    ): String {
        val request = SingleRequest(companyId, dataType, reportingPeriod, comment)
        return GlobalAuth.withTechnicalUser(user) {
            apiAccessor.dataSourcingRequestControllerApi.createRequest(request).requestId
        }
    }

    /**
     * Creates a new company and a new request for that company, returning the IDs of both.
     */
    fun createNewCompanyAndRequestAndReturnTheirIds(): Pair<String, String> {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)

        val companyId = createNewCompanyAndReturnId()

        return Pair(companyId, createRequest(companyId))
    }
}

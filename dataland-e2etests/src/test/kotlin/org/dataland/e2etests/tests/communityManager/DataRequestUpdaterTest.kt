package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.communitymanager.openApiClient.model.StoredDataRequest
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyNonFinancialsData
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataRequestUpdaterTest {
    val apiAccessor = ApiAccessor()
    val jwtHelper = JwtAuthenticationHelper()
    private val requestControllerApi = apiAccessor.requestControllerApi
    private val dataController = apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
    private lateinit var dummyCompanyAssociatedData: CompanyAssociatedDataEutaxonomyNonFinancialsData
    private val testDataEuTaxonomyNonFinancials = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getTData(1).first()

    private val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getCompanyInformationWithoutIdentifiers(1).first()

    @BeforeAll
    fun authenticateAsReader() { jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader) }

    @Test
    fun `post single data request and provide data approve it and change the request status`() {
        val mapOfIds = apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
            testCompanyInformation,
            testDataEuTaxonomyNonFinancials,
        )

        val singleDataRequest = SingleDataRequest(
            companyIdentifier = mapOfIds["companyId"].toString(),
            dataType = SingleDataRequest.DataType.eutaxonomyMinusNonMinusFinancials,
            reportingPeriods = setOf("2022", "2023"),
            contacts = setOf("someContact@webserver.de", "valid@e.mail"),
            message = "This is a test. The current timestamp is ${System.currentTimeMillis()}",
        )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val allStoredDataRequests = requestControllerApi.postSingleDataRequest(singleDataRequest)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        for (storedDataRequest in allStoredDataRequests) {
            val retrievedDataRequest = requestControllerApi.getDataRequestById(
                UUID.fromString(storedDataRequest.dataRequestId),
            )
            Assertions.assertEquals(storedDataRequest.requestStatus, retrievedDataRequest.requestStatus)
        }

        uploadDataset(mapOfIds)
        Thread.sleep(1000)
        for (storedDataRequest in allStoredDataRequests) {
            val retrievedDataRequest = requestControllerApi.getDataRequestById(
                UUID.fromString(storedDataRequest.dataRequestId),
            )
            checkRequestStatusAfterUpload(retrievedDataRequest)
        }
    }
    private fun uploadDataset(mapOfIds: Map<String, String>) {
        dummyCompanyAssociatedData =
            CompanyAssociatedDataEutaxonomyNonFinancialsData(
                mapOfIds["companyId"].toString(),
                "2022",
                testDataEuTaxonomyNonFinancials,
            )
        dataController.postCompanyAssociatedEutaxonomyNonFinancialsData(
            dummyCompanyAssociatedData, true,
        )
    }

    private fun checkRequestStatusAfterUpload(retrievedDataRequest: StoredDataRequest) {
        if (retrievedDataRequest.reportingPeriod == "2022") {
            Assertions.assertEquals(RequestStatus.answered, retrievedDataRequest.requestStatus)
        } else {
            Assertions.assertEquals(RequestStatus.open, retrievedDataRequest.requestStatus)
        }
    }
}

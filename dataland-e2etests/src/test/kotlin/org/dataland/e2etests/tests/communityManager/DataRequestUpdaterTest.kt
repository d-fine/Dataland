package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
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
import org.junit.jupiter.api.assertThrows
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
    private val clientError403 = "Client error : 403 "

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

    @Test
    fun `patch your own answered data request as a premiumUser to closed`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val stringThatMatchesThePermIdRegex = System.currentTimeMillis().toString()
        val singleDataRequest = SingleDataRequest(
            companyIdentifier = stringThatMatchesThePermIdRegex,
            dataType = SingleDataRequest.DataType.lksg,
            reportingPeriods = setOf("2022"),
        )

        val storedDataRequest = requestControllerApi.postSingleDataRequest(singleDataRequest).first()
        val dataRequestId = UUID.fromString(storedDataRequest.dataRequestId)
        Assertions.assertEquals(RequestStatus.open, storedDataRequest.requestStatus)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)

        val answeredDataRequest = requestControllerApi.patchDataRequestStatus(dataRequestId, RequestStatus.answered)
        Assertions.assertEquals(RequestStatus.answered, answeredDataRequest.requestStatus)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)

        val closedDataRequest = requestControllerApi.patchDataRequestStatus(dataRequestId, RequestStatus.closed)
        Assertions.assertEquals(RequestStatus.closed, closedDataRequest.requestStatus)
    }

    @Test
    fun `patch a non-owned, answered data request as a premiumUser and assert that it is forbidden`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)

        val stringThatMatchesThePermIdRegex = System.currentTimeMillis().toString()
        val singleDataRequest = SingleDataRequest(
            companyIdentifier = stringThatMatchesThePermIdRegex,
            dataType = SingleDataRequest.DataType.lksg,
            reportingPeriods = setOf("2022"),
        )

        val storedDataRequest = requestControllerApi.postSingleDataRequest(singleDataRequest).first()
        val dataRequestId = UUID.fromString(storedDataRequest.dataRequestId)

        val answeredDataRequest = requestControllerApi.patchDataRequestStatus(dataRequestId, RequestStatus.answered)
        Assertions.assertEquals(RequestStatus.answered, answeredDataRequest.requestStatus)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)

        for (requestStatus in RequestStatus.entries) {
            val clientException = assertThrows<ClientException> {
                requestControllerApi.patchDataRequestStatus(dataRequestId, requestStatus)
            }
            Assertions.assertEquals(clientError403, clientException.message)
        }
    }

    @Test
    fun `patch your own open data request as a premiumUser and assert that it is forbidden`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)

        val stringThatMatchesThePermIdRegex = System.currentTimeMillis().toString()
        val singleDataRequest = SingleDataRequest(
            companyIdentifier = stringThatMatchesThePermIdRegex,
            dataType = SingleDataRequest.DataType.lksg,
            reportingPeriods = setOf("2022"),
        )

        val storedDataRequest = requestControllerApi.postSingleDataRequest(singleDataRequest).first()
        val dataRequestId = UUID.fromString(storedDataRequest.dataRequestId)
        Assertions.assertEquals(RequestStatus.open, storedDataRequest.requestStatus)

        for (requestStatus in RequestStatus.entries) {
            val clientException: ClientException = assertThrows<ClientException> {
                requestControllerApi.patchDataRequestStatus(dataRequestId, requestStatus)
            }
            Assertions.assertEquals(clientError403, clientException.message)
        }
    }

    @Test
    fun `patch your own closed data request as a premiumUser and assert that it is forbidden`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)

        val stringThatMatchesThePermIdRegex = System.currentTimeMillis().toString()
        val singleDataRequest = SingleDataRequest(
            companyIdentifier = stringThatMatchesThePermIdRegex,
            dataType = SingleDataRequest.DataType.lksg,
            reportingPeriods = setOf("2022"),
        )

        val storedDataRequest = requestControllerApi.postSingleDataRequest(singleDataRequest).first()
        val dataRequestId = UUID.fromString(storedDataRequest.dataRequestId)
        Assertions.assertEquals(RequestStatus.open, storedDataRequest.requestStatus)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)

        val closedDataRequest = requestControllerApi.patchDataRequestStatus(dataRequestId, RequestStatus.closed)
        Assertions.assertEquals(RequestStatus.closed, closedDataRequest.requestStatus)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        for (requestStatus in RequestStatus.entries) {
            val clientException: ClientException = assertThrows<ClientException> {
                requestControllerApi.patchDataRequestStatus(dataRequestId, requestStatus)
            }
            Assertions.assertEquals(clientError403, clientException.message)
        }
    }
}

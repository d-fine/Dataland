package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForNonFinancials
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
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
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)
    private val dataController = apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
    lateinit var dummyCompanyAssociatedData: CompanyAssociatedDataEuTaxonomyDataForNonFinancials
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
// created two single data requests
        val singleDataRequest = SingleDataRequest(
            companyIdentifier = mapOfIds["companyId"]!!.toString(),
            frameworkName = SingleDataRequest.FrameworkName.eutaxonomyMinusNonMinusFinancials,
            listOfReportingPeriods = listOf("2022", "2023"),
            contactList = listOf("someContact@webserver.de", "simpleString"),
            message = "This is a test. The current timestamp is ${System.currentTimeMillis()}",
        )
// post two singe data requests

        val allStoredDataRequests = requestControllerApi.postSingleDataRequest(singleDataRequest)

// changed the technical user
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
//       getting the
        for (storedDataRequest in allStoredDataRequests) {
            val retrievedDataRequest = requestControllerApi.getDataRequestById(
                UUID.fromString(storedDataRequest.dataRequestId),
            )
            println(storedDataRequest.dataRequestId)
            println("Starting request status: " + retrievedDataRequest.requestStatus)
            Assertions.assertEquals(storedDataRequest, retrievedDataRequest)
        }
        dummyCompanyAssociatedData =
            CompanyAssociatedDataEuTaxonomyDataForNonFinancials(
                mapOfIds["companyId"]!!.toString(),
                "2022",
                testDataEuTaxonomyNonFinancials,
            )
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val testQaStatus = uploadDatasetAndValidatePendingState()
        Thread.sleep(1000)
        println(apiAccessor.metaDataControllerApi.getDataMetaInfo(testQaStatus).qaStatus)
        for (storedDataRequest in allStoredDataRequests) {
            val retrievedDataRequest = requestControllerApi.getDataRequestById(
                UUID.fromString(storedDataRequest.dataRequestId),
            )
            println("Ending request status: " + retrievedDataRequest.requestStatus)
            Assertions.assertEquals(storedDataRequest, retrievedDataRequest)
        }
    }
    private fun uploadDatasetAndValidatePendingState(): String {
        val dataId = dataController.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
            dummyCompanyAssociatedData, true,
        ).dataId
        return dataId
    }
}

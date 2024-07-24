package org.dataland.e2etests.tests.communityManager.accessRequests

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.model.AccessStatus
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.datalandbackend.openApiClient.api.VsmeDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.VsmeData
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.FrameworkTestDataProvider
import org.dataland.e2etests.utils.communityManager.getNewlyStoredRequestsAfterTimestamp
import org.dataland.e2etests.utils.communityManager.retrieveTimeAndWaitOneMillisecond
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class accessToActiveMatchingDatasetTest {

    val apiAccessor = ApiAccessor()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)
    val jwtHelper = JwtAuthenticationHelper()
    private val testDataEuTaxonomyNonFinancials = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getTData(1).first()

    private val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getCompanyInformationWithoutIdentifiers(1).first()

    private val vsmeDataControllerApi = VsmeDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val testVsmeData = FrameworkTestDataProvider(VsmeData::class.java).getTData(1).first()

    @BeforeEach
    fun authenticateAsPremiumUser() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
    }

    @Test
    fun privateFrameworkHasAccess() {
        val stringThatMatchesThePermIdRegex = System.currentTimeMillis().toString()

        val singleDataRequest = SingleDataRequest(
            companyIdentifier = stringThatMatchesThePermIdRegex,
            dataType = SingleDataRequest.DataType.vsme,
            reportingPeriods = setOf("2022"),
            contacts = setOf("someContact@example.com"),
            message = "This is a test. The current timestamp is ${System.currentTimeMillis()}",
        )
        val timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
        val response = requestControllerApi.postSingleDataRequest(singleDataRequest)
        //TODO
        // as admin lade company daten hoch, den admin zum comp-owner machen > vsme daten hochladen (aus vsme.kt)

        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)
        newlyStoredRequests.forEach {
            assertEquals(
                RequestStatus.Open, it.requestStatus, "The status of a newly stored data request is not 'Open'.",
            )
        }
        val dataRequestAccess = requestControllerApi.getDataRequestsForRequestingUser()
        // als admin: requestControllerApi.patchDataRequest()
        dataRequestAccess[0].accessStatus

        newlyStoredRequests.forEach{
            //it.accessStatus = AccessStatus.Granted
        }
    }

}
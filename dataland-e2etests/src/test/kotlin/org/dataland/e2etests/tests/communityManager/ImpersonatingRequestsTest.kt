package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ClientError
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest
import org.dataland.communitymanager.openApiClient.model.SingleDataRequest.DataType
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.communityManager.generateRandomLei
import org.dataland.e2etests.utils.communityManager.getIdForUploadedCompanyWithIdentifiers
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImpersonatingRequestsTest {
    val jwtHelper = JwtAuthenticationHelper()
    val apiAccessor = ApiAccessor()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

    private val dataReaderUserId = "18b67ecc-1176-4506-8414-1e81661017ca"
    private val sampleDataType = DataType.eutaxonomyMinusFinancials

    @BeforeAll
    fun authenticateAsAdmin() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
    }

    @Test
    fun `check that single data requests by admins are not subject to a daily quota check`() {
        var counter = 0
        val sampleReportingPeriod = 2020
        val sampleReportingPeriods: MutableSet<String> = mutableSetOf()
        while (counter < 11) {
            sampleReportingPeriods.add("${sampleReportingPeriod + counter}")
            counter++
        }
        val randomLei = generateRandomLei()
        val uploadedCompanyId =
            getIdForUploadedCompanyWithIdentifiers(
                lei = randomLei,
            )
        val singleDataRequest =
            SingleDataRequest(
                companyIdentifier = uploadedCompanyId,
                dataType = sampleDataType,
                reportingPeriods = sampleReportingPeriods,
                contacts = null,
                message = null,
            )
        requestControllerApi.postSingleDataRequest(
            singleDataRequest,
        )
    }

    @Test
    fun `check that impersonating requests for non premium users lead to daily quota checks`() {
        var counter = 0
        val sampleReportingPeriod = 2020
        val sampleReportingPeriods: MutableSet<String> = mutableSetOf()
        while (counter < 11) {
            sampleReportingPeriods.add("${sampleReportingPeriod + counter}")
            counter++
        }
        val randomLei = generateRandomLei()
        val uploadedCompanyId =
            getIdForUploadedCompanyWithIdentifiers(
                lei = randomLei,
            )
        val singleDataRequest =
            SingleDataRequest(
                companyIdentifier = uploadedCompanyId,
                dataType = sampleDataType,
                reportingPeriods = sampleReportingPeriods,
                contacts = null,
                message = null,
            )
        val exception =
            assertThrows<ClientException> {
                requestControllerApi.postSingleDataRequest(
                    singleDataRequest, dataReaderUserId,
                )
            }

        val responseBody = (exception.response as ClientError<*>).body as String
        assertTrue(
            responseBody.contains(
                "The daily quota capacity has been reached.",
            ),
        )
    }
}

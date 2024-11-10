package org.dataland.e2etests.tests.dataPoints

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackend.openApiClient.model.UploadableDataPoint
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SingleDataPoint {
    private val dataPointControllerApi = DataPointControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val apiAccessor = ApiAccessor()
    private val listOfOneCompanyInformation = apiAccessor.testDataProviderForSfdrData.getCompanyInformationWithoutIdentifiers(1)

    @Test
    fun `up- and download a single data point`() {
        val jwtHelper = JwtAuthenticationHelper()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)

        val storedCompanyInfos = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND).postCompany(listOfOneCompanyInformation.first())
        val dataPointContent =
            """
            "value": 0.5,
            "currency": "USD"
            """.trimIndent()
        val uploadableDataPoint =
            UploadableDataPoint(
                dataPointContent = dataPointContent,
                dataPointIdentifier = "extendedCurrencyEquity",
                companyId = UUID.fromString(storedCompanyInfos.companyId),
                reportingPeriod = "2022",
            )
        val dataPointId = dataPointControllerApi.postDataPoint(uploadableDataPoint, false).dataId
        val downloadedDataPoint = dataPointControllerApi.getDataPoint(dataPointId).data
        assertEquals(dataPointContent, downloadedDataPoint)
    }
}

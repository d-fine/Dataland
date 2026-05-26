package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.ExtendedDataPointBigDecimal
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandbackend.openApiClient.model.SfdrEnvironmental
import org.dataland.datalandbackend.openApiClient.model.SfdrEnvironmentalGreenhouseGasEmissions
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.api.ApiAwait
import org.dataland.e2etests.utils.api.Backend
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.math.BigDecimal

class AssembledDataControllerTest {
    private val apiAccessor = ApiAccessor()

    @Test
    fun `upload sfdr dataset without calculated field and verify it is computed on retrieval`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val reportingPeriod = "2025"

        val scope1 = BigDecimal("1.0")
        val scope2 = BigDecimal("2.0")

        val sfdrData =
            SfdrData(
                environmental =
                    SfdrEnvironmental(
                        greenhouseGasEmissions =
                            SfdrEnvironmentalGreenhouseGasEmissions(
                                scope1GhgEmissionsInTonnes = ExtendedDataPointBigDecimal(value = scope1),
                                scope2GhgEmissionsInTonnes = ExtendedDataPointBigDecimal(value = scope2),
                                scope1And2GhgEmissionsInTonnes = null,
                            ),
                    ),
            )

        Backend.sfdrDataControllerApi.postCompanyAssociatedSfdrData(
            CompanyAssociatedDataSfdrData(companyId, reportingPeriod, sfdrData),
            bypassQa = true,
        )

        ApiAwait.untilAsserted(retryOnHttpErrors = setOf(HttpStatus.NOT_FOUND)) {
            val response =
                Backend.sfdrDataControllerApi
                    .getCompanyAssociatedSfdrDataByDimensions(reportingPeriod, companyId)

            val calculatedField =
                response.data
                    .environmental
                    ?.greenhouseGasEmissions
                    ?.scope1And2GhgEmissionsInTonnes

            assertNotNull(calculatedField, "Calculated field scope1And2GhgEmissionsInTonnes should be present")
            assertEquals(
                0, BigDecimal("3.0").compareTo(calculatedField!!.value),
                "Expected 3.0 as calculated sum but got ${calculatedField.value}",
            )
        }
    }
}

package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyFinancials202673Data
import org.dataland.datalandbackend.openApiClient.model.CurrencyDataPoint
import org.dataland.datalandbackend.openApiClient.model.EutaxonomyFinancials202673Data
import org.dataland.datalandbackend.openApiClient.model.EutaxonomyFinancials202673InsuranceReinsurance
import org.dataland.datalandbackend.openApiClient.model.EutaxonomyFinancials202673InsurancereinsuranceUnderwritingKpi
import org.dataland.datalandbackend.openApiClient.model.UploadedDataPoint
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.api.ApiAwait
import org.dataland.e2etests.utils.api.Backend
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.math.BigDecimal

class AssembledDataControllerTest {
    private val apiAccessor = ApiAccessor()
    private val taxonomyEligibleButNonAlignedPremiumsDataPointType =
        "extendedCurrencyInsuranceReinsuranceTotalOfAbsolutePremiumsOfTaxonomyEligibleButTaxonomyNonAlignedActivities"

    private fun assertBigDecimalEquals(
        expected: BigDecimal,
        actual: BigDecimal?,
    ) {
        val actualValue = actual ?: fail("Expected BigDecimal value to be present")
        assertEquals(
            0,
            expected.compareTo(actualValue),
            "Expected $expected but got $actualValue",
        )
    }

    @Test
    @Suppress("LongMethod")
    fun `upload eutaxonomy financials dataset without calculated field and verify it is computed on retrieval`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val reportingPeriod = "2025"
        val currency = "EUR"

        val alignedPremiums = BigDecimal("0.5")
        val eligibleButNonAlignedPremiums = BigDecimal("1.0")
        val expectedEligiblePremiums = alignedPremiums + eligibleButNonAlignedPremiums

        val eutaxonomyFinancialsData =
            EutaxonomyFinancials202673Data(
                insuranceReinsurance =
                    EutaxonomyFinancials202673InsuranceReinsurance(
                        underwritingKpi =
                            EutaxonomyFinancials202673InsurancereinsuranceUnderwritingKpi(
                                totalOfAbsolutePremiumsOfTaxonomyAlignedActivities =
                                    CurrencyDataPoint(
                                        value = alignedPremiums,
                                        currency = currency,
                                    ),
                                totalOfAbsolutePremiumsOfTaxonomyEligibleActivities = null,
                            ),
                    ),
            )

        Backend.eutaxonomyFinancials202673DataControllerApi
            .postCompanyAssociatedEutaxonomyFinancials202673Data(
                CompanyAssociatedDataEutaxonomyFinancials202673Data(
                    companyId = companyId,
                    reportingPeriod = reportingPeriod,
                    data = eutaxonomyFinancialsData,
                ),
                bypassQa = true,
            )

        Backend.dataPointControllerApi
            .postDataPoint(
                UploadedDataPoint(
                    dataPoint = """{"value": $eligibleButNonAlignedPremiums, "currency": "$currency"}""",
                    dataPointType = taxonomyEligibleButNonAlignedPremiumsDataPointType,
                    companyId = companyId,
                    reportingPeriod = reportingPeriod,
                ),
                bypassQa = true,
            )

        ApiAwait.untilAsserted(retryOnHttpErrors = setOf(HttpStatus.NOT_FOUND)) {
            val response =
                Backend.eutaxonomyFinancials202673DataControllerApi
                    .getCompanyAssociatedEutaxonomyFinancials202673DataByDimensions(reportingPeriod, companyId)

            val calculatedField =
                response.data
                    .insuranceReinsurance
                    ?.underwritingKpi
                    ?.totalOfAbsolutePremiumsOfTaxonomyEligibleActivities
                    ?: fail("Calculated field totalOfAbsolutePremiumsOfTaxonomyEligibleActivities should be present")

            assertBigDecimalEquals(expectedEligiblePremiums, calculatedField.value)
            assertEquals(
                currency,
                calculatedField.currency,
                "Expected calculated field currency to be $currency",
            )
        }
    }
}

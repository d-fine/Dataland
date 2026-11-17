package org.dataland.e2etests.tests

import org.awaitility.Awaitility
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataPcafData
import org.dataland.datalandbackend.openApiClient.model.ExtendedDataPointBigDecimal
import org.dataland.datalandbackend.openApiClient.model.PcafData
import org.dataland.datalandbackend.openApiClient.model.PcafFinancial
import org.dataland.datalandbackend.openApiClient.model.PcafFinancialFinancial
import org.dataland.datalandbackend.openApiClient.model.PcafFinancialListedCompany
import org.dataland.datalandbackend.openApiClient.model.PcafFinancialUnlistedCompany
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class PcafCalculatedFieldsTest {
    private val apiAccessor = ApiAccessor()
    private val reportingPeriod = "2025"

    private fun dataPoint(value: Int) = ExtendedDataPointBigDecimal(value = value.toBigDecimal())

    private fun uploadInputsOnly(): Pair<String, String> {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val data =
            PcafData(
                financial =
                    PcafFinancial(
                        listedCompany =
                            PcafFinancialListedCompany(
                                marketCapitalizationInEUR = dataPoint(1000),
                                minoritiesInterestInEUR = dataPoint(30),
                            ),
                        financial = PcafFinancialFinancial(bookValueOfDebtInEUR = dataPoint(200)),
                        unlistedCompany = PcafFinancialUnlistedCompany(totalEquityInEUR = dataPoint(500)),
                    ),
            )
        val dataId =
            apiAccessor.dataControllerApiForPcafData
                .postCompanyAssociatedPcafData(CompanyAssociatedDataPcafData(companyId, reportingPeriod, data), bypassQa = true)
                .dataId
        return companyId to dataId
    }

    private fun assertValue(
        expected: Int,
        actual: ExtendedDataPointBigDecimal?,
    ) {
        assertNotNull(actual)
        assertEquals(0, BigDecimal(expected).compareTo(actual!!.value))
        assertTrue(actual.comment!!.contains("calculated using the following formula"))
    }

    @Test
    fun `ensure that enterprise value and total equity and debt are calculated from their inputs`() {
        val (companyId, dataId) = uploadInputsOnly()

        Awaitility.await().atMost(5000, TimeUnit.MILLISECONDS).pollDelay(1000, TimeUnit.MILLISECONDS).untilAsserted {
            val financial =
                apiAccessor.dataControllerApiForPcafData
                    .getCompanyAssociatedPcafDataByDimensions(reportingPeriod, companyId)
                    .data.financial
            assertValue(1230, financial?.listedCompany?.enterpriseValueInEUR)
            assertValue(700, financial?.unlistedCompany?.totalEquityAndDebtInEUR)
        }

        val stored =
            apiAccessor.dataControllerApiForPcafData
                .getCompanyAssociatedPcafData(dataId)
                .data.financial
        assertNull(stored?.listedCompany?.enterpriseValueInEUR)
        assertNull(stored?.unlistedCompany?.totalEquityAndDebtInEUR)
    }
}

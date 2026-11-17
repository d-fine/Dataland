package org.dataland.e2etests.tests.dataPoints

import org.awaitility.Awaitility
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.ExtendedDataPointBigDecimal
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandbackend.openApiClient.model.SfdrEnvironmental
import org.dataland.datalandbackend.openApiClient.model.SfdrEnvironmentalGreenhouseGasEmissions
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.api.Backend
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class AssembledDatasetCalculationTest {
    private val apiAccessor = ApiAccessor()
    private val dummyReportingPeriod = "2025"

    private fun uploadSfdrDataset(
        companyId: String,
        dataset: SfdrData,
    ): DataMetaInformation =
        Backend.sfdrDataControllerApi.postCompanyAssociatedSfdrData(
            CompanyAssociatedDataSfdrData(companyId = companyId, reportingPeriod = dummyReportingPeriod, data = dataset),
            bypassQa = true,
        )

    @Test
    fun `ensure that calculated fields are derived by dimensions but not returned by dataset id`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val inputDataset =
            SfdrData(
                environmental =
                    SfdrEnvironmental(
                        greenhouseGasEmissions =
                            SfdrEnvironmentalGreenhouseGasEmissions(
                                scope1GhgEmissionsInTonnes = ExtendedDataPointBigDecimal(value = 100.toBigDecimal()),
                                scope2GhgEmissionsInTonnes = ExtendedDataPointBigDecimal(value = 50.toBigDecimal()),
                            ),
                    ),
            )
        val dataMetaInformation = uploadSfdrDataset(companyId, inputDataset)

        Awaitility.await().atMost(5000, TimeUnit.MILLISECONDS).pollDelay(1000, TimeUnit.MILLISECONDS).untilAsserted {
            val calculated =
                Backend.sfdrDataControllerApi
                    .getCompanyAssociatedSfdrDataByDimensions(dataMetaInformation.reportingPeriod, companyId)
                    .data.environmental
                    ?.greenhouseGasEmissions
                    ?.scope1And2GhgEmissionsInTonnes
            assertNotNull(calculated)
            assertEquals(0, 150.toBigDecimal().compareTo(calculated!!.value))
            assertTrue(calculated.comment!!.contains("calculated using the following formula"))
        }

        val byDataId =
            Backend.sfdrDataControllerApi
                .getCompanyAssociatedSfdrData(dataMetaInformation.dataId)
                .data.environmental
                ?.greenhouseGasEmissions
        assertNull(byDataId?.scope1And2GhgEmissionsInTonnes)
    }

    @Test
    fun `ensure that a directly entered value takes precedence over the calculation`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val inputDataset =
            SfdrData(
                environmental =
                    SfdrEnvironmental(
                        greenhouseGasEmissions =
                            SfdrEnvironmentalGreenhouseGasEmissions(
                                scope1GhgEmissionsInTonnes = ExtendedDataPointBigDecimal(value = 100.toBigDecimal()),
                                scope2GhgEmissionsInTonnes = ExtendedDataPointBigDecimal(value = 50.toBigDecimal()),
                                scope1And2GhgEmissionsInTonnes = ExtendedDataPointBigDecimal(value = 7.toBigDecimal()),
                            ),
                    ),
            )
        val dataMetaInformation = uploadSfdrDataset(companyId, inputDataset)

        Awaitility.await().atMost(5000, TimeUnit.MILLISECONDS).pollDelay(1000, TimeUnit.MILLISECONDS).untilAsserted {
            val value =
                Backend.sfdrDataControllerApi
                    .getCompanyAssociatedSfdrDataByDimensions(dataMetaInformation.reportingPeriod, companyId)
                    .data.environmental
                    ?.greenhouseGasEmissions
                    ?.scope1And2GhgEmissionsInTonnes
                    ?.value
            assertEquals(0, 7.toBigDecimal().compareTo(value))
        }
    }
}

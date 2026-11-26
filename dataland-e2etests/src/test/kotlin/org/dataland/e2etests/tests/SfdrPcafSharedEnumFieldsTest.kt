package org.dataland.e2etests.tests

import org.awaitility.Awaitility
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.ExtendedDataPointSfdrGeneralCompanyCompanyExchangeStatusOptions
import org.dataland.datalandbackend.openApiClient.model.ExtendedDataPointSfdrGeneralCompanyMainPcafSectorOptions
import org.dataland.datalandbackend.openApiClient.model.PcafGeneralCompanyCompanyExchangeStatusOptions
import org.dataland.datalandbackend.openApiClient.model.PcafGeneralCompanyMainPcafSectorOptions
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandbackend.openApiClient.model.SfdrGeneral
import org.dataland.datalandbackend.openApiClient.model.SfdrGeneralCompany
import org.dataland.datalandbackend.openApiClient.model.SfdrGeneralCompanyCompanyExchangeStatusOptions
import org.dataland.datalandbackend.openApiClient.model.SfdrGeneralCompanyMainPcafSectorOptions
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

/**
 * PCAF datasets cannot be uploaded. SFDR and PCAF share the data point types of the main PCAF sector and the company
 * exchange status, so these values have to be available in PCAF as soon as they are uploaded via SFDR.
 */
class SfdrPcafSharedEnumFieldsTest {
    private val apiAccessor = ApiAccessor()
    private val reportingPeriod = "2025"

    private fun uploadSfdrDataset(
        mainPcafSector: SfdrGeneralCompanyMainPcafSectorOptions,
        companyExchangeStatus: SfdrGeneralCompanyCompanyExchangeStatusOptions,
    ): Pair<String, String> {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val data =
            SfdrData(
                general =
                    SfdrGeneral(
                        company =
                            SfdrGeneralCompany(
                                mainPcafSector = ExtendedDataPointSfdrGeneralCompanyMainPcafSectorOptions(value = mainPcafSector),
                                companyExchangeStatus =
                                    ExtendedDataPointSfdrGeneralCompanyCompanyExchangeStatusOptions(value = companyExchangeStatus),
                            ),
                    ),
            )
        val dataId =
            apiAccessor.dataControllerApiForSfdrData
                .postCompanyAssociatedSfdrData(CompanyAssociatedDataSfdrData(companyId, reportingPeriod, data), bypassQa = true)
                .dataId
        return companyId to dataId
    }

    @Test
    fun `ensure that main PCAF sector and company exchange status uploaded via SFDR are available in PCAF`() {
        val (companyId, _) =
            uploadSfdrDataset(SfdrGeneralCompanyMainPcafSectorOptions.RealEstate, SfdrGeneralCompanyCompanyExchangeStatusOptions.Listed)

        Awaitility.await().atMost(5000, TimeUnit.MILLISECONDS).pollDelay(1000, TimeUnit.MILLISECONDS).untilAsserted {
            val pcafCompany =
                apiAccessor.dataControllerApiForPcafData
                    .getCompanyAssociatedPcafDataByDimensions(reportingPeriod, companyId)
                    .data.general
                    ?.company
            assertEquals(PcafGeneralCompanyMainPcafSectorOptions.RealEstate, pcafCompany?.mainPcafSector?.value)
            assertEquals(PcafGeneralCompanyCompanyExchangeStatusOptions.Listed, pcafCompany?.companyExchangeStatus?.value)
        }
    }

    @Test
    fun `ensure that main PCAF sector and company exchange status are still stored and returned for SFDR`() {
        val (_, dataId) =
            uploadSfdrDataset(SfdrGeneralCompanyMainPcafSectorOptions.Public, SfdrGeneralCompanyCompanyExchangeStatusOptions.Unlisted)

        val sfdrCompany =
            apiAccessor.dataControllerApiForSfdrData
                .getCompanyAssociatedSfdrData(dataId)
                .data.general
                ?.company
        assertEquals(SfdrGeneralCompanyMainPcafSectorOptions.Public, sfdrCompany?.mainPcafSector?.value)
        assertEquals(SfdrGeneralCompanyCompanyExchangeStatusOptions.Unlisted, sfdrCompany?.companyExchangeStatus?.value)
    }
}

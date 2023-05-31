package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.openApiClient.model.QAStatus as BackendQaStatus
import org.dataland.datalandqaservice.openApiClient.model.QAStatus as QaServiceQaStatus
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class QaServiceTest {
    private val apiAccessor = ApiAccessor()

    private val testDataEuTaxonomyNonFinancials = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getTData(1).first()

    private val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getCompanyInformationWithoutIdentifiers(1).first()

    @Test
    fun `post a dummy company and a data set for it and check if that dummy data set can be retrieved`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val storedCompanyInfos = apiAccessor.companyDataControllerApi.postCompany(testCompanyInformation)
        val companyAssociatedEuTaxonomyNonFinancialsData =
            CompanyAssociatedDataEuTaxonomyDataForNonFinancials(
                storedCompanyInfos.companyId,
                "",
                testDataEuTaxonomyNonFinancials,
            )
        val dataId = apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
            .postCompanyAssociatedEuTaxonomyDataForNonFinancials(
                companyAssociatedEuTaxonomyNonFinancialsData, false,
            ).dataId

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)

        assertEquals(BackendQaStatus.pending, apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId).qaStatus)

        val qaServiceController = apiAccessor.qaServiceControllerApi
        assertTrue(qaServiceController.getUnreviewedDatasets().contains(dataId))
        qaServiceController.assignQualityStatus(dataId, QaServiceQaStatus.rejected)

        assertEquals(BackendQaStatus.pending, apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId).qaStatus)
    }
}

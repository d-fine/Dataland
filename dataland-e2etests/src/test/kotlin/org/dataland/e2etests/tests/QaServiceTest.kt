package org.dataland.e2etests.tests

import org.awaitility.Awaitility.await
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForNonFinancials
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit
import org.dataland.datalandbackend.openApiClient.model.QAStatus as BackendQaStatus
import org.dataland.datalandqaservice.openApiClient.model.QAStatus as QaServiceQaStatus

class QaServiceTest {
    private val apiAccessor = ApiAccessor()

    private val testDataEuTaxonomyNonFinancials = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getTData(1).first()

    private val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getCompanyInformationWithoutIdentifiers(1).first()

    @Test
    fun `post dummy data, qa it and check the qa status changes`() {
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

        assertEquals(BackendQaStatus.pending, apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId).qaStatus)

        val qaServiceController = apiAccessor.qaServiceControllerApi
        await().atMost(10, TimeUnit.SECONDS)
            .until { qaServiceController.getUnreviewedDatasets().contains(dataId) }
        qaServiceController.assignQualityStatus(dataId, QaServiceQaStatus.rejected)

        assertEquals(BackendQaStatus.rejected, apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId).qaStatus)
    }
}

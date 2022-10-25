package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataForFinancialsControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForFinancials
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForFinancials
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.TestDataProvider
import org.dataland.e2etests.accessmanagement.TokenHandler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EuTaxonomyFinancials {
    private val tokenHandler = TokenHandler()
    private val euTaxonomyDataForFinancialsControllerApi =
        EuTaxonomyDataForFinancialsControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    private val testDataProviderForEuTaxonomyDataForFinancials =
        TestDataProvider(EuTaxonomyDataForFinancials::class.java)

    private val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    private val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    private fun postOneCompanyAndEuTaxonomyDataForNonFinancials():
        Pair<DataMetaInformation, EuTaxonomyDataForFinancials> {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val testData = testDataProviderForEuTaxonomyDataForFinancials.getTData(1).first()
        val receivedCompanyId = companyDataControllerApi.postCompany(
            testDataProviderForEuTaxonomyDataForFinancials.getCompanyInformationWithoutIdentifiers(1).first()
        ).companyId
        val receivedDataMetaInformation = euTaxonomyDataForFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForFinancials(
            CompanyAssociatedDataEuTaxonomyDataForFinancials(receivedCompanyId, testData)
        )
        return Pair(
            DataMetaInformation(
                companyId = receivedCompanyId,
                dataId = receivedDataMetaInformation.dataId,
                dataType = receivedDataMetaInformation.dataType
            ),
            testData
        )
    }

    @Test
    fun `post a company with EuTaxonomyForFinancials data and check if the data can be retrieved correctly`() {
        val (receivedDataMetaInformation, uploadedData) = postOneCompanyAndEuTaxonomyDataForNonFinancials()
        val downloadedAssociatedData = euTaxonomyDataForFinancialsControllerApi
            .getCompanyAssociatedEuTaxonomyDataForFinancials(receivedDataMetaInformation.dataId)
        val downloadedAssociatedDataType = metaDataControllerApi.getDataMetaInfo(receivedDataMetaInformation.dataId)

        Assertions.assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType.dataType)
        // Sorting is required here as the backend models this field as a Set but this info is lost during the openApi
        // conversion
        Assertions.assertEquals(
            uploadedData.copy(financialServicesTypes = uploadedData.financialServicesTypes?.sorted()),
            downloadedAssociatedData.data?.copy(
                financialServicesTypes = downloadedAssociatedData.data?.financialServicesTypes?.sorted()
            )
        )
    }
}

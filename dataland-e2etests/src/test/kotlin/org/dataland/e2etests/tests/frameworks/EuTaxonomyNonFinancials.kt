package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataForNonFinancialsControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForNonFinancials
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.TestDataProvider
import org.dataland.e2etests.accessmanagement.TokenHandler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EuTaxonomyNonFinancials {
    private val tokenHandler = TokenHandler()
    private val euTaxonomyDataForNonFinancialsControllerApi =
        EuTaxonomyDataForNonFinancialsControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    private val testDataProviderForEuTaxonomyDataForNonFinancials =
        TestDataProvider(EuTaxonomyDataForNonFinancials::class.java)

    private val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    private val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    private fun postOneCompanyAndEuTaxonomyDataForNonFinancials():
        Pair<DataMetaInformation, EuTaxonomyDataForNonFinancials> {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val testData = testDataProviderForEuTaxonomyDataForNonFinancials.getTData(1).first()
        val receivedCompanyId = companyDataControllerApi.postCompany(
            testDataProviderForEuTaxonomyDataForNonFinancials.getCompanyInformationWithoutIdentifiers(1).first()
        ).companyId
        val receivedDataMetaInformation = euTaxonomyDataForNonFinancialsControllerApi
            .postCompanyAssociatedEuTaxonomyDataForNonFinancials(
                CompanyAssociatedDataEuTaxonomyDataForNonFinancials(receivedCompanyId, testData)
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
    fun `post a dummy company and a dummy data set for it and check if data Id appears in the companys meta data`() {
        val (testDataInformation, _) = postOneCompanyAndEuTaxonomyDataForNonFinancials()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val listOfDataMetaInfoForTestCompany = metaDataControllerApi.getListOfDataMetaInfo(
            testDataInformation.companyId,
            testDataInformation.dataType
        )
        Assertions.assertTrue(
            listOfDataMetaInfoForTestCompany.contains(
                DataMetaInformation(
                    testDataInformation.dataId,
                    testDataInformation.dataType,
                    testDataInformation.companyId
                )
            ),
            "The all-data-sets-list of the posted company does not contain the posted data set."
        )
    }

    @Test
    fun `post a company with EuTaxonomyForNonFinancials data and check if the data can be retrieved correctly`() {
        val (receivedDataMetaInformation, uploadedData) = postOneCompanyAndEuTaxonomyDataForNonFinancials()
        val downloadedAssociatedData = euTaxonomyDataForNonFinancialsControllerApi
            .getCompanyAssociatedEuTaxonomyDataForNonFinancials(receivedDataMetaInformation.dataId)
        val downloadedAssociatedDataType = metaDataControllerApi.getDataMetaInfo(receivedDataMetaInformation.dataId)

        Assertions.assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType.dataType)
        Assertions.assertEquals(uploadedData, downloadedAssociatedData.data)
    }
}

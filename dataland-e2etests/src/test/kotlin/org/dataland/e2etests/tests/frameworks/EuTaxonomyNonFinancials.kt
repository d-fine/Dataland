package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataForNonFinancialsControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForNonFinancials
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.utils.FrameworkTestDataProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EuTaxonomyNonFinancials {
    private val dataControllerApi = EuTaxonomyDataForNonFinancialsControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val testDataProvider = FrameworkTestDataProvider(EuTaxonomyDataForNonFinancials::class.java)
    private val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    @Test
    fun `post a dummy company and a dummy data set for it and check if data Id appears in the companys meta data`() {
        val (testDataInformation, _) = postOneCompanyAndItsData(
            testDataProvider, { data: CompanyAssociatedDataEuTaxonomyDataForNonFinancials ->
            dataControllerApi.postCompanyAssociatedEuTaxonomyDataForNonFinancials(data)
        }
        ) { companyId: String, data: EuTaxonomyDataForNonFinancials ->
            CompanyAssociatedDataEuTaxonomyDataForNonFinancials(companyId, data)
        }
        val listOfDataMetaInfoForTestCompany = metaDataControllerApi.getListOfDataMetaInfo(
            testDataInformation.companyId,
            testDataInformation.dataType
        )
        Assertions.assertTrue(
            listOfDataMetaInfoForTestCompany.contains(testDataInformation),
            "The all-data-sets-list of the posted company does not contain the posted data set."
        )
    }

    @Test
    fun `post a company with EuTaxonomyForNonFinancials data and check if the data can be retrieved correctly`() {
        val (receivedDataMetaInformation, uploadedData) = postOneCompanyAndItsData(
            testDataProvider, { data: CompanyAssociatedDataEuTaxonomyDataForNonFinancials ->
            dataControllerApi.postCompanyAssociatedEuTaxonomyDataForNonFinancials(data)
        }
        ) { companyId: String, data: EuTaxonomyDataForNonFinancials ->
            CompanyAssociatedDataEuTaxonomyDataForNonFinancials(companyId, data)
        }

        val downloadedAssociatedData = dataControllerApi
            .getCompanyAssociatedEuTaxonomyDataForNonFinancials(receivedDataMetaInformation.dataId)
        val downloadedAssociatedDataType = metaDataControllerApi.getDataMetaInfo(receivedDataMetaInformation.dataId)

        Assertions.assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType.dataType)
        Assertions.assertEquals(uploadedData, downloadedAssociatedData.data)
    }
}

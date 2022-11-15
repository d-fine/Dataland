package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataForFinancialsControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForFinancials
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForFinancials
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.FrameworkTestDataProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EuTaxonomyFinancials {
    private val dataControllerApi = EuTaxonomyDataForFinancialsControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val testDataProvider = FrameworkTestDataProvider(EuTaxonomyDataForFinancials::class.java)
    private val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    @Test
    fun `post a company with EuTaxonomyForFinancials data and check if the data can be retrieved correctly`() {
        val (receivedDataMetaInformation, uploadedData) = postOneCompanyAndItsData(
            testDataProvider, { data: CompanyAssociatedDataEuTaxonomyDataForFinancials ->
            dataControllerApi.postCompanyAssociatedEuTaxonomyDataForFinancials(data)
        }
        ) { companyId: String, data: EuTaxonomyDataForFinancials ->
            CompanyAssociatedDataEuTaxonomyDataForFinancials(companyId, data)
        }
        val downloadedAssociatedData = dataControllerApi
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

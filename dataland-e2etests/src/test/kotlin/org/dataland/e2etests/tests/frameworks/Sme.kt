package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.SmeDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSmeData
import org.dataland.datalandbackend.openApiClient.model.SmeData
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.utils.FrameworkTestDataProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Sme {

    private val smeDataControllerApi = SmeDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val testDataProviderForSmeData = FrameworkTestDataProvider(SmeData::class.java)
    private val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    @Test
    fun `post a company with SME data and check if the data can be retrieved correctly`() {
        val (receivedDataMetaInformation, uploadedData) = postOneCompanyAndItsData(
            testDataProviderForSmeData,
            { data: CompanyAssociatedDataSmeData -> smeDataControllerApi.postCompanyAssociatedSmeData(data) }
        ) { companyId: String, data: SmeData -> CompanyAssociatedDataSmeData(companyId, data) }

        val downloadedAssociatedData = smeDataControllerApi
            .getCompanyAssociatedSmeData(receivedDataMetaInformation.dataId)
        val downloadedAssociatedDataType = metaDataControllerApi.getDataMetaInfo(receivedDataMetaInformation.dataId)

        Assertions.assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType.dataType)
        Assertions.assertEquals(uploadedData, downloadedAssociatedData.data)
    }
}

package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.SfdrDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.FrameworkTestDataProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Sfdr {

    private val dataControllerApi = SfdrDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val testDataProvider = FrameworkTestDataProvider(SfdrData::class.java)
    private val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    @Test
    fun `post a company with Sfdr data and check if the data can be retrieved correctly`() {
        val (receivedDataMetaInformation, uploadedData) = postOneCompanyAndItsData(
            testDataProvider,
            { data: CompanyAssociatedDataSfdrData -> dataControllerApi.postCompanyAssociatedSfdrData(data) }
        ) { companyId: String, data: SfdrData -> CompanyAssociatedDataSfdrData(companyId, data) }

        val downloadedAssociatedData = dataControllerApi
            .getCompanyAssociatedSfdrData(receivedDataMetaInformation.dataId)
        val downloadedAssociatedDataType = metaDataControllerApi.getDataMetaInfo(receivedDataMetaInformation.dataId)

        Assertions.assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType.dataType)
        Assertions.assertEquals(uploadedData, downloadedAssociatedData.data)
    }
}

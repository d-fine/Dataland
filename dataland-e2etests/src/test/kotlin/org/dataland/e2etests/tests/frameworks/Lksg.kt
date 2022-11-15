package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.api.LksgDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataLksgData
import org.dataland.datalandbackend.openApiClient.model.LksgData
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.utils.FrameworkTestDataProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Lksg {

    private val lksgDataControllerApi = LksgDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val testDataProviderForLksgData = FrameworkTestDataProvider(LksgData::class.java)
    private val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    @Test
    fun `post a company with Lksg data and check if the data can be retrieved correctly`() {
        val (receivedDataMetaInformation, uploadedData) = postOneCompanyAndItsData(
            testDataProviderForLksgData,
            { data: CompanyAssociatedDataLksgData -> lksgDataControllerApi.postCompanyAssociatedLksgData(data) }
        ) { companyId: String, data: LksgData -> CompanyAssociatedDataLksgData(companyId, data) }

        val downloadedAssociatedData = lksgDataControllerApi
            .getCompanyAssociatedLksgData(receivedDataMetaInformation.dataId)
        val downloadedAssociatedDataType = metaDataControllerApi.getDataMetaInfo(receivedDataMetaInformation.dataId)

        Assertions.assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType.dataType)
        Assertions.assertEquals(uploadedData, downloadedAssociatedData.data)
    }
}

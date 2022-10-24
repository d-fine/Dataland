package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.LksgDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataLksgData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.LksgData
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.TestDataProvider
import org.dataland.e2etests.accessmanagement.TokenHandler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Lksg {
    private val tokenHandler = TokenHandler()
    private val lksgDataControllerApi =
        LksgDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    private val testDataProviderForLksgData =
        TestDataProvider(LksgData::class.java)

    private val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    private val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    private fun postOneCompanyAndLksg():
        Pair<DataMetaInformation, LksgData> {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val testData = testDataProviderForLksgData.getTData(1).first()
        val receivedCompanyId = companyDataControllerApi.postCompany(
            testDataProviderForLksgData.getCompanyInformationWithoutIdentifiers(1).first()
        ).companyId
        val receivedDataMetaInformation = lksgDataControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataLksgData(receivedCompanyId, testData)
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
    fun `post a company with Lksg data and check if the data can be retrieved correctly`() {
        val (receivedDataMetaInformation, uploadedData) = postOneCompanyAndLksg()
        val downloadedAssociatedData = lksgDataControllerApi
            .getCompanyAssociatedData(receivedDataMetaInformation.dataId)
        val downloadedAssociatedDataType = metaDataControllerApi.getDataMetaInfo(receivedDataMetaInformation.dataId)

        Assertions.assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType.dataType)
        Assertions.assertEquals(uploadedData, downloadedAssociatedData.data)
    }
}

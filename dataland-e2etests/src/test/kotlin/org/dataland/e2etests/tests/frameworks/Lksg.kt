package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.LksgDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.*
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

    private fun postOneCompanyAndLksg():
        Pair<DataMetaInformation, LksgData> {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val testData = testDataProviderForLksgData.getTData(1).first()
        val testDataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials
        val testCompanyId = companyDataControllerApi.postCompany(
            testDataProviderForLksgData.getCompanyInformationWithoutIdentifiers(1).first()
        ).companyId
        val testDataId = lksgDataControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataLksgData(testCompanyId, testData)
        ).dataId
        return Pair(
            DataMetaInformation(
                companyId = testCompanyId,
                dataId = testDataId,
                dataType = testDataType
            ),
            testData
        )
    }

    @Test
    fun `post a company with Lksg data and check if the data can be retrieved correctly`() {
        val (testDataInformation, uploadedData) = postOneCompanyAndLksg()
        val downloadedAssociatedData = lksgDataControllerApi
            .getCompanyAssociatedData(testDataInformation.dataId)

        Assertions.assertEquals(testDataInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(uploadedData, downloadedAssociatedData.data)
    }
}

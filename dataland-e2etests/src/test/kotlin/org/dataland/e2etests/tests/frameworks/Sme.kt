package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.SmeDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSmeData
import org.dataland.datalandbackend.openApiClient.model.SmeData
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.FrameworkTestDataProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Sme {

    private val apiAccessor = ApiAccessor()
    private val smeDataControllerApi = SmeDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val testDataProviderForSmeData = FrameworkTestDataProvider(SmeData::class.java)
    private val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    @Test
    fun `post a company with SME data and check if the data can be retrieved correctly`() {
        val listOfOneSmeDataSet = apiAccessor.testDataProviderForSmeData.getTData(1)
        val listOfOneCompanyInformation = apiAccessor.testDataProviderForSmeData
            .getCompanyInformationWithoutIdentifiers(1)
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            listOfOneCompanyInformation,
            listOfOneSmeDataSet,
            apiAccessor.smeUploaderFunction
        )
        val receivedDataMetaInformation = listOfUploadInfo[0].actualStoredDataMetaInfo
        val downloadedAssociatedData = apiAccessor.dataControllerApiForSmeData
            .getCompanyAssociatedSmeData(receivedDataMetaInformation!!.dataId)
        val downloadedAssociatedDataType = apiAccessor.metaDataControllerApi
            .getDataMetaInfo(receivedDataMetaInformation.dataId).dataType

        Assertions.assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType)
        Assertions.assertEquals(listOfOneSmeDataSet[0], downloadedAssociatedData.data)
    }
}

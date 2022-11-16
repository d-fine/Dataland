package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.SfdrDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.FrameworkTestDataProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Sfdr {

    private val apiAccessor = ApiAccessor()

    @Test
    fun `post a company with Sfdr data and check if the data can be retrieved correctly`() {
        val listOfOneSfdrDataSet = apiAccessor.testDataProviderForSfdrData.getTData(1)
        val listOfOneCompanyInformation = apiAccessor.testDataProviderForSfdrData
            .getCompanyInformationWithoutIdentifiers(1)
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            listOfOneCompanyInformation,
            listOfOneSfdrDataSet,
            apiAccessor.sfdrUploaderFunction
        )
        val receivedDataMetaInformation = listOfUploadInfo[0].actualStoredDataMetaInfo
        val downloadedAssociatedData = apiAccessor.dataControllerApiForSfdrData
            .getCompanyAssociatedSfdrData(receivedDataMetaInformation!!.dataId)
        val downloadedAssociatedDataType = apiAccessor.metaDataControllerApi
            .getDataMetaInfo(receivedDataMetaInformation.dataId).dataType

        Assertions.assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType)
        Assertions.assertEquals(listOfOneSfdrDataSet[0], downloadedAssociatedData.data)
    }
}

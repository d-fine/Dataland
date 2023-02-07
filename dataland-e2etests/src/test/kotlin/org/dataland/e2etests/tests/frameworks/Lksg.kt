package org.dataland.e2etests.tests.frameworks

import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Lksg {

    private val apiAccessor = ApiAccessor()

    private val listOfOneLksgDataSet = apiAccessor.testDataProviderForLksgData.getTData(1)
    private val listOfOneCompanyInformation = apiAccessor.testDataProviderForLksgData
        .getCompanyInformationWithoutIdentifiers(1)

    @Test
    fun `post a company with Lksg data and check if the data can be retrieved correctly`() {
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            listOfOneCompanyInformation,
            listOfOneLksgDataSet,
            apiAccessor.lksgUploaderFunction,
        )
        val receivedDataMetaInformation = listOfUploadInfo[0].actualStoredDataMetaInfo
        val downloadedAssociatedData = apiAccessor.dataControllerApiForLksgData
            .getCompanyAssociatedLksgData(receivedDataMetaInformation!!.dataId)
        val downloadedAssociatedDataType = apiAccessor.metaDataControllerApi
            .getDataMetaInfo(receivedDataMetaInformation.dataId).dataType

        Assertions.assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType)
        Assertions.assertEquals(listOfOneLksgDataSet[0], downloadedAssociatedData.data)
    }
}

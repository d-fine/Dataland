package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.model.LksgData
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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

        assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType)
        assertEquals(listOfOneLksgDataSet[0], downloadedAssociatedData.data)
    }

    @Test
    fun `check that reporting period and version history parameters of GET endpoint for companies work correctly`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val lksgData = apiAccessor.testDataProviderForLksgData.getTData(2)
        apiAccessor.repeatUploadWithWait<LksgData>(
            n = 2, companyId = companyId, dataList = lksgData, reportingPeriods = listOf("2022", "2023"),
            waitTime = 1000, uploadFunction = apiAccessor.lksgUploaderFunction,
        )
        val lksgDataSets = apiAccessor.dataControllerApiForLksgData.getAllCompanyLksgData(companyId, false)
        val activeLksgDatasets = apiAccessor.dataControllerApiForLksgData.getAllCompanyLksgData(companyId, true)
        val lksgDatasets2023 = apiAccessor.dataControllerApiForLksgData.getAllCompanyLksgData(companyId, false, "2023")
        val activeLksgDatasets2023 =
            apiAccessor.dataControllerApiForLksgData.getAllCompanyLksgData(companyId, true, "2023")
        assertTrue(
            lksgDataSets.size == 4 && activeLksgDatasets.size == 2 &&
                lksgDatasets2023.size == 2 && activeLksgDatasets2023.size == 1,
            "At least of the retrieved meta data lists does not have the expected size.",
        )
        assertEquals(activeLksgDatasets2023[0].data, lksgData[1], "Active dataset in 2023 not equal to latest upload.")
    }
}

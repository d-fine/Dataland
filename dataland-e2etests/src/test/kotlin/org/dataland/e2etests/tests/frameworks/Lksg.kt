package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.model.DataAndMetaInformationLksgData
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
        val (companyId, uploadedDataSets) = uploadFourDatasetsForACompany()
        val downLoadedDataSets = apiAccessor.dataControllerApiForLksgData.getAllCompanyLksgData(
            companyId = companyId,
            showOnlyActive = false,
        )
        val activeDownloadedDatasets = apiAccessor.dataControllerApiForLksgData.getAllCompanyLksgData(
            companyId = companyId,
            showOnlyActive = true,
        )
        val downloaded2023Datasets = apiAccessor.dataControllerApiForLksgData.getAllCompanyLksgData(
            companyId = companyId,
            showOnlyActive = false,
            reportingPeriod = "2023",
        )
        val downloadedActive2023Datasets =
            apiAccessor.dataControllerApiForLksgData.getAllCompanyLksgData(
                companyId = companyId,
                showOnlyActive = true,
                reportingPeriod = "2023",
            )
        assertDownloadedDatasets(
            downLoadedDataSets,
            activeDownloadedDatasets,
            downloaded2023Datasets,
            downloadedActive2023Datasets,
            uploadedDataSets,
        )
    }

    private fun assertDownloadedDatasets(
        downLoadedDataSets: List<DataAndMetaInformationLksgData>,
        activeDownloadedDatasets: List<DataAndMetaInformationLksgData>,
        downloaded2023Datasets: List<DataAndMetaInformationLksgData>,
        downloadedActive2023Datasets: List<DataAndMetaInformationLksgData>,
        uploadedDataSets: List<LksgData>,
    ) {
        assertTrue(
            downLoadedDataSets.size == 4 && activeDownloadedDatasets.size == 2 &&
                downloaded2023Datasets.size == 2 && downloadedActive2023Datasets.size == 1,
            "At least of the retrieved meta data lists does not have the expected size.",
        )
        assertEquals(
            downloadedActive2023Datasets[0].data,
            uploadedDataSets[1],
            "Active dataset in 2023 not equal to latest upload.",
        )
    }

    private fun uploadFourDatasetsForACompany(): Pair<String, List<LksgData>> {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val lksgData = apiAccessor.testDataProviderForLksgData.getTData(2)
        val uploadPairs = listOf(
            Pair(lksgData[0], "2022"), Pair(lksgData[0], "2022"), Pair(lksgData[1], "2023"), Pair(lksgData[1], "2023"),
        )
        uploadPairs.forEach { pair ->
            apiAccessor.uploadWithWait(
                companyId = companyId,
                frameworkData = pair.first,
                reportingPeriod = pair.second,
                uploadFunction = apiAccessor.lksgUploaderFunction,
            )
        }
        return Pair(companyId, lksgData)
    }
}

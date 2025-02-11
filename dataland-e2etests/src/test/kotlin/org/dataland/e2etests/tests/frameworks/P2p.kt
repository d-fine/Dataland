package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.model.PathwaysToParisData
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class P2p {
    private val apiAccessor = ApiAccessor()

    private val listOfOneP2pDataset = apiAccessor.testDataProviderForP2pData.getTData(1)
    private val listOfOneCompanyInformation =
        apiAccessor.testDataProviderForP2pData
            .getCompanyInformationWithoutIdentifiers(1)

    private fun getP2pDatasetWithSortedSectors(dataset: PathwaysToParisData): PathwaysToParisData =
        dataset.copy(
            general =
                dataset.general.copy(
                    general =
                        dataset.general.general.copy(
                            sectors =
                                dataset.general.general.sectors
                                    .sorted(),
                        ),
                ),
        )

    @Test
    fun `post a company with P2p data and check if the data can be retrieved correctly`() {
        val listOfUploadInfo =
            apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
                listOfOneCompanyInformation,
                listOfOneP2pDataset,
                apiAccessor::p2pUploaderFunction,
            )
        val receivedDataMetaInformation = listOfUploadInfo[0].actualStoredDataMetaInfo
        val downloadedAssociatedData =
            apiAccessor.dataControllerApiForP2pData
                .getCompanyAssociatedP2pData(receivedDataMetaInformation!!.dataId)
        val downloadedAssociatedDataType =
            apiAccessor.metaDataControllerApi
                .getDataMetaInfo(receivedDataMetaInformation.dataId)
                .dataType

        Assertions.assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType)
        Assertions.assertEquals(
            getP2pDatasetWithSortedSectors(listOfOneP2pDataset[0]),
            getP2pDatasetWithSortedSectors(downloadedAssociatedData.data),
        )
    }
}

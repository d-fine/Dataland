package org.dataland.e2etests.tests.frameworks
// TODO should be delete this completely and reintroduce it in the download story or leave it commented out
/*
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Sme {

    private val apiAccessor = ApiAccessor()

    private val listOfOneSmeDataSet = apiAccessor.testDataProviderForSmeData.getTData(1)
    private val listOfOneCompanyInformation = apiAccessor.testDataProviderForSmeData
        .getCompanyInformationWithoutIdentifiers(1)

    private fun sortNaturalHazardsCovered(dataset: SmeData): SmeData {
        return dataset.copy(
            insurances = dataset.insurances?.copy(
                naturalHazards = dataset.insurances?.naturalHazards?.copy(
                    naturalHazardsCovered = dataset.insurances?.naturalHazards?.naturalHazardsCovered?.sorted(),
                ),
            ),
        )
    }

    @Test
    fun `post a company with SME data and check if the data can be retrieved correctly`() {
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            listOfOneCompanyInformation,
            listOfOneSmeDataSet,
            apiAccessor::smeUploaderFunction,
        )
        val receivedDataMetaInformation = listOfUploadInfo[0].actualStoredDataMetaInfo
        val downloadedAssociatedData = apiAccessor.dataControllerApiForSmeData
            .getCompanyAssociatedSmeData(receivedDataMetaInformation!!.dataId)
        val downloadedAssociatedDataType = apiAccessor.metaDataControllerApi
            .getDataMetaInfo(receivedDataMetaInformation.dataId).dataType

        Assertions.assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType)

        Assertions.assertEquals(
            sortNaturalHazardsCovered(listOfOneSmeDataSet[0]),
            sortNaturalHazardsCovered(downloadedAssociatedData.data),
        )
    }
}
*/

package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentManagerAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Sfdr {

    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentManagerAccessor()

    private val listOfOneSfdrDataSet = apiAccessor.testDataProviderForSfdrData.getTData(1)
    private val listOfOneCompanyInformation = apiAccessor.testDataProviderForSfdrData
        .getCompanyInformationWithoutIdentifiers(1)

    @BeforeAll
    fun postTestDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    @Test
    fun `post a company with Sfdr data and check if the data can be retrieved correctly`() {
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            listOfOneCompanyInformation,
            listOfOneSfdrDataSet,
            apiAccessor::sfdrUploaderFunction,
        )
        val receivedDataMetaInformation = listOfUploadInfo[0].actualStoredDataMetaInfo
        val downloadedAssociatedData = apiAccessor.dataControllerApiForSfdrData
            .getCompanyAssociatedSfdrData(receivedDataMetaInformation!!.dataId)
        val downloadedAssociatedDataType = apiAccessor.metaDataControllerApi
            .getDataMetaInfo(receivedDataMetaInformation.dataId).dataType

        assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType)
        assertEquals(listOfOneSfdrDataSet[0], downloadedAssociatedData.data)
    }

    @Test
    fun `check that Sfdr dataset cannot be uploaded if document does not exist`() {
        //todo remove val companyId = "1908273127903192839781293898312983"
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val companyName = "TestForBrokenFileReference"

        val companyInformation = apiAccessor.testDataProviderForSfdrData.
        getSpecificCompanyByNameFromSfdrPreparedFixtures(companyName)

        val dataSet = companyInformation!!.t

        val uploadPair = Pair(dataSet, "2022")

        val exception = assertThrows<ClientException> {
            apiAccessor.uploadWithWait(
                companyId = companyId,
                frameworkData = uploadPair.first,
                reportingPeriod = uploadPair.second,
                uploadFunction = apiAccessor::sfdrUploaderFunction,
            )
        }

        val testClientError = exception.response as ClientError<*>

        assertTrue(testClientError.statusCode == 400)
        assertTrue(testClientError.body.toString().contains("Invalid input"))
        assertTrue(testClientError.body.toString().contains("The document reference doesn't exist"))


    }
}
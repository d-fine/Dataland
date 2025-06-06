package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandbackendutils.utils.JsonComparator
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentControllerApiAccessor
import org.dataland.e2etests.utils.api.ApiAwait
import org.dataland.e2etests.utils.assertEqualsByJsonComparator
import org.dataland.e2etests.utils.testDataProviders.FrameworkTestDataProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Sfdr {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentControllerApiAccessor()

    private val listOfOneSfdrDataset = apiAccessor.testDataProviderForSfdrData.getTData(1)
    private val listOfOneCompanyInformation =
        apiAccessor.testDataProviderForSfdrData
            .getCompanyInformationWithoutIdentifiers(1)

    @BeforeAll
    fun postTestDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    @Test
    fun `post a company with Sfdr data and check if the data can be retrieved correctly`() {
        val listOfUploadInfo =
            apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
                listOfOneCompanyInformation,
                listOfOneSfdrDataset,
                apiAccessor::sfdrUploaderFunction,
            )
        val receivedDataMetaInformation = listOfUploadInfo[0].actualStoredDataMetaInfo
        val downloadedAssociatedData =
            apiAccessor.dataControllerApiForSfdrData
                .getCompanyAssociatedSfdrData(receivedDataMetaInformation!!.dataId)
        val downloadedAssociatedDataType =
            apiAccessor.metaDataControllerApi
                .getDataMetaInfo(receivedDataMetaInformation.dataId)
                .dataType

        assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType)

        val ignoredKeys = setOf("publicationDate")
        assertEqualsByJsonComparator(
            listOfOneSfdrDataset[0],
            downloadedAssociatedData.data,
            JsonComparator.JsonComparisonOptions(ignoredKeys),
        )
    }

    @Test
    fun `check that an uploaded dataset can be retrieved by data dimensions`() {
        val listOfUploadInfo =
            apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
                listOfOneCompanyInformation,
                listOfOneSfdrDataset,
                apiAccessor::sfdrUploaderFunction,
            )

        val receivedDataMetaInformation = listOfUploadInfo[0].actualStoredDataMetaInfo
        Thread.sleep(2000)
        val downloadedAssociatedData =
            ApiAwait.waitForData(retryOnHttpErrors = setOf(HttpStatus.NOT_FOUND)) {
                apiAccessor.dataControllerApiForSfdrData
                    .getCompanyAssociatedSfdrDataByDimensions(
                        reportingPeriod = receivedDataMetaInformation!!.reportingPeriod,
                        companyId = receivedDataMetaInformation.companyId,
                    )
            }

        val ignoredKeys = setOf("publicationDate")
        assertEqualsByJsonComparator(
            listOfOneSfdrDataset[0],
            downloadedAssociatedData.data,
            JsonComparator.JsonComparisonOptions(ignoredKeys),
        )
    }

    @Test
    fun `check that Sfdr dataset cannot be uploaded if document does not exist`() {
        tryToUploadDataWithInvalidInputAndAssertThatItsForbidden(
            "TestForBrokenFileReference",
            "The document reference doesn't exist",
        )
    }

    @Test
    fun `check that Sfdr dataset cannot be uploaded if list of referenced Reports is incomplete`() {
        tryToUploadDataWithInvalidInputAndAssertThatItsForbidden(
            "TestForIncompleteReferencedReport",
            "The list of referenced reports is not complete.",
        )
    }

    private fun tryToUploadDataWithInvalidInputAndAssertThatItsForbidden(
        companyName: String,
        errorMessage: String,
    ) {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId

        val companyInformation =
            FrameworkTestDataProvider.forFrameworkPreparedFixtures(SfdrData::class.java).getByCompanyName(companyName)

        val dataset = companyInformation.t

        val uploadPair = Pair(dataset, "2022")

        val exception =
            assertThrows<ClientException> {
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
        assertTrue(testClientError.body.toString().contains(errorMessage))
    }
}

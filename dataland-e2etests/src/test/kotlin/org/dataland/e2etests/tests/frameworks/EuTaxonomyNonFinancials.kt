package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentManagerAccessor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EuTaxonomyNonFinancials {

    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentManagerAccessor()

    private val listOfOneEuTaxonomyNonFinancialsDataSet = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getTData(1)
    private val listOfOneCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getCompanyInformationWithoutIdentifiers(1)

    @BeforeAll
    fun postTestDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    @Test
    fun `post a dummy company and a dummy data set for it and check if data Id appears in the companys meta data`() {
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            listOfOneCompanyInformation, listOfOneEuTaxonomyNonFinancialsDataSet,
            apiAccessor::euTaxonomyNonFinancialsUploaderFunction,
        )
        val expectedDataMetaInformation = listOfUploadInfo[0].actualStoredDataMetaInfo
        val listOfDataMetaInfoForTestCompany = apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(
            expectedDataMetaInformation?.companyId,
            expectedDataMetaInformation?.dataType,
        )
        Assertions.assertTrue(
            listOfDataMetaInfoForTestCompany.contains(expectedDataMetaInformation),
            "The all-data-sets-list of the posted company does not contain the posted data set.",
        )
    }

    @Test
    fun `post a company with EuTaxonomyForNonFinancials data and check if the data can be retrieved correctly`() {
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            listOfOneCompanyInformation,
            listOfOneEuTaxonomyNonFinancialsDataSet,
            apiAccessor::euTaxonomyNonFinancialsUploaderFunction,
        )
        val receivedDataMetaInformation = listOfUploadInfo[0].actualStoredDataMetaInfo
        val downloadedAssociatedData = apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
            .getCompanyAssociatedEutaxonomyNonFinancialsData(receivedDataMetaInformation!!.dataId)
        val downloadedAssociatedDataType = apiAccessor.metaDataControllerApi
            .getDataMetaInfo(receivedDataMetaInformation.dataId).dataType

        Assertions.assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType)
        Assertions.assertEquals(listOfOneEuTaxonomyNonFinancialsDataSet[0], downloadedAssociatedData.data)
    }

    @Test
    fun `check that EuTaxonomyForNonFinancials data cannot be uploaded if list of referenced Reports is incomplete`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val companyName = "TestForIncompleteReferencedReport"

        val companyInformation = apiAccessor.testDataProviderForSfdrData
            .getSpecificCompanyByNameFromEuTaxonomyNonFinancialsPreparedFixtures(companyName)

        val dataSet = companyInformation!!.t

        val uploadPair = Pair(dataSet, "2024")

        val exception = assertThrows<ClientException> {
            apiAccessor.uploadWithWait(
                companyId = companyId,
                frameworkData = uploadPair.first,
                reportingPeriod = uploadPair.second,
                uploadFunction = apiAccessor::sfdrUploaderFunction,
            )
        }

        val testClientError = exception.response as ClientError<*>

        Assertions.assertTrue(testClientError.statusCode == 400)
        Assertions.assertTrue(testClientError.body.toString().contains("Invalid input"))
        Assertions.assertTrue(
            testClientError.body.toString().contains("The list of referenced reports is not complete.")
        )
    }
}

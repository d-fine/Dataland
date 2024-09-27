package org.dataland.e2etests.tests.frameworks

class EuTaxonomyFinancials {
    val a = "dummy" // TODO remove later
}
/* TODO deactivate for now
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.QaApiAccessor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EuTaxonomyFinancials {
    private val apiAccessor = ApiAccessor()

    private val listOfOneEuTaxonomyFinancialsDataSet = apiAccessor.testDataProviderEuTaxonomyForFinancials.getTData(1)

    /* Sorting is required in the last assertion as the backend models this field as a Set but this info is lost during
    the conversion */
    private val euTaxonomyFinancialsDataSetWithSortedFinancialServicesTypes = listOfOneEuTaxonomyFinancialsDataSet[0]
        .copy(financialServicesTypes = listOfOneEuTaxonomyFinancialsDataSet[0].financialServicesTypes?.sorted())
    private val listOfOneCompanyInformation = apiAccessor.testDataProviderEuTaxonomyForFinancials
        .getCompanyInformationWithoutIdentifiers(1)

    @Test
    fun `post a company with EuTaxonomyForFinancials data and check if the data can be retrieved correctly`() {
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            listOfOneCompanyInformation, listOfOneEuTaxonomyFinancialsDataSet,
            apiAccessor::euTaxonomyFinancialsUploaderFunction,
        )
        QaApiAccessor().ensureQaCompletedAndUpdateUploadInfo(listOfUploadInfo, apiAccessor.metaDataControllerApi)
        val receivedDataMetaInformation = listOfUploadInfo[0].actualStoredDataMetaInfo
        val downloadedAssociatedData = apiAccessor.dataControllerApiForEuTaxonomyFinancials
            .getCompanyAssociatedEuTaxonomyDataForFinancials(receivedDataMetaInformation!!.dataId)
        val downloadedAssociatedDataType = apiAccessor.metaDataControllerApi
            .getDataMetaInfo(receivedDataMetaInformation.dataId).dataType
        Assertions.assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType)
        Assertions.assertEquals(
            euTaxonomyFinancialsDataSetWithSortedFinancialServicesTypes,
            downloadedAssociatedData.data.copy(
                financialServicesTypes = downloadedAssociatedData.data.financialServicesTypes?.sorted(),
            ),
        )
    }

    @Test
    fun `check that EuTaxonomyForNonFinancials data cannot be uploaded if list of referenced Reports is incomplete`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val companyName = "TestForIncompleteReferencedReport"

        val companyInformation =
            apiAccessor.testDataProviderEuTaxonomyForFinancials
                .getSpecificCompanyByNameFromEuTaxonomyFinancialsPreparedFixtures(companyName)
        val dataSet = companyInformation!!.t

        val uploadPair = Pair(dataSet, "2023")

        val exception = assertThrows<ClientException> {
            apiAccessor.uploadWithWait(
                companyId = companyId,
                frameworkData = uploadPair.first,
                reportingPeriod = uploadPair.second,
                uploadFunction = apiAccessor::euTaxonomyFinancialsUploaderFunction,
            )
        }

        val testClientError = exception.response as ClientError<*>

        Assertions.assertTrue(testClientError.statusCode == 400)
        Assertions.assertTrue(testClientError.body.toString().contains("Invalid input"))
        Assertions.assertTrue(
            testClientError.body.toString().contains("The list of referenced reports is not complete."),
        )
    }*/

package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.QaApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EuTaxonomyFinancials {
    private val apiAccessor = ApiAccessor()

    private val euTaxoFinancialsDataset = apiAccessor.testDataProviderEuTaxonomyForFinancials.getTData(1)[0]

    private val companyInfo = apiAccessor.testDataProviderEuTaxonomyForFinancials
        .getCompanyInformationWithoutIdentifiers(1)[0]

    @Test
    fun `post a company with EuTaxonomyForFinancials data and check if the data can be retrieved correctly`() {
        val uploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            listOf(companyInfo), listOf(euTaxoFinancialsDataset),
            apiAccessor::euTaxonomyFinancialsUploaderFunction,
        )
        QaApiAccessor().ensureQaCompletedAndUpdateUploadInfo(uploadInfo, apiAccessor.metaDataControllerApi)

        val dataMetaInfoOfUpload = uploadInfo[0].actualStoredDataMetaInfo
        val dataId = dataMetaInfoOfUpload!!.dataId
        val downloadedMetaData = apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId)
        assertEquals(dataMetaInfoOfUpload, downloadedMetaData)

        val downloadedData = apiAccessor.dataControllerApiForEuTaxonomyFinancials
            .getCompanyAssociatedEutaxonomyFinancialsData(dataId).data
        assertEquals(euTaxoFinancialsDataset, downloadedData)
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

        assertTrue(testClientError.statusCode == 400)
        assertTrue(testClientError.body.toString().contains("Invalid input"))
        assertTrue(testClientError.body.toString().contains("The list of referenced reports is not complete."))
    }
}

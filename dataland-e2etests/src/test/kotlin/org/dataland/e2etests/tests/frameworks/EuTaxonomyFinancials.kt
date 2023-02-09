package org.dataland.e2etests.tests.frameworks

import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EuTaxonomyFinancials {
    private val apiAccessor = ApiAccessor()

    private val listOfOneEuTaxonomyFinancialsDataSet = apiAccessor.testDataProviderEuTaxonomyForFinancials.getTData(1)
    private val euTaxonomyFinancialsDataSetWithSortedFinancialServicesTypes = listOfOneEuTaxonomyFinancialsDataSet[0]
        .copy(financialServicesTypes = listOfOneEuTaxonomyFinancialsDataSet[0].financialServicesTypes?.sorted())
    private val listOfOneCompanyInformation = apiAccessor.testDataProviderEuTaxonomyForFinancials
        .getCompanyInformationWithoutIdentifiers(1)

    @Test
    fun `post a company with EuTaxonomyForFinancials data and check if the data can be retrieved correctly`() {
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            listOfOneCompanyInformation,
            listOfOneEuTaxonomyFinancialsDataSet,
            apiAccessor.euTaxonomyFinancialsUploaderFunction,
        )
        val receivedDataMetaInformation = listOfUploadInfo[0].actualStoredDataMetaInfo
        val downloadedAssociatedData = apiAccessor.dataControllerApiForEuTaxonomyFinancials
            .getCompanyAssociatedEuTaxonomyDataForFinancials(receivedDataMetaInformation!!.dataId)
        val downloadedAssociatedDataType = apiAccessor.metaDataControllerApi
            .getDataMetaInfo(receivedDataMetaInformation.dataId).dataType
        Assertions.assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType)
        Assertions.assertEquals(
            euTaxonomyFinancialsDataSetWithSortedFinancialServicesTypes,
            downloadedAssociatedData.data?.copy(
                financialServicesTypes = downloadedAssociatedData.data?.financialServicesTypes?.sorted(),
            ),
        )
    }
}
/* Sorting is required in the last assertion as the backend models this field as a Set but this info is lost during the
  conversion */

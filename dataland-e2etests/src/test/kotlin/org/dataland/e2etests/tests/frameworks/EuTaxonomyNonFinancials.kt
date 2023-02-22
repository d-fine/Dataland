package org.dataland.e2etests.tests.frameworks

import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EuTaxonomyNonFinancials {

    private val apiAccessor = ApiAccessor()

    private val listOfOneEuTaxonomyNonFinancialsDataSet = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getTData(1)
    private val listOfOneCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getCompanyInformationWithoutIdentifiers(1)

    @Test
    fun `post a dummy company and a dummy data set for it and check if data Id appears in the companys meta data`() {
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            listOfOneCompanyInformation,
            listOfOneEuTaxonomyNonFinancialsDataSet,
            apiAccessor.euTaxonomyNonFinancialsUploaderFunction,
        )
        val receivedDataMetaInformation = listOfUploadInfo[0].actualStoredDataMetaInfo
        val listOfDataMetaInfoForTestCompany = apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(
            receivedDataMetaInformation!!.companyId,
            receivedDataMetaInformation.dataType,
        )
        Assertions.assertTrue(
            listOfDataMetaInfoForTestCompany.component1().dataId.contains(receivedDataMetaInformation.dataId),
            "The expected data id was not found in the metadata database for the respective company.",
        )
    }

    @Test
    fun `post a company with EuTaxonomyForNonFinancials data and check if the data can be retrieved correctly`() {
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            listOfOneCompanyInformation,
            listOfOneEuTaxonomyNonFinancialsDataSet,
            apiAccessor.euTaxonomyNonFinancialsUploaderFunction,
        )
        val receivedDataMetaInformation = listOfUploadInfo[0].actualStoredDataMetaInfo
        val downloadedAssociatedData = apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
            .getCompanyAssociatedEuTaxonomyDataForNonFinancials(receivedDataMetaInformation!!.dataId)
        val downloadedAssociatedDataType = apiAccessor.metaDataControllerApi
            .getDataMetaInfo(receivedDataMetaInformation.dataId).dataType

        Assertions.assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType)
        Assertions.assertEquals(listOfOneEuTaxonomyNonFinancialsDataSet[0], downloadedAssociatedData.data)
    }
}

package org.dataland.e2etests.tests.frameworks

import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class NewEuTaxonomyNonFinancials {

    private val apiAccessor = ApiAccessor()

    private val listOfOneNewEuTaxonomyNonFinancialsDataSet =
        apiAccessor.testDataProviderForNewEuTaxonomyDataForNonFinancials
            .getTData(1)
    private val listOfOneCompanyInformation = apiAccessor.testDataProviderForNewEuTaxonomyDataForNonFinancials
        .getCompanyInformationWithoutIdentifiers(1)

    @Test
    fun `post a company with NewEuTaxonomyForNonFinancials data and check if the data can be retrieved correctly`() {
        println(listOfOneNewEuTaxonomyNonFinancialsDataSet)
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            listOfOneCompanyInformation,
            listOfOneNewEuTaxonomyNonFinancialsDataSet,
            apiAccessor::newEuTaxonomyNonFinancialsUploaderFunction,
        )
        println(listOfUploadInfo)
        val receivedDataMetaInformation = listOfUploadInfo[0].actualStoredDataMetaInfo
        val downloadedAssociatedData = apiAccessor.dataControllerApiForNewEuTaxonomyNonFinancials
            .getCompanyAssociatedNewEuTaxonomyDataForNonFinancials(receivedDataMetaInformation!!.dataId)
        val downloadedAssociatedDataType = apiAccessor.metaDataControllerApi
            .getDataMetaInfo(receivedDataMetaInformation.dataId).dataType

        Assertions.assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType)
        Assertions.assertEquals(listOfOneNewEuTaxonomyNonFinancialsDataSet[0], downloadedAssociatedData.data)
    }
}

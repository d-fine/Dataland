package org.dataland.e2etests.tests.dataPoints

import org.dataland.datalandbackend.openApiClient.model.AdditionalCompanyInformationData
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataJsonNode
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.api.Backend
import org.dataland.e2etests.utils.testDataProvivders.FrameworkTestDataProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DataMigrationTest {
    private val testDataProvider = FrameworkTestDataProvider(AdditionalCompanyInformationData::class.java)
    private val dummyDataset = testDataProvider.getTData(1)[0]
    private val apiAccessor = ApiAccessor()

    @Test
    fun `ensure the data can be retrieved correctly after migration`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val dataMetaInfo =
            Backend.dataMigrationControllerApi.forceUploadDatasetAsStoredDataset(
                dataType = DataTypeEnum.additionalMinusCompanyMinusInformation,
                companyAssociatedDataJsonNode =
                    CompanyAssociatedDataJsonNode(
                        companyId = companyId,
                        data = dummyDataset,
                        reportingPeriod = "2025",
                    ),
            )
        Backend.dataMigrationControllerApi.migrateStoredDatasetToAssembledDataset(dataMetaInfo.dataId)
        Thread.sleep(1000)
        val downloadedDataset =
            Backend.additionalCompanyInformationDataControllerApi
                .getCompanyAssociatedAdditionalCompanyInformationData(dataMetaInfo.dataId)

        assertEquals(
            dummyDataset.general?.general?.referencedReports,
            downloadedDataset.data.general
                ?.general
                ?.referencedReports,
        )
        assertEquals(
            dummyDataset.general?.financialInformation?.evic,
            downloadedDataset.data.general
                ?.financialInformation
                ?.evic
                // Ignore publication date as it is modified during referenced report processing
                ?.let { it.copy(dataSource = it.dataSource?.copy(publicationDate = null)) },
        )
    }
}

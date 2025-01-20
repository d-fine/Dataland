package org.dataland.e2etests.tests.dataPoints

import org.dataland.datalandbackend.openApiClient.model.AdditionalCompanyInformationData
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataJsonNode
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.api.Backend
import org.dataland.e2etests.utils.testDataProvivders.FrameworkTestDataProvider
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class DataMigrationTest {
    private val testDataProvider = FrameworkTestDataProvider(AdditionalCompanyInformationData::class.java)
    private val dummyDataset = testDataProvider.getTData(1)[0]
    private val apiAccessor = ApiAccessor()

    @Test
    fun `ensure the data migration works as expected`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val dataMetaInfo = Backend.dataMigrationControllerApi.forceUploadDatasetAsStoredDataset(
            dataType = DataTypeEnum.additionalMinusCompanyMinusInformation,
            companyAssociatedDataJsonNode = CompanyAssociatedDataJsonNode(
                companyId = companyId,
                data = dummyDataset,
                reportingPeriod = "2025"
            )
        )
        fail("DataId:  {${dataMetaInfo.dataId}}")
        // TODO: Continue test
    }
}
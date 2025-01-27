package org.dataland.e2etests.tests.dataPoints

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.Serializer.moshi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataJsonNode
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.openApiClient.model.QaStatus
import org.dataland.e2etests.tests.dataPoints.AssembledDatasetTest.LinkedQaReportTestData
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentManagerAccessor
import org.dataland.e2etests.utils.api.Backend
import org.dataland.e2etests.utils.api.QaService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataMigrationTest {
    private val linkedQaReportDataFile = File("./build/resources/test/AdditionalCompanyInformationQaReportPreparedFixtures.json")
    private val apiAccessor = ApiAccessor()

    private lateinit var linkedQaReportData: LinkedQaReportTestData

    @BeforeAll
    fun postTestDocuments() {
        DocumentManagerAccessor().uploadAllTestDocumentsAndAssurePersistence()
    }

    @BeforeAll
    fun loadLinkedQaReportData() {
        val moshiAdapter = moshi.adapter(LinkedQaReportTestData::class.java)
        linkedQaReportData = moshiAdapter.fromJson(linkedQaReportDataFile.readText())!!
    }

    private fun uploadDummyDataset(): DataMetaInformation {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        return Backend.dataMigrationControllerApi.forceUploadDatasetAsStoredDataset(
            dataType = DataTypeEnum.additionalMinusCompanyMinusInformation,
            companyAssociatedDataJsonNode =
                CompanyAssociatedDataJsonNode(
                    companyId = companyId,
                    data = linkedQaReportData.data,
                    reportingPeriod = "2025",
                ),
        )
    }

    @Test
    fun `ensure the data can be retrieved correctly after migration`() {
        val dataMetaInfo = uploadDummyDataset()
        Backend.dataMigrationControllerApi.migrateStoredDatasetToAssembledDataset(dataMetaInfo.dataId)
        Thread.sleep(1000)
        val downloadedDataset =
            Backend.additionalCompanyInformationDataControllerApi
                .getCompanyAssociatedAdditionalCompanyInformationData(dataMetaInfo.dataId)

        assertEquals(
            linkedQaReportData.data.general
                ?.general
                ?.referencedReports,
            downloadedDataset.data.general
                ?.general
                ?.referencedReports,
        )
        assertEquals(
            linkedQaReportData.data.general
                ?.financialInformation
                ?.evic,
            downloadedDataset.data.general
                ?.financialInformation
                ?.evic
                // Ignore publication date as it is modified during referenced report processing
                ?.let { it.copy(dataSource = it.dataSource?.copy(publicationDate = null)) },
        )
    }

    @Test
    fun `ensure a dataset cannot be migrated twice`() {
        val dataMetaInfo = uploadDummyDataset()
        Backend.dataMigrationControllerApi.migrateStoredDatasetToAssembledDataset(dataMetaInfo.dataId)
        Thread.sleep(1000)
        assertThrows<ClientException> {
            Backend.dataMigrationControllerApi.migrateStoredDatasetToAssembledDataset(dataMetaInfo.dataId)
        }
    }

    @ParameterizedTest
    @EnumSource(QaStatus::class, names = ["Accepted", "Rejected"])
    fun `ensure the data points keep the QA status of the dataset after migration`(qaStatus: QaStatus) {
        val dataMetaInfo = uploadDummyDataset()
        QaService.qaControllerApi.changeQaStatus(dataMetaInfo.dataId, qaStatus)
        Thread.sleep(1000)
        Backend.dataMigrationControllerApi.migrateStoredDatasetToAssembledDataset(dataMetaInfo.dataId)
        Thread.sleep(1000)
        val dataPointComposition =
            Backend.metaDataControllerApi.getContainedDataPoints(dataMetaInfo.dataId).values
        for (dataPoint in dataPointComposition) {
            val dataPointQaInfo = QaService.qaControllerApi.getDataPointQaReviewInformationByDataId(dataPoint)
            assertEquals(qaStatus, dataPointQaInfo[0].qaStatus)
        }
    }

    @Test
    fun `ensure that qa reports get migrated`() {
        val dataMetaInfo = uploadDummyDataset()
        val qaReportInfo =
            QaService.assembledDataMigrationControllerApi.forceUploadStoredQaReport(
                dataId = dataMetaInfo.dataId,
                body = linkedQaReportData.qaReport,
            )
        Backend.dataMigrationControllerApi.migrateStoredDatasetToAssembledDataset(dataMetaInfo.dataId)
        Thread.sleep(1000)
        QaService.additionalCompanyInformationDataQaReportControllerApi
            .getAdditionalCompanyInformationDataQaReport(qaReportInfo.dataId, qaReportInfo.qaReportId)
            .let {
                assertEquals(linkedQaReportData.qaReport, it.report)
            }
    }
}

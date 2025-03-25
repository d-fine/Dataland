package org.dataland.e2etests.tests.dataPoints

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.Serializer.moshi
import org.dataland.datalandbackend.openApiClient.model.AdditionalCompanyInformationData
import org.dataland.datalandbackend.openApiClient.model.AdditionalCompanyInformationGeneral
import org.dataland.datalandbackend.openApiClient.model.AdditionalCompanyInformationGeneralFinancialInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataJsonNode
import org.dataland.datalandbackend.openApiClient.model.CurrencyDataPoint
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandqaservice.openApiClient.model.QaStatus
import org.dataland.e2etests.tests.dataPoints.AssembledDatasetTest.LinkedQaReportTestData
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DataPointTestUtils
import org.dataland.e2etests.utils.DocumentControllerApiAccessor
import org.dataland.e2etests.utils.api.ApiAwait
import org.dataland.e2etests.utils.api.Backend
import org.dataland.e2etests.utils.api.QaService
import org.dataland.e2etests.utils.testDataProviders.FrameworkTestDataProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpStatus
import java.io.File
import java.math.BigDecimal
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataMigrationTest {
    private val linkedQaReportDataFile = File("./build/resources/test/AdditionalCompanyInformationQaReportPreparedFixtures.json")
    private val fakeFixtureProvider = FrameworkTestDataProvider.forFrameworkPreparedFixtures(AdditionalCompanyInformationData::class.java)
    private val apiAccessor = ApiAccessor()
    private val reportingPeriod = "2025"

    private lateinit var linkedQaReportData: LinkedQaReportTestData

    data class SfdrLinkedQaReportTestData(
        val data: SfdrData,
        val qaReport: org.dataland.datalandqaservice.openApiClient.model.SfdrData,
    )

    @BeforeAll
    fun postTestDocuments() {
        DocumentControllerApiAccessor().uploadAllTestDocumentsAndAssurePersistence()
    }

    @BeforeAll
    fun loadLinkedQaReportData() {
        val moshiAdapter = moshi.adapter(LinkedQaReportTestData::class.java)
        linkedQaReportData = moshiAdapter.fromJson(linkedQaReportDataFile.readText())!!
    }

    private fun loadSfdrLinkedQaReportData(file: File): SfdrLinkedQaReportTestData {
        val moshiAdapter = moshi.adapter(SfdrLinkedQaReportTestData::class.java)
        return moshiAdapter.fromJson(file.readText())!!
    }

    private fun uploadGenericDummyDataset(
        data: Any,
        dataType: DataTypeEnum,
        companyId: String? = null,
    ): DataMetaInformation =
        Backend.dataMigrationControllerApi.forceUploadDatasetAsStoredDataset(
            dataType = dataType,
            companyAssociatedDataJsonNode =
                CompanyAssociatedDataJsonNode(
                    companyId = companyId ?: apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
                    data = data,
                    reportingPeriod = reportingPeriod,
                ),
            bypassQa = true,
        )

    @Test
    fun `ensure the data can be retrieved correctly after migration`() {
        val dataMetaInfo = uploadGenericDummyDataset(linkedQaReportData.data, DataTypeEnum.additionalMinusCompanyMinusInformation)
        Backend.dataMigrationControllerApi.migrateStoredDatasetToAssembledDataset(dataMetaInfo.dataId)

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
    fun `ensure that nullish values get migrated correctly`() {
        val fixture = fakeFixtureProvider.getByCompanyName("additional-company-information-dataset-with-nullish-fields").t
        val dataMetaInfo = uploadGenericDummyDataset(fixture, DataTypeEnum.additionalMinusCompanyMinusInformation)
        Backend.dataMigrationControllerApi.migrateStoredDatasetToAssembledDataset(dataMetaInfo.dataId)
        val downloadedDataset =
            Backend.additionalCompanyInformationDataControllerApi
                .getCompanyAssociatedAdditionalCompanyInformationData(dataMetaInfo.dataId)
        assertEquals(
            null,
            downloadedDataset.data.general
                ?.financialInformation,
        )
    }

    @Test
    fun `ensure a dataset cannot be migrated twice`() {
        val dataMetaInfo = uploadGenericDummyDataset(linkedQaReportData.data, DataTypeEnum.additionalMinusCompanyMinusInformation)
        Backend.dataMigrationControllerApi.migrateStoredDatasetToAssembledDataset(dataMetaInfo.dataId)
        assertThrows<ClientException> {
            Backend.dataMigrationControllerApi.migrateStoredDatasetToAssembledDataset(dataMetaInfo.dataId)
        }
    }

    @ParameterizedTest
    @EnumSource(QaStatus::class, names = ["Accepted", "Rejected"])
    fun `ensure the data points keep the QA status of the dataset after migration`(qaStatus: QaStatus) {
        val dataMetaInfo = uploadGenericDummyDataset(linkedQaReportData.data, DataTypeEnum.additionalMinusCompanyMinusInformation)
        ApiAwait.waitForSuccess(retryOnHttpErrors = setOf(HttpStatus.NOT_FOUND)) {
            QaService.qaControllerApi.changeQaStatus(dataMetaInfo.dataId, qaStatus)
        }

        val datasetQaEntity = QaService.qaControllerApi.getQaReviewResponseByDataId(UUID.fromString(dataMetaInfo.dataId))

        ApiAwait.waitForCondition {
            Backend.metaDataControllerApi
                .getDataMetaInfo(dataMetaInfo.dataId)
                .qaStatus.value == qaStatus.value
        }

        Backend.dataMigrationControllerApi.migrateStoredDatasetToAssembledDataset(dataMetaInfo.dataId)
        val dataPointComposition =
            Backend.metaDataControllerApi.getContainedDataPoints(dataMetaInfo.dataId).values

        for (dataPoint in dataPointComposition) {
            ApiAwait
                .waitForData(
                    supplier = { QaService.qaControllerApi.getDataPointQaReviewInformationByDataId(dataPoint) },
                    condition = { it.isNotEmpty() },
                ).let {
                    assertEquals(qaStatus, it[0].qaStatus)
                    assertEquals(datasetQaEntity.timestamp, it[0].timestamp)
                    assertEquals(datasetQaEntity.triggeringUserId, it[0].reviewerId)
                }
        }
    }

    @Test
    fun `ensure that qa reports get migrated`() {
        val dataMetaInfo = uploadGenericDummyDataset(linkedQaReportData.data, DataTypeEnum.additionalMinusCompanyMinusInformation)
        val qaReportInfo =
            QaService.assembledDataMigrationControllerApi.forceUploadStoredQaReport(
                dataId = dataMetaInfo.dataId,
                body = linkedQaReportData.qaReport,
            )
        Backend.dataMigrationControllerApi.migrateStoredDatasetToAssembledDataset(dataMetaInfo.dataId)
        ApiAwait
            // When the API Call is faster than the migration, the QA Report might not be migrated yet, resulting
            // in a 500. This is expected.
            .waitForData(retryOnHttpErrors = setOf(HttpStatus.INTERNAL_SERVER_ERROR)) {
                QaService.additionalCompanyInformationDataQaReportControllerApi
                    .getAdditionalCompanyInformationDataQaReport(qaReportInfo.dataId, qaReportInfo.qaReportId)
            }.let {
                assertEquals(linkedQaReportData.qaReport, it.report)
            }
    }

    @Test
    fun `ensure that sfdr data gets migrated correctly`() {
        val originalData = FrameworkTestDataProvider.forFrameworkFixtures(SfdrData::class.java).getTData(1).first()
        val dataMetaInfo = uploadGenericDummyDataset(data = originalData, dataType = DataTypeEnum.sfdr)
        Backend.dataMigrationControllerApi.migrateStoredDatasetToAssembledDataset(dataMetaInfo.dataId)
        val migratedData = Backend.sfdrDataControllerApi.getCompanyAssociatedSfdrData(dataMetaInfo.dataId)
        DataPointTestUtils().assertDataEqualsIgnoringPublicationDates(originalData, migratedData.data)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "./build/resources/test/SfdrLinkedDataAndQaReportPreparedFixtures.json",
            "./build/resources/test/SfdrLinkedDataAndQaReportWithManyNullsPreparedFixtures.json",
        ],
    )
    fun `ensure that sfdr qa reports get migrated correctly`(testDataLocation: String) {
        val linkedQaReportData = loadSfdrLinkedQaReportData(File(testDataLocation))
        val dataMetaInfo = uploadGenericDummyDataset(data = linkedQaReportData.data, dataType = DataTypeEnum.sfdr)
        val qaReportInfo =
            QaService.assembledDataMigrationControllerApi.forceUploadStoredQaReport(
                dataId = dataMetaInfo.dataId,
                body = linkedQaReportData.qaReport,
            )
        Backend.dataMigrationControllerApi.migrateStoredDatasetToAssembledDataset(dataMetaInfo.dataId)
        ApiAwait
            // When the API Call is faster than the migration, the QA Report might not be migrated yet, resulting
            // in a 500. This is expected.
            .waitForData(retryOnHttpErrors = setOf(HttpStatus.INTERNAL_SERVER_ERROR)) {
                QaService.sfdrDataQaReportControllerApi
                    .getSfdrDataQaReport(qaReportInfo.dataId, qaReportInfo.qaReportId)
            }.let {
                assertEquals(DataPointTestUtils().removeEmptyEntries(linkedQaReportData.qaReport), it.report)
            }
    }

    @Test
    fun `ensure that the active status is correctly preserved by the migration`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val firstDataset = FrameworkTestDataProvider.forFrameworkFixtures(SfdrData::class.java).getTData(1).first()
        val secondDataset =
            FrameworkTestDataProvider
                .forFrameworkPreparedFixtures(SfdrData::class.java)
                .getByCompanyName("Sfdr-dataset-with-no-null-fields")
                .t

        val metaInfo1 = uploadGenericDummyDataset(firstDataset, DataTypeEnum.sfdr, companyId = companyId)
        val metaInfo2 = uploadGenericDummyDataset(secondDataset, DataTypeEnum.sfdr, companyId = companyId)

        Backend.dataMigrationControllerApi.migrateStoredDatasetToAssembledDataset(metaInfo1.dataId)
        Backend.dataMigrationControllerApi.migrateStoredDatasetToAssembledDataset(metaInfo2.dataId)
        val allDataPoints = Backend.metaDataControllerApi.getContainedDataPoints(metaInfo2.dataId)

        ApiAwait.waitForCondition {
            allDataPoints.all { Backend.dataPointControllerApi.getDataPointMetaInfo(it.value).currentlyActive }
        }

        val downloadedData =
            Backend.sfdrDataControllerApi.getCompanyAssociatedSfdrDataByDimensions(reportingPeriod = reportingPeriod, companyId = companyId)
        DataPointTestUtils().assertSfdrDataEquals(downloadedData.data, secondDataset)
    }

    private val minimalDatasetDebt =
        AdditionalCompanyInformationData(
            general =
                AdditionalCompanyInformationGeneral(
                    financialInformation =
                        AdditionalCompanyInformationGeneralFinancialInformation(
                            debt = CurrencyDataPoint(BigDecimal.valueOf(1)),
                        ),
                ),
        )
    private val minimalDatasetEquity =
        AdditionalCompanyInformationData(
            general =
                AdditionalCompanyInformationGeneral(
                    financialInformation =
                        AdditionalCompanyInformationGeneralFinancialInformation(
                            equity = CurrencyDataPoint(BigDecimal.valueOf(1)),
                        ),
                ),
        )

    @Test
    fun `ensure that after the migration non overlapping accepted datasets result in the correct dynamic view`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        uploadGenericDummyDataset(minimalDatasetEquity, DataTypeEnum.additionalMinusCompanyMinusInformation, companyId = companyId)
        uploadGenericDummyDataset(minimalDatasetDebt, DataTypeEnum.additionalMinusCompanyMinusInformation, companyId = companyId)
        Backend.dataMigrationControllerApi.triggerMigrationForAllStoredDatasets()
        ApiAwait
            .waitForData(timeoutInSeconds = 30, retryOnHttpErrors = setOf(HttpStatus.NOT_FOUND)) {
                Backend.additionalCompanyInformationDataControllerApi
                    .getCompanyAssociatedAdditionalCompanyInformationDataByDimensions(
                        reportingPeriod = reportingPeriod, companyId = companyId,
                    )
            }.let {
                assertEquals(
                    it.data.general
                        ?.financialInformation
                        ?.equity
                        ?.value,
                    minimalDatasetEquity.general
                        ?.financialInformation
                        ?.equity
                        ?.value,
                )
                assertEquals(
                    it.data.general
                        ?.financialInformation
                        ?.debt
                        ?.value,
                    minimalDatasetDebt.general
                        ?.financialInformation
                        ?.debt
                        ?.value,
                )
            }
    }
}

package org.dataland.e2etests.tests.dataPoints

import org.awaitility.Awaitility
import org.dataland.datalandbackend.openApiClient.infrastructure.Serializer.moshi
import org.dataland.datalandbackend.openApiClient.model.AdditionalCompanyInformationData
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataAdditionalCompanyInformationData
import org.dataland.datalandbackend.openApiClient.model.DataAndMetaInformationAdditionalCompanyInformationData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.UploadedDataPoint
import org.dataland.datalandqaservice.openApiClient.model.CurrencyDataPoint
import org.dataland.datalandqaservice.openApiClient.model.DataPointQaReport
import org.dataland.datalandqaservice.openApiClient.model.QaReportDataPointCurrencyDataPoint
import org.dataland.datalandqaservice.openApiClient.model.QaStatus
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentControllerApiAccessor
import org.dataland.e2etests.utils.api.ApiAwait
import org.dataland.e2etests.utils.api.Backend
import org.dataland.e2etests.utils.api.QaService
import org.dataland.e2etests.utils.testDataProviders.FrameworkTestDataProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpStatus
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AssembledDatasetTest {
    private val testDataProvider =
        FrameworkTestDataProvider.forFrameworkFixtures(AdditionalCompanyInformationData::class.java)
    private val dummyDataset = testDataProvider.getTData(1)[0]
    private val apiAccessor = ApiAccessor()
    private val linkedQaReportDataFile =
        File("./build/resources/test/AdditionalCompanyInformationQaReportPreparedFixtures.json")
    private val reportingPeriod = "2025"
    private val testValue = 1.2345.toBigDecimal()
    private val testComment = "This is a specific test comment."

    data class LinkedQaReportTestData(
        val data: AdditionalCompanyInformationData,
        val qaReport: org.dataland.datalandqaservice.openApiClient.model.AdditionalCompanyInformationData,
    )

    private lateinit var linkedQaReportData: LinkedQaReportTestData

    @BeforeAll
    fun postTestDocuments() {
        DocumentControllerApiAccessor().uploadAllTestDocumentsAndAssurePersistence()
    }

    @BeforeAll
    fun loadLinkedQaReportData() {
        val moshiAdapter = moshi.adapter(LinkedQaReportTestData::class.java)
        linkedQaReportData = moshiAdapter.fromJson(linkedQaReportDataFile.readText())!!
    }

    private fun uploadDummyAdditionalCompanyInformationDataset(
        companyId: String,
        bypassQa: Boolean,
    ): DataMetaInformation {
        val dataMetaInformationResponse =
            Backend.additionalCompanyInformationDataControllerApi.postCompanyAssociatedAdditionalCompanyInformationData(
                CompanyAssociatedDataAdditionalCompanyInformationData(
                    companyId = companyId,
                    reportingPeriod = reportingPeriod,
                    data = dummyDataset,
                ),
                bypassQa = bypassQa,
            )
        return dataMetaInformationResponse
    }

    @Test
    fun `ensure uploading and downloading an assembled dataset works consistently`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val dataMetaInformation = uploadDummyAdditionalCompanyInformationDataset(companyId, bypassQa = true)
        val downloadedDataset =
            Backend.additionalCompanyInformationDataControllerApi
                .getCompanyAssociatedAdditionalCompanyInformationData(dataMetaInformation.dataId)

        compareAdditionalCompanyInformationDatasets(dummyDataset, downloadedDataset.data)
    }

    @Test
    fun `ensure that an uploaded dataset can be downloaded via dimensions`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val dataMetaInformation = uploadDummyAdditionalCompanyInformationDataset(companyId, bypassQa = true)
        Awaitility.await().atMost(5000, TimeUnit.MILLISECONDS).pollDelay(1000, TimeUnit.MILLISECONDS).untilAsserted {
            val downloadedDataset =
                Backend.additionalCompanyInformationDataControllerApi.getCompanyAssociatedAdditionalCompanyInformationDataByDimensions(
                    dataMetaInformation.reportingPeriod,
                    companyId,
                )

            compareAdditionalCompanyInformationDatasets(dummyDataset, downloadedDataset.data)
        }
    }

    private fun compareAdditionalCompanyInformationDatasets(
        expected: AdditionalCompanyInformationData,
        actual: AdditionalCompanyInformationData,
    ) {
        assertEquals(
            expected.general?.general?.referencedReports,
            actual.general
                ?.general
                ?.referencedReports,
        )
        assertEquals(
            expected.general?.financialInformation?.evic,
            actual.general
                ?.financialInformation
                ?.evic
                // Ignore publication date as it is modified during referenced report processing
                ?.let { it.copy(dataSource = it.dataSource?.copy(publicationDate = null)) },
        )
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `ensure an uploaded assembled dataset ends up in the qa queues with the correct qa status`(bypassQa: Boolean) {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId

        val expectedQaStatus = if (bypassQa) QaStatus.Accepted else QaStatus.Pending
        val dataMetaInformation = uploadDummyAdditionalCompanyInformationDataset(companyId, bypassQa = bypassQa)

        ApiAwait
            .waitForData(retryOnHttpErrors = setOf(HttpStatus.NOT_FOUND)) {
                QaService.qaControllerApi.getQaReviewResponseByDataId(UUID.fromString(dataMetaInformation.dataId))
            }.let {
                assertEquals(expectedQaStatus, it.qaStatus)
            }

        val allUploadedFacts = Backend.metaDataControllerApi.getContainedDataPoints(dataMetaInformation.dataId).values

        allUploadedFacts.forEach { factId ->
            ApiAwait
                .waitForData(
                    supplier = {
                        QaService.qaControllerApi.getDataPointQaReviewInformationByDataId(factId)
                    },
                    condition = { it.isNotEmpty() },
                ).let {
                    assertEquals(expectedQaStatus, it[0].qaStatus)
                }
        }
    }

    @Test
    fun `ensure that accepting an assembled dataset also accepts all datapoints`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val dataMetaInformation = uploadDummyAdditionalCompanyInformationDataset(companyId, bypassQa = false)

        ApiAwait.waitForSuccess(retryOnHttpErrors = setOf(HttpStatus.NOT_FOUND)) {
            QaService.qaControllerApi.changeQaStatus(dataMetaInformation.dataId, QaStatus.Accepted)
        }

        val allUploadedFacts = Backend.metaDataControllerApi.getContainedDataPoints(dataMetaInformation.dataId).values

        allUploadedFacts.forEach { factId ->
            ApiAwait
                .waitForData(
                    supplier = { QaService.qaControllerApi.getDataPointQaReviewInformationByDataId(factId) },
                    condition = { it.size >= 2 },
                ).let {
                    assertEquals(QaStatus.Accepted, it[0].qaStatus)
                }
        }
    }

    data class LinkedQaReportMetaInfo(
        val companyId: String,
        val dataId: String,
        val qaReportId: String,
    )

    private fun uploadAdditionalCompanyInformationWithLinkedQaReport(): LinkedQaReportMetaInfo {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val uploadedDataset =
            Backend.additionalCompanyInformationDataControllerApi.postCompanyAssociatedAdditionalCompanyInformationData(
                CompanyAssociatedDataAdditionalCompanyInformationData(
                    companyId = companyId,
                    reportingPeriod = reportingPeriod,
                    data = linkedQaReportData.data,
                ),
                bypassQa = false,
            )
        val uploadedQaReport =
            ApiAwait.waitForData(retryOnHttpErrors = setOf(HttpStatus.NOT_FOUND)) {
                QaService.additionalCompanyInformationDataQaReportControllerApi.postAdditionalCompanyInformationDataQaReport(
                    uploadedDataset.dataId,
                    linkedQaReportData.qaReport,
                )
            }

        return LinkedQaReportMetaInfo(
            companyId = companyId,
            dataId = uploadedDataset.dataId,
            qaReportId = uploadedQaReport.qaReportId,
        )
    }

    @Test
    fun `ensure a qa report for an assembled dataset can be downloaded`() {
        val linkedQaReportMetaInfo = uploadAdditionalCompanyInformationWithLinkedQaReport()
        QaService.additionalCompanyInformationDataQaReportControllerApi
            .getAdditionalCompanyInformationDataQaReport(
                linkedQaReportMetaInfo.dataId,
                linkedQaReportMetaInfo.qaReportId,
            ).let {
                assertEquals(linkedQaReportData.qaReport, it.report)
            }
    }

    data class ExpectedDataForFact(
        val qaStatus: QaStatus,
        val qaReport: QaReportDataPointCurrencyDataPoint,
    )

    private fun assertQaReportsAlign(
        currencyQaReport: QaReportDataPointCurrencyDataPoint,
        dataPointQaReport: DataPointQaReport,
    ) {
        assertEquals(currencyQaReport.verdict, dataPointQaReport.verdict)
        assertEquals(currencyQaReport.comment, dataPointQaReport.comment)

        val moshiAdapter = moshi.adapter(CurrencyDataPoint::class.java)
        val actualData = moshiAdapter.fromJson(dataPointQaReport.correctedData!!)
        assertEquals(currencyQaReport.correctedData, actualData)
    }

    @Test
    fun `ensure information from an assembled qa report gets applied to the individual datapoints`() {
        val linkedQaReportMetaInfo = uploadAdditionalCompanyInformationWithLinkedQaReport()
        val expectedDataPointInformation =
            mapOf(
                "extendedCurrencyEquity" to
                    ExpectedDataForFact(
                        qaStatus = QaStatus.Accepted,
                        qaReport =
                            linkedQaReportData.qaReport.general!!
                                .financialInformation!!
                                .equity!!,
                    ),
                "extendedCurrencyDebt" to
                    ExpectedDataForFact(
                        qaStatus = QaStatus.Pending,
                        qaReport =
                            linkedQaReportData.qaReport.general!!
                                .financialInformation!!
                                .debt!!,
                    ),
                "extendedCurrencyEvic" to
                    ExpectedDataForFact(
                        qaStatus = QaStatus.Rejected,
                        qaReport =
                            linkedQaReportData.qaReport.general!!
                                .financialInformation!!
                                .evic!!,
                    ),
            )

        val datasetComposition = Backend.metaDataControllerApi.getContainedDataPoints(linkedQaReportMetaInfo.dataId)
        expectedDataPointInformation.forEach { (dataPointType, expectedData) ->
            val dataPointId = datasetComposition[dataPointType]!!
            QaService.qaControllerApi.getDataPointQaReviewInformationByDataId(dataPointId).let {
                assertEquals(expectedData.qaStatus, it[0].qaStatus)
            }
            QaService.dataPointQaReportControllerApi.getAllQaReportsForDataPoint(dataPointId).let {
                assertQaReportsAlign(expectedData.qaReport, it[0])
            }
        }
    }

    private fun postExtendedCurrencyEquityDatapoint(
        companyId: String,
        reportingPeriod: String,
    ) {
        val dummyDatapoint = """{"value": "$testValue", "currency": "EUR", "comment": "$testComment"}""".trimIndent()
        val dummyDataPointType = "extendedCurrencyEquity"
        val uploadedDataPoint =
            UploadedDataPoint(
                dataPoint = dummyDatapoint,
                dataPointType = dummyDataPointType,
                companyId = companyId,
                reportingPeriod = reportingPeriod,
            )
        ApiAwait.waitForSuccess { Backend.dataPointControllerApi.postDataPoint(uploadedDataPoint, bypassQa = true) }
    }

    private fun getAdditionalCompanyInformationDataset(
        companyId: String,
        reportingPeriod: String,
    ): DataAndMetaInformationAdditionalCompanyInformationData =
        ApiAwait.waitForData {
            Backend.additionalCompanyInformationDataControllerApi
                .getAllCompanyAdditionalCompanyInformationData(companyId, reportingPeriod = reportingPeriod)[0]
        }

    @Test
    fun `ensure that uploading a datapoint for an existing dataset changes active data retrieved by api`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        ApiAwait.waitForSuccess { uploadDummyAdditionalCompanyInformationDataset(companyId, bypassQa = true) }

        this.postExtendedCurrencyEquityDatapoint(companyId, reportingPeriod)

        Awaitility.await().atMost(5000, TimeUnit.MILLISECONDS).pollDelay(1000, TimeUnit.MILLISECONDS).untilAsserted {
            val activeAdditionalCompanyInformationDataset =
                this.getAdditionalCompanyInformationDataset(companyId, reportingPeriod)

            val currencyDataPoint =
                activeAdditionalCompanyInformationDataset.data.general
                    ?.financialInformation
                    ?.equity
            assertNotNull(currencyDataPoint)
            assertEquals(currencyDataPoint?.value, testValue)
            assertEquals(currencyDataPoint?.comment, testComment)
        }
    }

    @Test
    fun `ensure that uploading only a single datapoint for a company renders the reporting period active`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId

        this.postExtendedCurrencyEquityDatapoint(companyId, reportingPeriod)
        Awaitility.await().atMost(5000, TimeUnit.MILLISECONDS).pollDelay(1000, TimeUnit.MILLISECONDS).untilAsserted {
            val activeAdditionalCompanyInformationDataset =
                this.getAdditionalCompanyInformationDataset(companyId, reportingPeriod)

            val currencyDataPoint =
                activeAdditionalCompanyInformationDataset.data.general
                    ?.financialInformation
                    ?.equity
            assertNotNull(currencyDataPoint)
            assertEquals(currencyDataPoint?.value, testValue)
            assertEquals(currencyDataPoint?.comment, testComment)
        }
    }
}

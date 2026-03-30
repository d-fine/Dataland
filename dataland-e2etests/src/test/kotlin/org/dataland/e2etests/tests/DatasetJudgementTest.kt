package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandqaservice.openApiClient.infrastructure.ClientException
import org.dataland.datalandqaservice.openApiClient.model.AcceptedDataPointSource
import org.dataland.datalandqaservice.openApiClient.model.DatasetJudgementState
import org.dataland.datalandqaservice.openApiClient.model.JudgementDetailsPatch
import org.dataland.datalandqaservice.openApiClient.model.QaReportDataPointString
import org.dataland.datalandqaservice.openApiClient.model.QaReportDataPointVerdict
import org.dataland.datalandqaservice.openApiClient.model.QaStatus
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentControllerApiAccessor
import org.dataland.e2etests.utils.api.Backend
import org.dataland.e2etests.utils.api.QaService
import org.dataland.e2etests.utils.testDataProviders.FrameworkTestDataProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatasetJudgementTest {
    private val testDataProvider =
        FrameworkTestDataProvider.forFrameworkFixtures(SfdrData::class.java)
    private val dummyDataset = testDataProvider.getTData(1)[0]
    private val dummyReportingPeriod = "2026"
    private val apiAccessor = ApiAccessor()
    private val dataPointType1 = "extendedDecimalScope1GhgEmissionsInTonnes"
    private val dataPointType2 = "extendedDecimalScope2GhgEmissionsLocationBasedInTonnes"
    private val dataPointType3 = "extendedDecimalScope2GhgEmissionsMarketBasedInTonnes"
    private val customDataPoint = "{\"value\":1000,\"quality\":\"Reported\",\"comment\":null,\"dataSource\":null}"

    private val dummyQaReport1 =
        QaReportDataPointString(
            comment = "",
            verdict = QaReportDataPointVerdict.QaAccepted,
            correctedData = "{\"value\":100,\"quality\":\"Reported\",\"comment\":null,\"dataSource\":null}",
        )

    private val dummyQaReport2 =
        QaReportDataPointString(
            comment = "",
            verdict = QaReportDataPointVerdict.QaRejected,
            correctedData = "{\"value\":200,\"quality\":\"Reported\",\"comment\":null,\"dataSource\":null}",
        )

    @BeforeAll
    fun postTestDocuments() {
        DocumentControllerApiAccessor().uploadAllTestDocumentsAndAssurePersistence()
    }

    private fun uploadDummySfdrDataset(
        companyId: String,
        bypassQa: Boolean,
    ): DataMetaInformation {
        val dataMetaInformationResponse =
            Backend.sfdrDataControllerApi.postCompanyAssociatedSfdrData(
                CompanyAssociatedDataSfdrData(
                    companyId = companyId,
                    reportingPeriod = dummyReportingPeriod,
                    data = dummyDataset,
                ),
                bypassQa = bypassQa,
            )
        return dataMetaInformationResponse
    }

    private fun postQaReport(
        datapointId: String,
        qaReport: QaReportDataPointString,
    ) = QaService.dataPointQaReportControllerApi
        .postQaReport(datapointId, qaReport)
        .reporterUserId

    private data class DatasetAndJudgementAndDataPointIds(
        val datasetId: String,
        val datasetJudgementId: String,
        val datapointIds: Map<String, String>,
    )

    private fun postJudgementWithPatches(
        datasetId: String,
        dataPoints: Map<String, String>,
    ): String {
        var datasetJudgementId: String? = null

        val datapointId1 = dataPoints.getValue(dataPointType1)
        val datapointId2 = dataPoints.getValue(dataPointType2)
        val datapointId3 = dataPoints.getValue(dataPointType3)
        val reporterUserId1 = postQaReport(datapointId1, dummyQaReport1)
        val reporterUserId2 = postQaReport(datapointId2, dummyQaReport2)

        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            datasetJudgementId =
                QaService.datasetJudgementControllerApi
                    .postDatasetJudgement(datasetId)
                    .dataSetJudgementId

            data class PatchOperation(
                val dataPointType: String,
                val acceptedSource: AcceptedDataPointSource,
                val reporterUserIdOfAcceptedQaReport: String? = null,
                val customDataPoint: String? = null,
            )
            val explicitlyHandledDataPointIds = setOf(datapointId1, datapointId2, datapointId3)

            val patchOperations =
                listOf(
                    PatchOperation(dataPointType1, AcceptedDataPointSource.Qa, reporterUserId1),
                    PatchOperation(dataPointType2, AcceptedDataPointSource.Qa, reporterUserId2),
                    PatchOperation(dataPointType1, AcceptedDataPointSource.Original),
                    PatchOperation(dataPointType2, AcceptedDataPointSource.Qa, reporterUserId2),
                    PatchOperation(dataPointType3, AcceptedDataPointSource.Custom, null, customDataPoint),
                ) +
                    dataPoints
                        .filter { (_, dataPointId) -> dataPointId !in explicitlyHandledDataPointIds }
                        .keys
                        .map { dataPointType ->
                            PatchOperation(dataPointType, AcceptedDataPointSource.Original)
                        }

            val currentDatasetJudgementId = requireNotNull(datasetJudgementId)
            patchOperations.forEach {
                QaService.datasetJudgementControllerApi.patchJudgementDetails(
                    currentDatasetJudgementId,
                    it.dataPointType,
                    JudgementDetailsPatch(
                        it.acceptedSource,
                        it.reporterUserIdOfAcceptedQaReport,
                        it.customDataPoint,
                    ),
                )
            }
        }
        return datasetJudgementId!!
    }

    private fun createDatasetWithJudgement(): DatasetAndJudgementAndDataPointIds {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val datasetId = uploadDummySfdrDataset(companyId, bypassQa = false).dataId
        val dataPoints = Backend.metaDataControllerApi.getContainedDataPoints(datasetId)
        val datasetJudgementId = postJudgementWithPatches(datasetId, dataPoints)
        return DatasetAndJudgementAndDataPointIds(
            datasetId,
            datasetJudgementId,
            mapOf(
                dataPointType1 to dataPoints.getValue(dataPointType1),
                dataPointType2 to dataPoints.getValue(dataPointType2),
                dataPointType3 to dataPoints.getValue(dataPointType3),
            ),
        )
    }

    @Test
    fun `ensure dataset judgement entities behave correctly`() {
        val datasetAndDataPointIds = createDatasetWithJudgement()

        val datasetJudgement =
            QaService.datasetJudgementControllerApi
                .getDatasetJudgementsByDatasetId(datasetAndDataPointIds.datasetId)
                .first { it.dataSetJudgementId == datasetAndDataPointIds.datasetJudgementId }

        assertEquals(AcceptedDataPointSource.Original, datasetJudgement.dataPoints[dataPointType1]?.acceptedSource)
        assertNull(datasetJudgement.dataPoints[dataPointType1]?.reporterUserIdOfAcceptedQaReport)

        assertEquals(AcceptedDataPointSource.Qa, datasetJudgement.dataPoints[dataPointType2]?.acceptedSource)

        assertEquals(AcceptedDataPointSource.Custom, datasetJudgement.dataPoints[dataPointType3]?.acceptedSource)
        assertEquals(customDataPoint, datasetJudgement.dataPoints[dataPointType3]?.customValue)
    }

    @Test
    fun `ensure Finish Review works as expected`() {
        val datasetAndJudgementAndDataPointIds = createDatasetWithJudgement()
        val datasetId = datasetAndJudgementAndDataPointIds.datasetId
        val datasetJudgementId = datasetAndJudgementAndDataPointIds.datasetJudgementId

        QaService.datasetJudgementControllerApi.setJudgementState(datasetJudgementId, DatasetJudgementState.FinishedWithDatasetAcceptance)

        assertQaStatusOfDataset(QaStatus.Accepted, datasetId)
        assertDatasetJudgementState(datasetJudgementId, DatasetJudgementState.FinishedWithDatasetAcceptance)

        assertQaStatusOfDatapoint(QaStatus.Accepted, datasetAndJudgementAndDataPointIds.datapointIds.getValue(dataPointType1))

        assertQaStatusOfDatapoint(QaStatus.Rejected, datasetAndJudgementAndDataPointIds.datapointIds.getValue(dataPointType2))
        assertNewDatapointWithQaStatusAccepted(
            datasetId,
            dataPointType2,
            dummyQaReport2.correctedData!!,
        )

        assertQaStatusOfDatapoint(QaStatus.Rejected, datasetAndJudgementAndDataPointIds.datapointIds.getValue(dataPointType3))

        assertNewDatapointWithQaStatusAccepted(
            datasetId,
            dataPointType3,
            customDataPoint,
        )
    }

    @Test
    fun `ensure Reject Dataset works as expected`() {
        val datasetAndJudgementAndDataPointIds = createDatasetWithJudgement()
        val datasetId = datasetAndJudgementAndDataPointIds.datasetId
        val datasetJudgementId = datasetAndJudgementAndDataPointIds.datasetJudgementId

        QaService.datasetJudgementControllerApi.setJudgementState(datasetJudgementId, DatasetJudgementState.FinishedWithDatasetRejection)

        assertQaStatusOfDataset(QaStatus.Rejected, datasetId)
        assertDatasetJudgementState(datasetJudgementId, DatasetJudgementState.FinishedWithDatasetRejection)

        assertQaStatusOfDatapoint(QaStatus.Rejected, datasetAndJudgementAndDataPointIds.datapointIds.getValue(dataPointType1))
        assertQaStatusOfDatapoint(QaStatus.Rejected, datasetAndJudgementAndDataPointIds.datapointIds.getValue(dataPointType2))
        assertQaStatusOfDatapoint(QaStatus.Rejected, datasetAndJudgementAndDataPointIds.datapointIds.getValue(dataPointType3))
    }

    @Test
    fun `ensure non-judge cannot call patchJudgementDetails or setJudgementState`() {
        val (_, datasetJudgementId, _) = createDatasetWithJudgement()

        for (nonJudgeUser in listOf(TechnicalUser.Uploader, TechnicalUser.Reviewer, TechnicalUser.Reader)) {
            GlobalAuth.withTechnicalUser(nonJudgeUser) {
                val patchException =
                    assertThrows<ClientException> {
                        QaService.datasetJudgementControllerApi.patchJudgementDetails(
                            datasetJudgementId,
                            dataPointType1,
                            JudgementDetailsPatch(
                                AcceptedDataPointSource.Original,
                                null,
                                null,
                            ),
                        )
                    }
                assertEquals(HttpStatus.FORBIDDEN.value(), patchException.statusCode)

                val stateException =
                    assertThrows<ClientException> {
                        QaService.datasetJudgementControllerApi.setJudgementState(
                            datasetJudgementId,
                            DatasetJudgementState.FinishedWithDatasetAcceptance,
                        )
                    }
                assertEquals(HttpStatus.FORBIDDEN.value(), stateException.statusCode)
            }
        }
    }

    @Test
    fun `ensure posting a dataset judgement twice returns 409`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val datasetId = uploadDummySfdrDataset(companyId, bypassQa = false).dataId

        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            QaService.datasetJudgementControllerApi.postDatasetJudgement(datasetId)

            val exception =
                assertThrows<ClientException> {
                    QaService.datasetJudgementControllerApi.postDatasetJudgement(datasetId)
                }
            assertEquals(HttpStatus.CONFLICT.value(), exception.statusCode)
        }
    }

    @Test
    fun `ensure finishing an already-finished judgement throws an error`() {
        val (_, datasetJudgementId, _) = createDatasetWithJudgement()

        QaService.datasetJudgementControllerApi.setJudgementState(
            datasetJudgementId,
            DatasetJudgementState.FinishedWithDatasetAcceptance,
        )

        val exception =
            assertThrows<ClientException> {
                QaService.datasetJudgementControllerApi.setJudgementState(
                    datasetJudgementId,
                    DatasetJudgementState.FinishedWithDatasetRejection,
                )
            }
        assertEquals(HttpStatus.CONFLICT.value(), exception.statusCode)
    }

    private fun assertQaStatusOfDataset(
        expectedQaStatus: QaStatus,
        datasetId: String,
    ) = assertEquals(
        expectedQaStatus,
        QaService.qaControllerApi.getQaReviewResponseByDataId(UUID.fromString(datasetId)).qaStatus,
    )

    private fun assertDatasetJudgementState(
        datasetJudgementId: String,
        datasetJudgementState: DatasetJudgementState,
    ) = assertEquals(
        datasetJudgementState,
        QaService.datasetJudgementControllerApi.getDatasetJudgement(datasetJudgementId).judgementState,
    )

    private fun assertQaStatusOfDatapoint(
        expectedQaStatus: QaStatus,
        datapointId: String,
    ) = assertEquals(
        expectedQaStatus,
        QaService.qaControllerApi.getDataPointQaReviewInformationByDataId(datapointId)[0].qaStatus,
    )

    private fun assertNewDatapointWithQaStatusAccepted(
        datasetId: String,
        datapointType: String,
        expectedDataPointContent: String,
    ) {
        val dataset = QaService.qaControllerApi.getQaReviewResponseByDataId(UUID.fromString(datasetId))
        val datapointsInDataset =
            QaService.qaControllerApi.getDataPointQaReviewInformation(
                companyId = dataset.companyId,
                reportingPeriod = dataset.reportingPeriod,
                dataType = datapointType,
                qaStatus = QaStatus.Accepted,
            )
        val datapoints = datapointsInDataset.filter { it.dataPointType == datapointType }
        assertEquals(1, datapoints.size)
        val datapointId = datapoints[0].dataPointId
        assertEquals(
            expectedDataPointContent,
            Backend.dataPointControllerApi.getDataPoint(datapointId).dataPoint,
        )
        assertEquals(
            QaStatus.Accepted,
            QaService.qaControllerApi.getDataPointQaReviewInformationByDataId(datapointId)[0].qaStatus,
        )
    }
}

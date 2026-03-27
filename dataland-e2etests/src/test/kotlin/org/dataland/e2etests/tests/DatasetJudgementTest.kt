package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandqaservice.openApiClient.model.AcceptedDataPointSource
import org.dataland.datalandqaservice.openApiClient.model.DatasetJudgementState
import org.dataland.datalandqaservice.openApiClient.model.JudgementDetailsPatch
import org.dataland.datalandqaservice.openApiClient.model.QaDecision
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
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatasetJudgementTest {
    private val testDataProvider =
        FrameworkTestDataProvider.forFrameworkFixtures(SfdrData::class.java)
    private val dummyDataset = testDataProvider.getTData(1)[0]
    private val dummyReportingPeriod = "2026"
    private val apiAccessor = ApiAccessor()
    private val datapointType1 = "extendedDecimalScope1GhgEmissionsInTonnes"
    private val datapointType2 = "extendedDecimalScope2GhgEmissionsLocationBasedInTonnes"
    private val datapointType3 = "extendedDecimalScope2GhgEmissionsMarketBasedInTonnes"
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

    private fun patchJudgementDetails(
        datasetJudgementId: String,
        dataPointType: String,
        acceptedSource: AcceptedDataPointSource,
        reporterUserIdOfAcceptedQaReport: String?,
        customDataPoint: String?,
    ) {
        QaService.datasetJudgementControllerApi.patchJudgementDetails(
            datasetJudgementId,
            dataPointType,
            JudgementDetailsPatch(
                acceptedSource,
                reporterUserIdOfAcceptedQaReport,
                customDataPoint,
            ),
        )
    }

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

        val datapointId1 = dataPoints[datapointType1]!!
        val datapointId2 = dataPoints[datapointType2]!!
        val datapointId3 = dataPoints[datapointType3]!!
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
                    PatchOperation(datapointType1, AcceptedDataPointSource.Qa, reporterUserId1),
                    PatchOperation(datapointType2, AcceptedDataPointSource.Qa, reporterUserId2),
                    PatchOperation(datapointType1, AcceptedDataPointSource.Original),
                    PatchOperation(datapointType2, AcceptedDataPointSource.Qa, reporterUserId2),
                    PatchOperation(datapointType3, AcceptedDataPointSource.Custom, null, customDataPoint),
                ) +
                    dataPoints
                        .filter { (_, dataPointId) -> dataPointId !in explicitlyHandledDataPointIds }
                        .keys
                        .map { dataPointType ->
                            PatchOperation(dataPointType, AcceptedDataPointSource.Original)
                        }

            val currentDatasetJudgementId = requireNotNull(datasetJudgementId)
            patchOperations.forEach {
                patchJudgementDetails(
                    currentDatasetJudgementId,
                    it.dataPointType,
                    it.acceptedSource,
                    it.reporterUserIdOfAcceptedQaReport,
                    it.customDataPoint,
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
                datapointType1 to dataPoints[datapointType1]!!,
                datapointType2 to dataPoints[datapointType2]!!,
                datapointType3 to dataPoints[datapointType3]!!,
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

        assertEquals(AcceptedDataPointSource.Original, datasetJudgement.dataPoints[datapointType1]?.acceptedSource)
        assertNull(datasetJudgement.dataPoints[datapointType1]?.reporterUserIdOfAcceptedQaReport)

        assertEquals(AcceptedDataPointSource.Qa, datasetJudgement.dataPoints[datapointType2]?.acceptedSource)

        assertEquals(AcceptedDataPointSource.Custom, datasetJudgement.dataPoints[datapointType3]?.acceptedSource)
        assertEquals(customDataPoint, datasetJudgement.dataPoints[datapointType3]?.customValue)
    }

    @Test
    fun `ensure Finish Review works as expected`() {
        val datasetAndJudgementAndDataPointIds = createDatasetWithJudgement()
        val datasetId = datasetAndJudgementAndDataPointIds.datasetId
        val datasetJudgementId = datasetAndJudgementAndDataPointIds.datasetJudgementId

        QaService.datasetJudgementControllerApi.finishJudgement(datasetJudgementId, QaDecision.Accepted)

        assertQaStatusOfDataset(QaStatus.Accepted, datasetId)
        assertDatasetJudgementIsFinished(datasetJudgementId)

        assertQaStatusOfDatapoint(QaStatus.Accepted, datasetAndJudgementAndDataPointIds.datapointIds[datapointType1]!!)

        assertQaStatusOfDatapoint(QaStatus.Rejected, datasetAndJudgementAndDataPointIds.datapointIds[datapointType2]!!)
        assertNewDatapointWithQaStatusAccepted(
            datasetId,
            datapointType2,
            dummyQaReport2.correctedData!!,
        )

        assertQaStatusOfDatapoint(QaStatus.Rejected, datasetAndJudgementAndDataPointIds.datapointIds[datapointType3]!!)

        assertNewDatapointWithQaStatusAccepted(
            datasetId,
            datapointType3,
            customDataPoint,
        )
    }

    @Test
    fun `ensure Reject Dataset works as expected`() {
        val datasetAndJudgementAndDataPointIds = createDatasetWithJudgement()
        val datasetId = datasetAndJudgementAndDataPointIds.datasetId
        val datasetJudgementId = datasetAndJudgementAndDataPointIds.datasetJudgementId

        QaService.datasetJudgementControllerApi.finishJudgement(datasetJudgementId, QaDecision.Rejected)

        assertQaStatusOfDataset(QaStatus.Rejected, datasetId)
        assertDatasetJudgementIsFinished(datasetJudgementId)

        assertQaStatusOfDatapoint(QaStatus.Rejected, datasetAndJudgementAndDataPointIds.datapointIds[datapointType1]!!)
        assertQaStatusOfDatapoint(QaStatus.Rejected, datasetAndJudgementAndDataPointIds.datapointIds[datapointType2]!!)
        assertQaStatusOfDatapoint(QaStatus.Rejected, datasetAndJudgementAndDataPointIds.datapointIds[datapointType3]!!)
    }

    private fun assertQaStatusOfDataset(
        expectedQaStatus: QaStatus,
        datasetId: String,
    ) = assertEquals(
        expectedQaStatus,
        QaService.qaControllerApi.getQaReviewResponseByDataId(UUID.fromString(datasetId)).qaStatus,
    )

    private fun assertDatasetJudgementIsFinished(datasetJudgementId: String) =
        assertEquals(
            DatasetJudgementState.Finished,
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

package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandqaservice.openApiClient.model.AcceptedDataPointSource
import org.dataland.datalandqaservice.openApiClient.model.JudgementDetailsPatch
import org.dataland.datalandqaservice.openApiClient.model.QaReportDataPointString
import org.dataland.datalandqaservice.openApiClient.model.QaReportDataPointVerdict
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
    private val customDataPoint = "{ \"value\": \" 1000\", \"quality\": \"Reported\"}"

    private val dummyQaReport1 =
        QaReportDataPointString(
            comment = "",
            verdict = QaReportDataPointVerdict.QaAccepted,
            correctedData = "{ \"value\": \" 100\", \"quality\": \"Reported\"}",
        )

    private val dummyQaReport2 =
        QaReportDataPointString(
            comment = "",
            verdict = QaReportDataPointVerdict.QaRejected,
            correctedData = "{ \"value\": \" 200\", \"quality\": \"Reported\"}",
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

    @Test
    fun `ensure dataset judgement entities behave correctly`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val datasetId = uploadDummySfdrDataset(companyId, bypassQa = false).dataId
        val dataPoints = Backend.metaDataControllerApi.getContainedDataPoints(datasetId)
        val datapointId1 = dataPoints[datapointType1]!!
        val datapointId2 = dataPoints[datapointType2]!!

        val reporterUserId1 = postQaReport(datapointId1, dummyQaReport1)
        val reporterUserId2 = postQaReport(datapointId2, dummyQaReport2)

        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            val datasetJudgementId =
                QaService.datasetJudgementControllerApi
                    .postDatasetJudgement(datasetId)
                    .dataSetJudgementId

            data class PatchOperation(
                val dataPointType: String,
                val acceptedSource: AcceptedDataPointSource,
                val reporterUserIdOfAcceptedQaReport: String? = null,
                val customDataPoint: String? = null,
            )

            val patchOperations =
                listOf(
                    PatchOperation(datapointType1, AcceptedDataPointSource.Qa, reporterUserId1.toString()),
                    PatchOperation(datapointType2, AcceptedDataPointSource.Qa, reporterUserId2.toString()),
                    PatchOperation(datapointType1, AcceptedDataPointSource.Original),
                    PatchOperation(datapointType2, AcceptedDataPointSource.Qa, reporterUserId2.toString()),
                    PatchOperation(datapointType3, AcceptedDataPointSource.Custom, null, customDataPoint),
                )

            patchOperations.forEach {
                patchJudgementDetails(
                    datasetJudgementId,
                    it.dataPointType,
                    it.acceptedSource,
                    it.reporterUserIdOfAcceptedQaReport,
                    it.customDataPoint,
                )
            }
        }

        val datasetJudgement = QaService.datasetJudgementControllerApi.getDatasetJudgementsByDatasetId(datasetId)[0]

        assertEquals(AcceptedDataPointSource.Original, datasetJudgement.dataPoints[datapointType1]?.acceptedSource)
        assertNull(datasetJudgement.dataPoints[datapointType1]?.reporterUserIdOfAcceptedQaReport)

        assertEquals(AcceptedDataPointSource.Qa, datasetJudgement.dataPoints[datapointType2]?.acceptedSource)

        assertEquals(AcceptedDataPointSource.Custom, datasetJudgement.dataPoints[datapointType3]?.acceptedSource)
        assertEquals(customDataPoint, datasetJudgement.dataPoints[datapointType3]?.customValue)
    }
}

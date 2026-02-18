package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.SfdrData
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
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatasetReviewTest {
    private val testDataProvider =
        FrameworkTestDataProvider.forFrameworkFixtures(SfdrData::class.java)
    private val dummyDataset = testDataProvider.getTData(1)[0]
    private val dummyReportingPeriod = "2026"
    private val apiAccessor = ApiAccessor()
    private val datapointType1 = "extendedDecimalScope1GhgEmissionsInTonnes"
    private val datapointType2 = "extendedDecimalScope2GhgEmissionsLocationBasedInTonnes"
    private val datapointType3 = "extendedDecimalScope2GhgEmissionsInTonnes"
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

    @Test
    fun `ensure dataset review entities behave correctly`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val datasetId = uploadDummySfdrDataset(companyId, bypassQa = false).dataId
        val dataPoints = Backend.metaDataControllerApi.getContainedDataPoints(datasetId)
        val datapointId1 = dataPoints[datapointType1]!!
        val datapointId2 = dataPoints[datapointType2]!!

        val uploadedQaReportId1 =
            QaService.dataPointQaReportControllerApi
                .postQaReport(datapointId1, dummyQaReport1)
                .qaReportId

        val uploadedQaReportId2 =
            QaService.dataPointQaReportControllerApi
                .postQaReport(datapointId2, dummyQaReport2)
                .qaReportId

        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            val datasetReviewId = QaService.datasetReviewControllerApi.postDatasetReview(datasetId).dataSetReviewId
            QaService.datasetReviewControllerApi.acceptQaReport(datasetReviewId, uploadedQaReportId1)
            QaService.datasetReviewControllerApi.acceptQaReport(datasetReviewId, uploadedQaReportId2)
            QaService.datasetReviewControllerApi.acceptOriginalDatapoint(
                datasetReviewId,
                datapointId1,
            )
            QaService.datasetReviewControllerApi.acceptCustomDataPoint(
                datasetReviewId,
                customDataPoint,
                datapointType3,
            )
        }
        val datasetReview = QaService.datasetReviewControllerApi.getDatasetReviewsByDatasetId(datasetId)[0]
        assertEquals(
            mapOf(datapointType1 to datapointId1),
            datasetReview.approvedDataPointIds,
        )
        assertEquals(
            mapOf(datapointType2 to uploadedQaReportId2),
            datasetReview.approvedQaReportIds,
        )
        assertEquals(
            mapOf(datapointType3 to customDataPoint),
            datasetReview.approvedCustomDataPointIds,
        )
    }
}

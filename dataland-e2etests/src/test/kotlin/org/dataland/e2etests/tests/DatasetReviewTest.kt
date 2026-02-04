package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandqaservice.openApiClient.model.QaReportDataPointString
import org.dataland.datalandqaservice.openApiClient.model.QaReportDataPointVerdict
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.api.Backend
import org.dataland.e2etests.utils.api.QaService
import org.dataland.e2etests.utils.testDataProviders.FrameworkTestDataProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DatasetReviewTest {
    private val testDataProvider =
        FrameworkTestDataProvider.forFrameworkFixtures(SfdrData::class.java)
    private val dummyDataset = testDataProvider.getTData(1)[0]
    private val dummyReportingPeriod = "2026"
    private val apiAccessor = ApiAccessor()

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
        val datasetId = uploadDummySfdrDataset(companyId, bypassQa = true).dataId
        val dataPoints = Backend.metaDataControllerApi.getContainedDataPoints(datasetId)
        val datapoint1 = dataPoints["extendedDecimalScope1GhgEmissionsInTonnes"]!!
        val datapoint2 = dataPoints["extendedDecimalScope2GhgEmissionsLocationBasedInTonnes"]!!

        val dummyQaReport1 =
            QaReportDataPointString(
                comment = "",
                verdict = QaReportDataPointVerdict.QaAccepted,
                correctedData = "{ \"value\": \" 100\", \"quality\": \"Reported\"}",
            )

        val dummyQaReport2 =
            QaReportDataPointString(
                comment = "",
                verdict = QaReportDataPointVerdict.QaRejected,
                correctedData = "{ \"value\": \" 200\", \"quality\": \"Reported\"}",
            )

        val uploadedQaReportId1 =
            QaService.dataPointQaReportControllerApi
                .postQaReport(datapoint1, dummyQaReport1)
                .qaReportId
        val uploadedQaReportId2 =
            QaService.dataPointQaReportControllerApi
                .postQaReport(datapoint2, dummyQaReport2)
                .qaReportId
        val customDataPoint = "{ \"value\": \" 1000\", \"quality\": \"Reported\"}"

        val datasetReviewId = QaService.datasetReviewControllerApi.postDatasetReview(datasetId).dataSetReviewId

        QaService.datasetReviewControllerApi.acceptQaReport(datasetReviewId, uploadedQaReportId1)
        QaService.datasetReviewControllerApi.acceptQaReport(datasetReviewId, uploadedQaReportId2)
        QaService.datasetReviewControllerApi.acceptOriginalDatapoint(
            datasetReviewId,
            datapoint1,
        )
        QaService.datasetReviewControllerApi.acceptCustomDataPoint(
            datasetReviewId,
            customDataPoint,
            "extendedDecimalScope2GhgEmissionsInTonnes",
        )

        val datasetReview = QaService.datasetReviewControllerApi.getDatasetReviewsByDatasetId(datasetId)[0]
        assertEquals(
            mapOf("extendedDecimalScope2GhgEmissionsLocationBasedInTonnes" to uploadedQaReportId2),
            datasetReview.approvedQaReportIds,
        )
        assertEquals(
            mapOf("extendedDecimalScope2GhgEmissionsLocationBasedInTonnes" to datapoint1),
            datasetReview.approvedDataPointIds,
        )
        assertEquals(
            mapOf("extendedDecimalScope2GhgEmissionsInTonnes" to customDataPoint),
            datasetReview.approvedCustomDataPointIds,
        )
    }
}

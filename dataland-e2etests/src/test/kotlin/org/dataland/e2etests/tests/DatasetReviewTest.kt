package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandqaservice.openApiClient.model.AcceptedDataPointSource
import org.dataland.datalandqaservice.openApiClient.model.QaReportDataPointString
import org.dataland.datalandqaservice.openApiClient.model.QaReportDataPointVerdict
import org.dataland.datalandqaservice.openApiClient.model.ReviewDetailsPatch
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
class DatasetReviewTest {
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

    @Test
    fun `ensure dataset review entities behave correctly`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val datasetId = uploadDummySfdrDataset(companyId, bypassQa = false).dataId
        val dataPoints = Backend.metaDataControllerApi.getContainedDataPoints(datasetId)
        val datapointId1 = dataPoints[datapointType1]!!
        val datapointId2 = dataPoints[datapointType2]!!

        val reporterUserId1 =
            QaService.dataPointQaReportControllerApi
                .postQaReport(datapointId1, dummyQaReport1)
                .reporterUserId
        val reporterUserId2 =
            QaService.dataPointQaReportControllerApi
                .postQaReport(datapointId2, dummyQaReport2)
                .reporterUserId

        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            val datasetReview = QaService.datasetReviewControllerApi.postDatasetReview(datasetId)
            val datasetReviewId = datasetReview.dataSetReviewId

            QaService.datasetReviewControllerApi.patchReviewDetails(
                datasetReviewId,
                datapointType1,
                ReviewDetailsPatch(
                    AcceptedDataPointSource.Qa,
                    reporterUserId1.toString(),
                    null,
                ),
            )

            QaService.datasetReviewControllerApi.patchReviewDetails(
                datasetReviewId,
                datapointType2,
                ReviewDetailsPatch(
                    AcceptedDataPointSource.Qa,
                    reporterUserId2.toString(),
                    null,
                ),
            )

            QaService.datasetReviewControllerApi.patchReviewDetails(
                datasetReviewId,
                datapointType1,
                ReviewDetailsPatch(
                    AcceptedDataPointSource.Original,
                    null,
                    null,
                ),
            )
            QaService.datasetReviewControllerApi.patchReviewDetails(
                datasetReviewId,
                datapointType2,
                ReviewDetailsPatch(
                    AcceptedDataPointSource.Qa,
                    reporterUserId2.toString(),
                    null,
                ),
            )
            QaService.datasetReviewControllerApi.patchReviewDetails(
                datasetReviewId,
                datapointType3,
                ReviewDetailsPatch(
                    AcceptedDataPointSource.Custom,
                    null,
                    customDataPoint,
                ),
            )
        }

        val datasetReview = QaService.datasetReviewControllerApi.getDatasetReviewsByDatasetId(datasetId)[0]

        assertEquals(AcceptedDataPointSource.Original, datasetReview.dataPoints[datapointType1]?.acceptedSource)
        assertNull(datasetReview.dataPoints[datapointType1]?.reporterUserIdOfAcceptedQaReport)
        assertNull(datasetReview.dataPoints[datapointType1]?.companyIdOfAcceptedQaReport)

        assertEquals(AcceptedDataPointSource.Qa, datasetReview.dataPoints[datapointType2]?.acceptedSource)

        assertEquals(AcceptedDataPointSource.Custom, datasetReview.dataPoints[datapointType3]?.acceptedSource)
        assertEquals(customDataPoint, datasetReview.dataPoints[datapointType3]?.customValue)
    }
}

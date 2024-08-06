package org.dataland.e2etests.tests

import org.dataland.datalandqaservice.openApiClient.infrastructure.ClientException
import org.dataland.datalandqaservice.openApiClient.model.QaReportMetaInformation
import org.dataland.datalandqaservice.openApiClient.model.SfdrData
import org.dataland.e2etests.auth.GlobalAuth.withTechnicalUser
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentManagerAccessor
import org.dataland.e2etests.utils.QaApiAccessor
import org.dataland.e2etests.utils.UploadConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QaReportControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentManagerAccessor()
    private val qaApiAccessor = QaApiAccessor()

    private val listOfOneSfdrDataSet = apiAccessor.testDataProviderForSfdrData.getTData(1)
    private val listOfOneCompanyInformation = apiAccessor.testDataProviderForSfdrData
        .getCompanyInformationWithoutIdentifiers(1)

    @BeforeAll
    fun postTestDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    private fun postEmptyQaReportForNewDataId(bypassQa: Boolean): QaReportMetaInformation {
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            listOfOneCompanyInformation,
            listOfOneSfdrDataSet,
            apiAccessor::sfdrUploaderFunction,
            uploadConfig = UploadConfiguration(bypassQa = bypassQa),
            ensureQaPassed = bypassQa,
        )
        return withTechnicalUser(TechnicalUser.Admin) {
            qaApiAccessor.sfdrQaReportControllerApi.postQaReport(
                listOfUploadInfo[0].actualStoredDataMetaInfo!!.dataId,
                SfdrData(),
            )
        }
    }

    private fun assertCanAccessQaDataset(reportMetaInformation: QaReportMetaInformation) {
        assertEquals(
            reportMetaInformation.dataId,
            qaApiAccessor.sfdrQaReportControllerApi.getQaReport(
                dataId = reportMetaInformation.dataId,
                qaReportId = reportMetaInformation.qaReportId,
            ).metaInfo.dataId,
        )
    }

    private fun assertCannotAccessQaDataset(reportMetaInformation: QaReportMetaInformation) {
        val exception = assertThrows<ClientException> {
            qaApiAccessor.sfdrQaReportControllerApi.getQaReport(
                dataId = reportMetaInformation.dataId,
                qaReportId = reportMetaInformation.qaReportId,
            )
        }
        assertEquals(exception.statusCode, HttpStatus.FORBIDDEN.value())
    }

    @Test
    fun `post a qa report and check retrieval permissions`() {
        val reportMetaInfo = postEmptyQaReportForNewDataId(bypassQa = false)

        withTechnicalUser(TechnicalUser.Admin) { assertCanAccessQaDataset(reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Reviewer) { assertCanAccessQaDataset(reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Uploader) { assertCannotAccessQaDataset(reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Reader) { assertCannotAccessQaDataset(reportMetaInfo) }
    }

    @Test
    fun `post a qa report and check that it is deleted with the dataset`() {
        val reportMetaInfo = postEmptyQaReportForNewDataId(bypassQa = true)
        withTechnicalUser(TechnicalUser.Admin) {
            assertCanAccessQaDataset(reportMetaInfo)
            apiAccessor.dataDeletionControllerApi.deleteCompanyAssociatedData(reportMetaInfo.dataId)

            val exception = assertThrows<ClientException> {
                qaApiAccessor.sfdrQaReportControllerApi.getQaReport(
                    dataId = reportMetaInfo.dataId,
                    qaReportId = reportMetaInfo.qaReportId,
                )
            }
            assertEquals(HttpStatus.NOT_FOUND.value(), exception.statusCode)
        }
    }

// TODO
//    @Test
//    fun `Post a QA report and check that the report can be retrieved`() {
//        val dataId = postSfdrDatasetAndRetrieveDataId()
//        val sfdrData = qaApiAccessor.createFullQaSfdrData()
//        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reviewer)
//        val qaReportMetaInfo = qaApiAccessor.sfdrQaReportControllerApi.postQaReport(dataId, sfdrData)
//
//        val retrievedReport = qaApiAccessor.sfdrQaReportControllerApi.getQaReport(
//            dataId = dataId,
//            qaReportId = qaReportMetaInfo.qaReportId,
//        )
//
//        assertEquals(retrievedReport.metaInfo.reporterUserId, dataReviewerUserId)
//        assertEquals(retrievedReport.metaInfo.dataId, dataId)
//        assertEquals(retrievedReport.metaInfo.dataType, "sfdr")
//        assertEquals(retrievedReport.report, sfdrData, "the uploaded and retrieved reports do not match")
//    }
//

//
//    private fun postSfdrDatasetAndRetrieveDataId(): String {
//        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
//        return apiAccessor.dataControllerApiForSfdrData.postCompanyAssociatedSfdrData(
//            dummyCompanyAssociatedData, true,
//        ).dataId
//    }
}

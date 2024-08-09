package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.openApiClient.infrastructure.ClientException
import org.dataland.datalandqaservice.openApiClient.model.QaReportMetaInformation
import org.dataland.e2etests.auth.GlobalAuth.withTechnicalUser
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentManagerAccessor
import org.dataland.e2etests.utils.QaApiAccessor
import org.dataland.e2etests.utils.UploadConfiguration
import org.dataland.e2etests.utils.testDataProvivders.QaReportTestDataProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import java.util.*
import org.dataland.datalandqaservice.openApiClient.model.SfdrData as SfdrQaReport

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QaReportControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentManagerAccessor()
    private val qaApiAccessor = QaApiAccessor()

    private val testSfdrDataset = apiAccessor.testDataProviderForSfdrData.getTData(1)
    private val testCompanyInformation = apiAccessor.testDataProviderForSfdrData
        .getCompanyInformationWithoutIdentifiers(1)

    @BeforeAll
    fun postTestDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    private fun postSfdrQaReportForNewDataId(
        sfdrQaReport: SfdrQaReport = SfdrQaReport(),
        bypassQa: Boolean,
    ): QaReportMetaInformation {
        val uploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            testCompanyInformation,
            testSfdrDataset,
            apiAccessor::sfdrUploaderFunction,
            uploadConfig = UploadConfiguration(bypassQa = bypassQa),
            ensureQaPassed = bypassQa,
        )
        val dataIdOfUpload = uploadInfo.first().actualStoredDataMetaInfo!!.dataId
        return withTechnicalUser(TechnicalUser.Admin) {
            qaApiAccessor.sfdrQaReportControllerApi.postQaReport(dataIdOfUpload, sfdrQaReport)
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
        val reportMetaInfo = postSfdrQaReportForNewDataId(bypassQa = false)

        withTechnicalUser(TechnicalUser.Admin) { assertCanAccessQaDataset(reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Reviewer) { assertCanAccessQaDataset(reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Uploader) { assertCannotAccessQaDataset(reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Reader) { assertCannotAccessQaDataset(reportMetaInfo) }
    }

    @Test
    fun `post a qa report and check that it is deleted with the dataset`() {
        val reportMetaInfo = postSfdrQaReportForNewDataId(bypassQa = true)
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

    @Test
    fun `post a QA report and check that the report can be retrieved`() {
        val qaReportTestDataProvider = QaReportTestDataProvider(SfdrQaReport::class.java)
        val sfdrQaReportWithOneCorrection = qaReportTestDataProvider.getTData(1).first()

        val sfdrQaReportMetaInfo = postSfdrQaReportForNewDataId(sfdrQaReportWithOneCorrection, true)

        val userUploadingTheQaReport = TechnicalUser.Admin
        withTechnicalUser(userUploadingTheQaReport) {
            val retrievedReport = qaApiAccessor.sfdrQaReportControllerApi.getQaReport(
                sfdrQaReportMetaInfo.dataId,
                sfdrQaReportMetaInfo.qaReportId,
            )
            assertEquals(userUploadingTheQaReport.technicalUserId, retrievedReport.metaInfo.reporterUserId)
            assertEquals(sfdrQaReportMetaInfo.dataId, retrievedReport.metaInfo.dataId)
            assertEquals(DataTypeEnum.sfdr.value, retrievedReport.metaInfo.dataType)
            assertEquals(sfdrQaReportWithOneCorrection, retrievedReport.report)
        }
    }
}

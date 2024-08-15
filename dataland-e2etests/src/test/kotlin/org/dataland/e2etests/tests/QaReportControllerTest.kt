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
import org.dataland.datalandqaservice.openApiClient.model.EutaxonomyNonFinancialsData as EuTaxonomyNonFinancialsReport
import org.dataland.datalandqaservice.openApiClient.model.SfdrData as SfdrQaReport

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QaReportControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentManagerAccessor()
    private val qaApiAccessor = QaApiAccessor()

    private val testSfdrDataset = apiAccessor.testDataProviderForSfdrData.getTData(1)
    private val testEuTaxonomyNonFinancialsDataset =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getTData(1)
    private val testCompanyInformationSfdr = apiAccessor.testDataProviderForSfdrData
        .getCompanyInformationWithoutIdentifiers(1)
    private val testCompanyInformationEuTaxonomyNonFinancials =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getCompanyInformationWithoutIdentifiers(1)

    @BeforeAll
    fun postTestDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    private fun postSfdrQaReportForNewDataId(
        sfdrQaReport: SfdrQaReport = SfdrQaReport(),
        bypassQa: Boolean,
    ): QaReportMetaInformation {
        val uploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            testCompanyInformationSfdr,
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

    private fun postEuTaxonomyNonFinancialsQaReportForNewDataId(
        euTaxonomyNonFinancialsQaReport: EuTaxonomyNonFinancialsReport = EuTaxonomyNonFinancialsReport(),
        bypassQa: Boolean,
    ): QaReportMetaInformation {
        val uploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            testCompanyInformationEuTaxonomyNonFinancials,
            testEuTaxonomyNonFinancialsDataset,
            apiAccessor::euTaxonomyNonFinancialsUploaderFunction,
            uploadConfig = UploadConfiguration(bypassQa = bypassQa),
            ensureQaPassed = bypassQa,
        )
        val dataIdOfUpload = uploadInfo.first().actualStoredDataMetaInfo!!.dataId
        return withTechnicalUser(TechnicalUser.Admin) {
            qaApiAccessor.euTaxonomyNonFinancialsQaReportControllerApi.postQaReport1(
                dataIdOfUpload,
                euTaxonomyNonFinancialsQaReport,
            )
        }
    }

    private fun assertCanAccessSfdrQaDataset(reportMetaInformation: QaReportMetaInformation) {
        assertEquals(
            reportMetaInformation.dataId,
            qaApiAccessor.sfdrQaReportControllerApi.getQaReport(
                dataId = reportMetaInformation.dataId,
                qaReportId = reportMetaInformation.qaReportId,
            ).metaInfo.dataId,
        )
    }

    private fun assertCanAccessEuTaxonomyNonFinancialsQaDataset(reportMetaInformation: QaReportMetaInformation) {
        assertEquals(
            reportMetaInformation.dataId,
            qaApiAccessor.euTaxonomyNonFinancialsQaReportControllerApi.getQaReport1(
                dataId = reportMetaInformation.dataId,
                qaReportId = reportMetaInformation.qaReportId,
            ).metaInfo.dataId,
        )
    }

    private fun assertCannotAccessSfdrQaDataset(reportMetaInformation: QaReportMetaInformation) {
        val exception = assertThrows<ClientException> {
            qaApiAccessor.sfdrQaReportControllerApi.getQaReport(
                dataId = reportMetaInformation.dataId,
                qaReportId = reportMetaInformation.qaReportId,
            )
        }
        assertEquals(exception.statusCode, HttpStatus.FORBIDDEN.value())
    }

    private fun assertCannotAccessEuTaxonomyNonFinancialsQaDataset(reportMetaInformation: QaReportMetaInformation) {
        val exception = assertThrows<ClientException> {
            qaApiAccessor.euTaxonomyNonFinancialsQaReportControllerApi.getQaReport1(
                dataId = reportMetaInformation.dataId,
                qaReportId = reportMetaInformation.qaReportId,
            )
        }
        assertEquals(exception.statusCode, HttpStatus.FORBIDDEN.value())
    }

    @Test
    fun `post an sfdr qa report and check retrieval permissions`() {
        val reportMetaInfo = postSfdrQaReportForNewDataId(bypassQa = false)

        withTechnicalUser(TechnicalUser.Admin) { assertCanAccessSfdrQaDataset(reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Reviewer) { assertCanAccessSfdrQaDataset(reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Uploader) { assertCannotAccessSfdrQaDataset(reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Reader) { assertCannotAccessSfdrQaDataset(reportMetaInfo) }
    }

    @Test
    fun `post an sfdr qa report and check that it is deleted with the dataset`() {
        val reportMetaInfo = postSfdrQaReportForNewDataId(bypassQa = true)
        withTechnicalUser(TechnicalUser.Admin) {
            assertCanAccessSfdrQaDataset(reportMetaInfo)
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
    fun `post an sfdr qa report and check that the report can be retrieved`() {
        val qaReportTestDataProvider = QaReportTestDataProvider(SfdrQaReport::class.java, "sfdr")
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

    @Test
    fun `post an eu taxonomy non financials qa report and check retrieval permissions`() {
        val reportMetaInfo = postEuTaxonomyNonFinancialsQaReportForNewDataId(bypassQa = false)

        withTechnicalUser(TechnicalUser.Admin) { assertCanAccessEuTaxonomyNonFinancialsQaDataset(reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Reviewer) { assertCanAccessEuTaxonomyNonFinancialsQaDataset(reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Uploader) { assertCannotAccessEuTaxonomyNonFinancialsQaDataset(reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Reader) { assertCannotAccessEuTaxonomyNonFinancialsQaDataset(reportMetaInfo) }
    }

    @Test
    fun `post an eu taxonomy non financials qa report and check that it is deleted with the dataset`() {
        val reportMetaInfo = postEuTaxonomyNonFinancialsQaReportForNewDataId(bypassQa = true)
        withTechnicalUser(TechnicalUser.Admin) {
            assertCanAccessEuTaxonomyNonFinancialsQaDataset(reportMetaInfo)
            apiAccessor.dataDeletionControllerApi.deleteCompanyAssociatedData(reportMetaInfo.dataId)

            val exception = assertThrows<ClientException> {
                qaApiAccessor.euTaxonomyNonFinancialsQaReportControllerApi.getQaReport1(
                    dataId = reportMetaInfo.dataId,
                    qaReportId = reportMetaInfo.qaReportId,
                )
            }
            assertEquals(HttpStatus.NOT_FOUND.value(), exception.statusCode)
        }
    }

    @Test
    fun `post an eu taxonomy non financials qa report and check that the report can be retrieved`() {
        val qaReportTestDataProvider =
            QaReportTestDataProvider(EuTaxonomyNonFinancialsReport::class.java, "eutaxonomy-non-financials")
        val euTaxonomyNonFinancialsQaReportWithOneCorrection = qaReportTestDataProvider.getTData(1).first()

        val euTaxonomyNonFinancialsQaReportMetaInfo =
            postEuTaxonomyNonFinancialsQaReportForNewDataId(euTaxonomyNonFinancialsQaReportWithOneCorrection, true)

        val userUploadingTheQaReport = TechnicalUser.Admin
        withTechnicalUser(userUploadingTheQaReport) {
            val retrievedReport = qaApiAccessor.euTaxonomyNonFinancialsQaReportControllerApi.getQaReport1(
                euTaxonomyNonFinancialsQaReportMetaInfo.dataId,
                euTaxonomyNonFinancialsQaReportMetaInfo.qaReportId,
            )
            assertEquals(userUploadingTheQaReport.technicalUserId, retrievedReport.metaInfo.reporterUserId)
            assertEquals(euTaxonomyNonFinancialsQaReportMetaInfo.dataId, retrievedReport.metaInfo.dataId)
            assertEquals(DataTypeEnum.eutaxonomyMinusNonMinusFinancials.value, retrievedReport.metaInfo.dataType)
            assertEquals(euTaxonomyNonFinancialsQaReportWithOneCorrection, retrievedReport.report)
        }
    }
}

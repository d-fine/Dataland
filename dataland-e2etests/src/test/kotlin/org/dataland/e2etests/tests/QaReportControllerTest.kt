package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.openApiClient.infrastructure.ClientException
import org.dataland.datalandqaservice.openApiClient.model.EutaxonomyNonFinancialsData
import org.dataland.datalandqaservice.openApiClient.model.QaReportMetaInformation
import org.dataland.datalandqaservice.openApiClient.model.SfdrData
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QaReportControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentManagerAccessor()
    private val qaApiAccessor = QaApiAccessor()

    private val testSfdrDataset = apiAccessor.testDataProviderForSfdrData.getTData(1)
    private val testEuTaxonomyNonFinancialsDataset =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getTData(1)
    private val testCompanyInformationForSfdrData = apiAccessor.testDataProviderForSfdrData
        .getCompanyInformationWithoutIdentifiers(1)
    private val testCompanyInformationForEuTaxonomyNonFinancialsData =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1)

    @BeforeAll
    fun postTestDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    private fun postSfdrQaReportForNewDataId(
        sfdrQaReport: SfdrData,
        bypassQa: Boolean,
    ): QaReportMetaInformation {
        val uploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            testCompanyInformationForSfdrData,
            testSfdrDataset,
            apiAccessor::sfdrUploaderFunction,
            uploadConfig = UploadConfiguration(bypassQa = bypassQa),
            ensureQaPassed = bypassQa,
        )
        val dataIdOfUpload = uploadInfo.first().actualStoredDataMetaInfo!!.dataId
        return withTechnicalUser(TechnicalUser.Admin) {
            qaApiAccessor.sfdrQaReportControllerApi.postSfdrDataQaReport(dataIdOfUpload, sfdrQaReport)
        }
    }

    private fun postEuTaxonomyNonFinancialsQaReportForNewDataId(
        euTaxonomyNonFinancialsQaReport: EutaxonomyNonFinancialsData,
        bypassQa: Boolean,
    ): QaReportMetaInformation {
        val uploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            testCompanyInformationForEuTaxonomyNonFinancialsData,
            testEuTaxonomyNonFinancialsDataset,
            apiAccessor::euTaxonomyNonFinancialsUploaderFunction,
            uploadConfig = UploadConfiguration(bypassQa = bypassQa),
            ensureQaPassed = bypassQa,
        )
        val dataIdOfUpload = uploadInfo.first().actualStoredDataMetaInfo!!.dataId
        return withTechnicalUser(TechnicalUser.Admin) {
            qaApiAccessor.euTaxonomyNonFinancialsQaReportControllerApi.postEutaxonomyNonFinancialsDataQaReport(
                dataIdOfUpload,
                euTaxonomyNonFinancialsQaReport,
            )
        }
    }

    private fun assertCanAccessSfdrQaDataset(reportMetaInformation: QaReportMetaInformation) {
        assertEquals(
            reportMetaInformation.dataId,
            qaApiAccessor.sfdrQaReportControllerApi.getSfdrDataQaReport(
                dataId = reportMetaInformation.dataId,
                qaReportId = reportMetaInformation.qaReportId,
            ).metaInfo.dataId,
        )
    }

    private fun assertCanAccessEuTaxonomyNonFinancialsQaDataset(reportMetaInformation: QaReportMetaInformation) {
        assertEquals(
            reportMetaInformation.dataId,
            qaApiAccessor.euTaxonomyNonFinancialsQaReportControllerApi.getEutaxonomyNonFinancialsDataQaReport(
                dataId = reportMetaInformation.dataId,
                qaReportId = reportMetaInformation.qaReportId,
            ).metaInfo.dataId,
        )
    }

    private fun assertCannotAccessSfdrQaDataset(reportMetaInformation: QaReportMetaInformation) {
        val exception = assertThrows<ClientException> {
            qaApiAccessor.sfdrQaReportControllerApi.getSfdrDataQaReport(
                dataId = reportMetaInformation.dataId,
                qaReportId = reportMetaInformation.qaReportId,
            )
        }
        assertEquals(exception.statusCode, HttpStatus.FORBIDDEN.value())
    }

    private fun assertCannotAccessEuTaxonomyNonFinancialsQaDataset(reportMetaInformation: QaReportMetaInformation) {
        val exception = assertThrows<ClientException> {
            qaApiAccessor.euTaxonomyNonFinancialsQaReportControllerApi.getEutaxonomyNonFinancialsDataQaReport(
                dataId = reportMetaInformation.dataId,
                qaReportId = reportMetaInformation.qaReportId,
            )
        }
        assertEquals(exception.statusCode, HttpStatus.FORBIDDEN.value())
    }

    @Test
    fun `post an sfdr qa report and check retrieval permissions`() {
        val sfdrQaReport = SfdrData()
        val reportMetaInfo = postSfdrQaReportForNewDataId(sfdrQaReport, bypassQa = false)

        withTechnicalUser(TechnicalUser.Admin) { assertCanAccessSfdrQaDataset(reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Reviewer) { assertCanAccessSfdrQaDataset(reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Uploader) { assertCannotAccessSfdrQaDataset(reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Reader) { assertCannotAccessSfdrQaDataset(reportMetaInfo) }
    }

    @Test
    fun `post an sfdr qa report and check that it is deleted with the dataset`() {
        val sfdrQaReport = SfdrData()
        val reportMetaInfo = postSfdrQaReportForNewDataId(sfdrQaReport, bypassQa = true)
        withTechnicalUser(TechnicalUser.Admin) {
            assertCanAccessSfdrQaDataset(reportMetaInfo)
            apiAccessor.dataDeletionControllerApi.deleteCompanyAssociatedData(reportMetaInfo.dataId)

            val exception = assertThrows<ClientException> {
                qaApiAccessor.sfdrQaReportControllerApi.getSfdrDataQaReport(
                    dataId = reportMetaInfo.dataId,
                    qaReportId = reportMetaInfo.qaReportId,
                )
            }
            assertEquals(HttpStatus.NOT_FOUND.value(), exception.statusCode)
        }
    }

    @Test
    fun `post an sfdr qa report and check that the report can be retrieved`() {
        val qaReportTestDataProvider = QaReportTestDataProvider(SfdrData::class.java, "sfdr")
        val sfdrQaReportWithOneCorrection = qaReportTestDataProvider.getTData(1).first()

        val sfdrQaReportMetaInfo = postSfdrQaReportForNewDataId(sfdrQaReportWithOneCorrection, true)

        val userUploadingTheQaReport = TechnicalUser.Admin
        withTechnicalUser(userUploadingTheQaReport) {
            val retrievedReport = qaApiAccessor.sfdrQaReportControllerApi.getSfdrDataQaReport(
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
        val euTaxonomyNonFinancialsQaReport = EutaxonomyNonFinancialsData()
        val reportMetaInfo =
            postEuTaxonomyNonFinancialsQaReportForNewDataId(euTaxonomyNonFinancialsQaReport, bypassQa = false)

        withTechnicalUser(TechnicalUser.Admin) { assertCanAccessEuTaxonomyNonFinancialsQaDataset(reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Reviewer) { assertCanAccessEuTaxonomyNonFinancialsQaDataset(reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Uploader) { assertCannotAccessEuTaxonomyNonFinancialsQaDataset(reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Reader) { assertCannotAccessEuTaxonomyNonFinancialsQaDataset(reportMetaInfo) }
    }

    @Test
    fun `post an eu taxonomy non financials qa report and check that it is deleted with the dataset`() {
        val euTaxonomyNonFinancialsQaReport = EutaxonomyNonFinancialsData()
        val reportMetaInfo =
            postEuTaxonomyNonFinancialsQaReportForNewDataId(euTaxonomyNonFinancialsQaReport, bypassQa = true)
        withTechnicalUser(TechnicalUser.Admin) {
            assertCanAccessEuTaxonomyNonFinancialsQaDataset(reportMetaInfo)
            apiAccessor.dataDeletionControllerApi.deleteCompanyAssociatedData(reportMetaInfo.dataId)

            val exception = assertThrows<ClientException> {
                qaApiAccessor.euTaxonomyNonFinancialsQaReportControllerApi.getEutaxonomyNonFinancialsDataQaReport(
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
            QaReportTestDataProvider(EutaxonomyNonFinancialsData::class.java, "eutaxonomy-non-financials")
        val euTaxonomyNonFinancialsQaReportWithOneCorrection = qaReportTestDataProvider.getTData(1)
            .first()

        val euTaxonomyNonFinancialsQaReportMetaInfo =
            postEuTaxonomyNonFinancialsQaReportForNewDataId(
                euTaxonomyNonFinancialsQaReportWithOneCorrection,
                true,
            )

        val userUploadingTheQaReport = TechnicalUser.Admin
        withTechnicalUser(userUploadingTheQaReport) {
            val retrievedReport =
                qaApiAccessor.euTaxonomyNonFinancialsQaReportControllerApi.getEutaxonomyNonFinancialsDataQaReport(
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

package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandqaservice.openApiClient.infrastructure.ClientException
import org.dataland.datalandqaservice.openApiClient.model.QaReportMetaInformation
import org.dataland.e2etests.auth.GlobalAuth.withTechnicalUser
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentControllerApiAccessor
import org.dataland.e2etests.utils.QaApiAccessor
import org.dataland.e2etests.utils.UploadConfiguration
import org.dataland.e2etests.utils.UploadInfo
import org.dataland.e2etests.utils.testDataProviders.FrameworkTestDataProvider
import org.dataland.e2etests.utils.testDataProviders.QaReportTestDataProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.dataland.datalandqaservice.openApiClient.model.EutaxonomyNonFinancialsData
as EuTaxonomyNonFinancialsQaReport
import org.dataland.datalandqaservice.openApiClient.model.SfdrData as SfdrQaReport

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QaReportControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentControllerApiAccessor()
    private val qaApiAccessor = QaApiAccessor()

    private val testSfdrDataset =
        FrameworkTestDataProvider
            .forFrameworkPreparedFixtures(SfdrData::class.java)
            .getByCompanyName("Sfdr-dataset-with-no-null-fields")
            .t
    private val testEuTaxonomyNonFinancialsDataset =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getTData(1)
    private val testCompanyInformationForSfdrData =
        apiAccessor.testDataProviderForSfdrData
            .getCompanyInformationWithoutIdentifiers(1)
    private val testCompanyInformationForEuTaxonomyNonFinancialsData =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1)
    private val sfdrQaReportTestDataProvider = QaReportTestDataProvider(SfdrQaReport::class.java, "sfdr")

    @BeforeAll
    fun postTestDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    private fun <T> uploadFrameworkData(
        qaReport: T,
        bypassQa: Boolean,
    ): List<UploadInfo> =
        when (qaReport) {
            is SfdrQaReport ->
                apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
                    testCompanyInformationForSfdrData,
                    listOf(testSfdrDataset),
                    apiAccessor::sfdrUploaderFunction,
                    uploadConfig = UploadConfiguration(bypassQa = bypassQa),
                    ensureQaPassed = bypassQa,
                )

            is EuTaxonomyNonFinancialsQaReport ->
                apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
                    testCompanyInformationForEuTaxonomyNonFinancialsData,
                    testEuTaxonomyNonFinancialsDataset,
                    apiAccessor::euTaxonomyNonFinancialsUploaderFunction,
                    uploadConfig = UploadConfiguration(bypassQa = bypassQa),
                    ensureQaPassed = bypassQa,
                )

            else -> throw IllegalArgumentException("The framework of $qaReport does not support QA reports.")
        }

    private fun <T> postQaReportForNewDataId(
        qaReport: T,
        bypassQa: Boolean,
    ): QaReportMetaInformation {
        val uploadInfo = uploadFrameworkData(qaReport, bypassQa)
        val dataIdOfUpload = uploadInfo.first().actualStoredDataMetaInfo!!.dataId
        return withTechnicalUser(TechnicalUser.Admin) {
            when (qaReport) {
                is SfdrQaReport ->
                    qaApiAccessor.sfdrQaReportControllerApi
                        .postSfdrDataQaReport(dataIdOfUpload, qaReport)

                is EuTaxonomyNonFinancialsQaReport ->
                    qaApiAccessor.euTaxonomyNonFinancialsQaReportControllerApi
                        .postEutaxonomyNonFinancialsDataQaReport(dataIdOfUpload, qaReport)

                else -> throw IllegalArgumentException("The framework of $qaReport does not support QA reports.")
            }
        }
    }

    private fun <T> assertCanAccessQaDataset(
        qaReport: T,
        reportMetaInformation: QaReportMetaInformation,
    ) {
        assertEquals(
            reportMetaInformation.dataId,
            when (qaReport) {
                is SfdrQaReport ->
                    qaApiAccessor.sfdrQaReportControllerApi
                        .getSfdrDataQaReport(
                            dataId = reportMetaInformation.dataId,
                            qaReportId = reportMetaInformation.qaReportId,
                        ).metaInfo.dataId

                is EuTaxonomyNonFinancialsQaReport ->
                    qaApiAccessor.euTaxonomyNonFinancialsQaReportControllerApi
                        .getEutaxonomyNonFinancialsDataQaReport(
                            dataId = reportMetaInformation.dataId,
                            qaReportId = reportMetaInformation.qaReportId,
                        ).metaInfo.dataId

                else -> throw IllegalArgumentException("The framework of $qaReport does not support QA reports.")
            },
        )
    }

    private fun <T> assertCannotAccessQaDataset(
        qaReport: T,
        reportMetaInformation: QaReportMetaInformation,
    ) {
        val exception =
            assertThrows<ClientException> {
                when (qaReport) {
                    is SfdrQaReport ->
                        qaApiAccessor.sfdrQaReportControllerApi.getSfdrDataQaReport(
                            dataId = reportMetaInformation.dataId,
                            qaReportId = reportMetaInformation.qaReportId,
                        )

                    is EuTaxonomyNonFinancialsQaReport ->
                        qaApiAccessor.euTaxonomyNonFinancialsQaReportControllerApi
                            .getEutaxonomyNonFinancialsDataQaReport(
                                dataId = reportMetaInformation.dataId,
                                qaReportId = reportMetaInformation.qaReportId,
                            )

                    else -> throw IllegalArgumentException("The framework of $qaReport does not support QA reports.")
                }
            }
        assertEquals(exception.statusCode, HttpStatus.FORBIDDEN.value())
    }

    @Test
    fun `post an sfdr qa report and check retrieval permissions`() {
        val sfdrQaReport = sfdrQaReportTestDataProvider.getTData(1).first()
        val reportMetaInfo = postQaReportForNewDataId(sfdrQaReport, bypassQa = false)

        withTechnicalUser(TechnicalUser.Admin) { assertCanAccessQaDataset(sfdrQaReport, reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Reviewer) { assertCanAccessQaDataset(sfdrQaReport, reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Uploader) { assertCannotAccessQaDataset(sfdrQaReport, reportMetaInfo) }
        withTechnicalUser(TechnicalUser.Reader) { assertCannotAccessQaDataset(sfdrQaReport, reportMetaInfo) }
    }

    @Test
    fun `post an sfdr qa report and check that it is deleted with the dataset`() {
        val sfdrQaReport = sfdrQaReportTestDataProvider.getTData(1).first()
        val reportMetaInfo = postQaReportForNewDataId(sfdrQaReport, bypassQa = true)
        withTechnicalUser(TechnicalUser.Admin) {
            assertCanAccessQaDataset(sfdrQaReport, reportMetaInfo)
            apiAccessor.dataDeletionControllerApi.deleteCompanyAssociatedData(reportMetaInfo.dataId)

            val exception =
                assertThrows<ClientException> {
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
        val sfdrQaReportWithOneCorrection = sfdrQaReportTestDataProvider.getTData(1).first()

        val sfdrQaReportMetaInfo = postQaReportForNewDataId(sfdrQaReportWithOneCorrection, true)

        val userUploadingTheQaReport = TechnicalUser.Admin
        withTechnicalUser(userUploadingTheQaReport) {
            val retrievedReport =
                qaApiAccessor.sfdrQaReportControllerApi.getSfdrDataQaReport(
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
        val euTaxonomyNonFinancialsQaReport = EuTaxonomyNonFinancialsQaReport()
        val reportMetaInfo =
            postQaReportForNewDataId(euTaxonomyNonFinancialsQaReport, bypassQa = false)

        withTechnicalUser(TechnicalUser.Admin) {
            assertCanAccessQaDataset(
                euTaxonomyNonFinancialsQaReport,
                reportMetaInfo,
            )
        }
        withTechnicalUser(TechnicalUser.Reviewer) {
            assertCanAccessQaDataset(
                euTaxonomyNonFinancialsQaReport,
                reportMetaInfo,
            )
        }
        withTechnicalUser(TechnicalUser.Uploader) {
            assertCannotAccessQaDataset(
                euTaxonomyNonFinancialsQaReport,
                reportMetaInfo,
            )
        }
        withTechnicalUser(TechnicalUser.Reader) {
            assertCannotAccessQaDataset(
                euTaxonomyNonFinancialsQaReport,
                reportMetaInfo,
            )
        }
    }

    @Test
    fun `post an eu taxonomy non financials qa report and check that it is deleted with the dataset`() {
        val euTaxonomyNonFinancialsQaReport = EuTaxonomyNonFinancialsQaReport()
        val reportMetaInfo =
            postQaReportForNewDataId(euTaxonomyNonFinancialsQaReport, bypassQa = true)
        withTechnicalUser(TechnicalUser.Admin) {
            assertCanAccessQaDataset(euTaxonomyNonFinancialsQaReport, reportMetaInfo)
            apiAccessor.dataDeletionControllerApi.deleteCompanyAssociatedData(reportMetaInfo.dataId)

            val exception =
                assertThrows<ClientException> {
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
            QaReportTestDataProvider(
                EuTaxonomyNonFinancialsQaReport::class.java,
                "eutaxonomy-non-financials",
            )
        val euTaxonomyNonFinancialsQaReportWithOneCorrection =
            qaReportTestDataProvider
                .getTData(1)
                .first()

        val euTaxonomyNonFinancialsQaReportMetaInfo =
            postQaReportForNewDataId(
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

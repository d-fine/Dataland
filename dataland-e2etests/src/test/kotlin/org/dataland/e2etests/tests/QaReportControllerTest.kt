package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandqaservice.openApiClient.model.QaReportStatusPatch
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentManagerAccessor
import org.dataland.e2etests.utils.QaApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QaReportControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentManagerAccessor()
    private val qaApiAccessor = QaApiAccessor()
    private lateinit var dummyCompanyAssociatedData: CompanyAssociatedDataSfdrData
    private val dataReviewerUserId = TechnicalUser.Reviewer.technicalUserId

    @BeforeAll
    fun postCompany() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
        val testCompanyInformation = apiAccessor.testDataProviderForSfdrData
            .getCompanyInformationWithoutIdentifiers(1).first()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val storedCompanyInfos = apiAccessor.companyDataControllerApi.postCompany(testCompanyInformation)
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val testDataSfdr = apiAccessor.testDataProviderForSfdrData
            .getTData(1).first()
        dummyCompanyAssociatedData =
            CompanyAssociatedDataSfdrData(
                storedCompanyInfos.companyId,
                "",
                testDataSfdr,
            )
    }

    @Test
    fun `post a QA report and check that the report can be retrieved`() {
        val dataId = postSfdrDatasetAndRetrieveDataId()
        val sfdrData = qaApiAccessor.createFullQaSfdrData()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reviewer)
        val qaReportMetaInfo = qaApiAccessor.sfdrQaReportControllerApi.postQaReport(dataId, sfdrData)

        val retrievedReport = qaApiAccessor.sfdrQaReportControllerApi.getQaReport(
            dataId = dataId,
            qaReportId = qaReportMetaInfo.qaReportId,
        )

        assertEquals(retrievedReport.metaInfo.reporterUserId, dataReviewerUserId)
        assertEquals(retrievedReport.metaInfo.dataId, dataId)
        assertEquals(retrievedReport.metaInfo.dataType, "sfdr")
        assertEquals(retrievedReport.report, sfdrData, "the uploaded and retrieved reports do not match")
    }

    @Test
    fun `post a QA report and change its status`() {
        val dataId = postSfdrDatasetAndRetrieveDataId()
        val sfdrData = qaApiAccessor.createQaSfdrDataWithOneFullQaDataPoint()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reviewer)
        val qaReportMetaInfo = qaApiAccessor.sfdrQaReportControllerApi.postQaReport(dataId, sfdrData)

        assertDoesNotThrow {
            qaApiAccessor.sfdrQaReportControllerApi.setQaReportStatus(
                dataId = dataId,
                qaReportId = qaReportMetaInfo.qaReportId,
                qaReportStatusPatch = QaReportStatusPatch(false),
            )
        }

        val newReportStatus = qaApiAccessor.sfdrQaReportControllerApi.getAllQaReportsForDataset(
            dataId = dataId,
            showInactive = true,
        ).first().metaInfo.active

        assertEquals(newReportStatus, false)
    }

    private fun postSfdrDatasetAndRetrieveDataId(): String {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        return apiAccessor.dataControllerApiForSfdrData.postCompanyAssociatedSfdrData(
            dummyCompanyAssociatedData, true,
        ).dataId
    }
}

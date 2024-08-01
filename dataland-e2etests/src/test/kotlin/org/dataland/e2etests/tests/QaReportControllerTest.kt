package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentManagerAccessor
import org.dataland.e2etests.utils.QaApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
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

        assertNotNull(qaReportMetaInfo)
        assertNotNull(qaReportMetaInfo.qaReportId)
        assertEquals(dataId, qaReportMetaInfo.dataId)
        assertEquals(dataReviewerUserId, qaReportMetaInfo.reporterUserId)
    }

    private fun postSfdrDatasetAndRetrieveDataId(): String {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        return apiAccessor.dataControllerApiForSfdrData.postCompanyAssociatedSfdrData(
            dummyCompanyAssociatedData, true,
        ).dataId
    }
}

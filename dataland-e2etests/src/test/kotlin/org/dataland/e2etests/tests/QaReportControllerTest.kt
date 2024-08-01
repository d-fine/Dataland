package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandqaservice.openApiClient.infrastructure.ClientError
import org.dataland.datalandqaservice.openApiClient.infrastructure.ClientException
import org.dataland.datalandqaservice.openApiClient.model.QaReportStatusPatch
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentManagerAccessor
import org.dataland.e2etests.utils.QaApiAccessor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
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
    fun `post a QA report and make sure its status can be changed`() {
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
        assertEquals(
            qaApiAccessor.sfdrQaReportControllerApi.getAllQaReportsForDataset(
                dataId = dataId,
                showInactive = true,
            ).first().metaInfo.active,
            false,
        )
    }

    @Test
    fun `make sure changing the status of a report as a reviewer other than the reporter throws an exception`() {
        val dataId = postSfdrDatasetAndRetrieveDataId()
        val sfdrData = qaApiAccessor.createQaSfdrDataWithOneFullQaDataPoint()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val qaReportMetaInfo = qaApiAccessor.sfdrQaReportControllerApi.postQaReport(dataId, sfdrData)

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reviewer)
        val exception = assertThrows<ClientException> {
            qaApiAccessor.sfdrQaReportControllerApi.setQaReportStatus(
                dataId = dataId,
                qaReportId = qaReportMetaInfo.qaReportId,
                qaReportStatusPatch = QaReportStatusPatch(false),
            )
        }
        val responseBody = (exception.response as ClientError<*>).body as String
        Assertions.assertTrue(
            responseBody.contains(
                "You do not have the required access rights to update QA report with the id:" +
                    " ${qaReportMetaInfo.qaReportId}",
            ),
        )
    }

    @Test
    fun `check that using invalid ids on the get endpoint produces the expected exception messages`() {
        val dataId1 = postSfdrDatasetAndRetrieveDataId()
        val dataId2 = postSfdrDatasetAndRetrieveDataId()
        val sfdrData = qaApiAccessor.createQaSfdrDataWithOneFullQaDataPoint()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reviewer)
        val qaReportMetaInfo = qaApiAccessor.sfdrQaReportControllerApi.postQaReport(dataId1, sfdrData)
        val qaReportId = qaReportMetaInfo.qaReportId
        val falseQaReportId = UUID.randomUUID().toString()
        val exception1 = assertThrows<ClientException> {
            qaApiAccessor.sfdrQaReportControllerApi.getQaReport(dataId1, falseQaReportId)
        }
        val responseBody1 = (exception1.response as ClientError<*>).body as String
        Assertions.assertTrue(
            responseBody1.contains(
                "No QA report with the id: $falseQaReportId could be found",
            ),
        )

        val exception2 = assertThrows<ClientException> {
            qaApiAccessor.sfdrQaReportControllerApi.getQaReport(dataId2, qaReportId)
        }
        val responseBody2 = (exception2.response as ClientError<*>).body as String
        Assertions.assertTrue(
            responseBody2.contains(
                "The requested Qa Report '$qaReportId' is not associated with data '$dataId2'," +
                    " but with data '$dataId1'.",
            ),
        )
    }

    @Test
    fun `try posting a QA report with an incorrect data type and data id and assert an exception is thrown`() {
        postSfdrDatasetAndRetrieveDataId()
        /* ------> can be readded after we have eu taxo data <---------


        val NonFinancialData = qaApiAccessor.createFullQaNonFinancialData()

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reviewer)
        val exception1 = assertThrows<InvalidInputApiException> {
            qaApiAccessor.sfdrQaReportControllerApi.postQaReport(dataId, NonFinancialData)
        }
        val responseBody1 = (exception1.summary as ClientError<*>).body as String
        Assertions.assertTrue(
            responseBody1.contains(
                "Data type mismatch",
            ),
        )
        */

        val sfdrData = qaApiAccessor.createQaSfdrDataWithOneFullQaDataPoint()
        val falseDataId = UUID.randomUUID().toString()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reviewer)
        val exception2 = assertThrows<ClientException> {
            qaApiAccessor.sfdrQaReportControllerApi.postQaReport(falseDataId, sfdrData)
        }
        val responseBody2 = (exception2.response as ClientError<*>).body as String
        Assertions.assertTrue(
            responseBody2.contains(
                "No data set with the id: $falseDataId could be found.",
            ),
        )
    }

    private fun postSfdrDatasetAndRetrieveDataId(): String {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        return apiAccessor.dataControllerApiForSfdrData.postCompanyAssociatedSfdrData(
            dummyCompanyAssociatedData, true,
        ).dataId
    }
}

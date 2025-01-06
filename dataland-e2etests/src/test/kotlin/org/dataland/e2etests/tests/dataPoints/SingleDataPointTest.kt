package org.dataland.e2etests.tests.dataPoints

import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.datalandbackend.openApiClient.model.DataPointMetaInformation
import org.dataland.datalandbackend.openApiClient.model.UploadedDataPoint
import org.dataland.e2etests.auth.GlobalAuth.withTechnicalUser
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.ExceptionUtils.assertAccessDeniedWrapper
import org.dataland.e2etests.utils.api.Backend
import org.dataland.e2etests.utils.api.CommunityManager
import org.dataland.e2etests.utils.api.QaService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID
import org.dataland.datalandbackend.openApiClient.model.QaStatus as QaStatusBackend
import org.dataland.datalandqaservice.openApiClient.model.QaStatus as QaStatusQaService

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SingleDataPointTest {
    private val apiAccessor = ApiAccessor()
    private val dummyDatapointContent =
        """
        {"value": 0.5, "currency": "USD"}
        """.trimIndent()
    private val dummyDataPointIdentifier = "extendedCurrencyEquity"
    private val listOfOneCompanyInformation = apiAccessor.testDataProviderForSfdrData.getCompanyInformationWithoutIdentifiers(1)

    private fun uploadDummyDatapoint(
        companyId: String,
        bypassQa: Boolean,
    ): DataPointMetaInformation {
        val uploadedDataPoint =
            UploadedDataPoint(
                dataPointContent = dummyDatapointContent,
                dataPointIdentifier = dummyDataPointIdentifier,
                companyId = companyId,
                reportingPeriod = "2022",
            )
        val datapointMetaInfo = Backend.dataPointControllerApi.postDataPoint(uploadedDataPoint, bypassQa)
        Thread.sleep(1000)
        return datapointMetaInfo
    }

    private fun createDummyCompany(): String = Backend.companyDataControllerApi.postCompany(listOfOneCompanyInformation.first()).companyId

    @Test
    fun `ensure a data point can be uploaded and downloaded without inconsistencies`() {
        withTechnicalUser(TechnicalUser.Admin) {
            val companyId = createDummyCompany()
            val dataPointId = uploadDummyDatapoint(companyId, bypassQa = false).dataId
            val downloadedDataPoint = Backend.dataPointControllerApi.getDataPoint(dataPointId)
            assertEquals(dummyDatapointContent, downloadedDataPoint.dataPointContent)
        }
    }

    @Test
    fun `ensure the data point bypass qa works as expected`() {
        val dataPointId =
            withTechnicalUser(TechnicalUser.Admin) {
                val companyId = createDummyCompany()
                uploadDummyDatapoint(companyId, bypassQa = true).dataId
            }
        withTechnicalUser(TechnicalUser.Reader) {
            val datapointMetaInformation = Backend.dataPointControllerApi.getDataPointMetaInfo(dataPointId)
            assertEquals(QaStatusBackend.Accepted, datapointMetaInformation.qaStatus)
            assertEquals(true, datapointMetaInformation.currentlyActive)
            assertDoesNotThrow { Backend.dataPointControllerApi.getDataPoint(dataPointId) }
        }
    }

    @Test
    fun `ensure a datapoint in qa can be downloaded by authorized users`() {
        val dataPointId =
            withTechnicalUser(TechnicalUser.Admin) {
                val companyId = createDummyCompany()
                withTechnicalUser(TechnicalUser.Uploader) {
                    uploadDummyDatapoint(companyId, bypassQa = false).dataId
                }
            }
        withTechnicalUser(TechnicalUser.Reader) {
            assertAccessDeniedWrapper { Backend.dataPointControllerApi.getDataPoint(dataPointId) }
        }
        val allowedUsers = listOf(TechnicalUser.Admin, TechnicalUser.Uploader)
        allowedUsers.forEach { user ->
            withTechnicalUser(user) {
                val downloadedDataPoint = Backend.dataPointControllerApi.getDataPoint(dataPointId)
                assertEquals(dummyDatapointContent, downloadedDataPoint.dataPointContent)
            }
        }
    }

    @Test
    fun `ensure reader can upload data for companies iff assigned correct company roles`() {
        val dummyCompanyId =
            withTechnicalUser(TechnicalUser.Admin) {
                createDummyCompany()
            }
        val rolesThatCanUploadPublicData = listOf(CompanyRole.CompanyOwner, CompanyRole.DataUploader)
        withTechnicalUser(TechnicalUser.Reader) {
            assertAccessDeniedWrapper { uploadDummyDatapoint(dummyCompanyId, bypassQa = false) }
        }

        for (role in CompanyRole.entries) {
            withTechnicalUser(TechnicalUser.Admin) {
                CommunityManager.companyRolesControllerApi.assignCompanyRole(
                    role = role,
                    userId = UUID.fromString(TechnicalUser.Reader.technicalUserId),
                    companyId = UUID.fromString(dummyCompanyId),
                )
            }
            withTechnicalUser(TechnicalUser.Reader) {
                if (rolesThatCanUploadPublicData.contains(role)) {
                    assertDoesNotThrow { uploadDummyDatapoint(dummyCompanyId, bypassQa = false) }
                } else {
                    assertAccessDeniedWrapper { uploadDummyDatapoint(dummyCompanyId, bypassQa = false) }
                }
            }
        }
    }

    @Test
    fun `ensure a data point can be uploaded by an uploader then QA approved by a reviewer and afterwards retrieved by a reader`() {
        var dataPointId = ""
        withTechnicalUser(TechnicalUser.Admin) {
            val companyId = createDummyCompany()
            dataPointId = uploadDummyDatapoint(companyId, bypassQa = false).dataId
            val datapointMetaInformation = Backend.dataPointControllerApi.getDataPointMetaInfo(dataPointId)
            assertEquals(datapointMetaInformation.qaStatus, QaStatusBackend.Pending)
        }

        withTechnicalUser(TechnicalUser.Reader) {
            assertAccessDeniedWrapper { Backend.dataPointControllerApi.getDataPoint(dataPointId) }
        }

        withTechnicalUser(TechnicalUser.Reviewer) {
            val reviewQueueItem = QaService.QaControllerApi.getDataPointReviewQueue().firstOrNull { it.dataId == dataPointId }
            assert(reviewQueueItem != null) { "Data point not found in review queue" }
            assert(reviewQueueItem!!.dataPointIdentifier == dummyDataPointIdentifier)
            assert(reviewQueueItem.qaStatus == QaStatusQaService.Pending)
            QaService.QaControllerApi.changeDataPointQaStatus(dataPointId, QaStatusQaService.Accepted)
        }

        withTechnicalUser(TechnicalUser.Reader) {
            val dataPointInstance = Backend.dataPointControllerApi.getDataPoint(dataPointId)
            val datapointMetaInformation = Backend.dataPointControllerApi.getDataPointMetaInfo(dataPointId)
            assertEquals(dummyDatapointContent, dataPointInstance.dataPointContent)
            assertEquals(datapointMetaInformation.qaStatus, QaStatusBackend.Accepted)
        }
    }
}

package org.dataland.e2etests.tests.dataPoints

import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.datalandbackend.openApiClient.model.DataPointMetaInformation
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackend.openApiClient.model.UploadedDataPoint
import org.dataland.e2etests.auth.GlobalAuth.withTechnicalUser
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.ExceptionUtils.assertAccessDeniedWrapper
import org.dataland.e2etests.utils.api.Backend
import org.dataland.e2etests.utils.api.CommunityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SingleDataPoint {
    private val apiAccessor = ApiAccessor()
    private val dummyDatapointContent =
        """
        {"value": 0.5, "currency": "USD"}
        """.trimIndent()
    private val listOfOneCompanyInformation = apiAccessor.testDataProviderForSfdrData.getCompanyInformationWithoutIdentifiers(1)

    private fun uploadDummyDatapoint(
        companyId: String,
        bypassQa: Boolean,
    ): DataPointMetaInformation {
        val uploadedDataPoint =
            UploadedDataPoint(
                dataPointContent = dummyDatapointContent,
                dataPointIdentifier = "extendedCurrencyEquity",
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
            assertEquals(QaStatus.Accepted, datapointMetaInformation.qaStatus)
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
}

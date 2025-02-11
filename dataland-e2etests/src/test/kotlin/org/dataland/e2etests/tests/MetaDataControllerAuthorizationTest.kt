package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.MetaDataUtils
import org.dataland.e2etests.utils.UploadConfiguration
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

class MetaDataControllerAuthorizationTest {
    private val apiAccessor = ApiAccessor()

    private val listOfOneTeaserTestCompanyInformation =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getCompanyInformationWithoutIdentifiers(1).map {
            it.copy(isTeaserCompany = true)
        }
    private val listOfOneNonTeaserTestCompanyInformation =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getCompanyInformationWithoutIdentifiers(1).map {
            it.copy(isTeaserCompany = false)
        }

    @Test
    fun `post a dummy teaser company and data for it and confirm unauthorized meta info access succeeds`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusFinancials
        val listOfUploadInfo =
            apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
                mapOf(testDataType to listOfOneTeaserTestCompanyInformation), 1,
            )
        val testDataId = listOfUploadInfo[0].actualStoredDataMetaInfo!!.dataId
        val dataMetaInformation = apiAccessor.unauthorizedMetaDataControllerApi.getDataMetaInfo(testDataId)
        MetaDataUtils.assertDataMetaInfoMatches(
            actualDataMetaInfo = dataMetaInformation,
            expectedDataMetaInfo =
                MetaDataUtils.buildAcceptedAndActiveDataMetaInformation(
                    dataId = testDataId,
                    companyId = listOfUploadInfo[0].actualStoredCompany.companyId,
                    testDataType = testDataType,
                    user = TechnicalUser.Admin,
                ),
        )
    }

    @Test
    fun `post a dummy company and taxonomy data for it and confirm unauthorized meta info access is denied`() {
        val listOfUploadInfo =
            apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
                mapOf(DataTypeEnum.eutaxonomyMinusFinancials to listOfOneNonTeaserTestCompanyInformation), 1,
            )
        val testDataId = listOfUploadInfo[0].actualStoredDataMetaInfo!!.dataId
        val exception =
            assertThrows<IllegalArgumentException> {
                apiAccessor.unauthorizedMetaDataControllerApi.getDataMetaInfo(testDataId)
            }
        Assertions.assertTrue(exception.message!!.contains("Unauthorized access failed"))
    }

    @Test
    fun `post a dummy company as teaser company and data for it and confirm unauthorized meta info search succeeds`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusFinancials
        val listOfUploadInfo =
            apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
                mapOf(testDataType to listOfOneTeaserTestCompanyInformation), 1,
            )
        val testDataId = listOfUploadInfo[0].actualStoredDataMetaInfo!!.dataId
        val testCompanyId = listOfUploadInfo[0].actualStoredCompany.companyId
        val expectedMetaInformation =
            MetaDataUtils.buildAcceptedAndActiveDataMetaInformation(
                dataId = testDataId,
                companyId = testCompanyId,
                testDataType = testDataType,
                user = TechnicalUser.Admin,
            )
        val actualMetaInformation =
            apiAccessor.unauthorizedMetaDataControllerApi
                .getListOfDataMetaInfo(
                    companyId = testCompanyId,
                    dataType = testDataType,
                ).filter { it.dataId == testDataId }
        MetaDataUtils.assertDataMetaInfoMatches(
            actualDataMetaInfo = actualMetaInformation.first(),
            expectedDataMetaInfo = expectedMetaInformation,
        )
    }

    @Test
    fun `post a dummy company and taxonomy data for it and confirm unauthorized meta info search is denied`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusFinancials
        val testCompanyId =
            apiAccessor
                .uploadCompanyAndFrameworkDataForMultipleFrameworks(
                    mapOf(testDataType to listOfOneNonTeaserTestCompanyInformation), 1,
                )[0]
                .actualStoredCompany.companyId
        val exception =
            assertThrows<IllegalArgumentException> {
                apiAccessor.unauthorizedMetaDataControllerApi.getListOfDataMetaInfo(testCompanyId, testDataType)
            }
        Assertions.assertTrue(exception.message!!.contains("Unauthorized access failed"))
    }

    @Test
    fun `post two companies with data and check that the access to the uploaderUserId field is not restricted`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusFinancials
        val metaInfoOfUploaderUpload =
            apiAccessor
                .uploadCompanyAndFrameworkDataForMultipleFrameworks(
                    companyInformationPerFramework = mapOf(testDataType to listOfOneNonTeaserTestCompanyInformation),
                    numberOfDatasetsPerCompany = 1,
                    uploadConfig = UploadConfiguration(TechnicalUser.Uploader, false),
                    ensureQaPassed = false,
                )[0]
                .actualStoredDataMetaInfo!!
        val metaInfoOfAdminUpload =
            apiAccessor
                .uploadCompanyAndFrameworkDataForMultipleFrameworks(
                    mapOf(testDataType to listOfOneNonTeaserTestCompanyInformation),
                    1,
                    UploadConfiguration(TechnicalUser.Admin),
                )[0]
                .actualStoredDataMetaInfo!!

        expectUserIdToBe(metaInfoOfAdminUpload, TechnicalUser.Reader, TechnicalUser.Admin.technicalUserId)
        expectUserIdToBe(metaInfoOfUploaderUpload, TechnicalUser.Uploader, TechnicalUser.Uploader.technicalUserId)
        expectUserIdToBe(metaInfoOfAdminUpload, TechnicalUser.Uploader, TechnicalUser.Admin.technicalUserId)
        expectUserIdToBe(metaInfoOfUploaderUpload, TechnicalUser.Admin, TechnicalUser.Uploader.technicalUserId)
        expectUserIdToBe(metaInfoOfAdminUpload, TechnicalUser.Admin, TechnicalUser.Admin.technicalUserId)
    }

    private fun expectUserIdToBe(
        dataMetaInformation: DataMetaInformation,
        requestingTechnicalUser: TechnicalUser,
        expectedUploaderId: String?,
    ) {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(requestingTechnicalUser)

        val uploaderUserIdFromMetaInfo =
            apiAccessor.metaDataControllerApi
                .getDataMetaInfo(dataMetaInformation.dataId)
                .uploaderUserId
        val msg =
            "Technical user $requestingTechnicalUser saw user ID $uploaderUserIdFromMetaInfo but expected was " +
                "$expectedUploaderId"
        Assertions.assertEquals(expectedUploaderId, uploaderUserIdFromMetaInfo, msg)

        val uploaderUserIdFromCompanyInfo =
            apiAccessor.companyDataControllerApi
                .getCompanyById(dataMetaInformation.companyId)
                .dataRegisteredByDataland
                .firstOrNull()
                ?.uploaderUserId
        Assertions.assertEquals(uploaderUserIdFromCompanyInfo, uploaderUserIdFromMetaInfo, msg)
    }
}

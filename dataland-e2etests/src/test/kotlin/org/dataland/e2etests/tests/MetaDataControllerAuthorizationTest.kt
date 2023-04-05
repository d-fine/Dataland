package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException
import java.time.Instant

class MetaDataControllerAuthorizationTest {

    private val apiAccessor = ApiAccessor()
    private val metaDataControllerTest = MetaDataControllerTest()

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
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(testDataType to listOfOneTeaserTestCompanyInformation), 1,
        )
        val testDataId = listOfUploadInfo[0].actualStoredDataMetaInfo!!.dataId
        val dataMetaInformation = apiAccessor.unauthorizedMetaDataControllerApi.getDataMetaInfo(testDataId)
        val uploadTime = Instant.now().toEpochMilli()
        Assertions.assertEquals(
            metaDataControllerTest.buildAcceptedAndActiveDataMetaInformation(
                testDataId,
                listOfUploadInfo[0].actualStoredCompany.companyId,
                testDataType,
                uploadTime,
            ),
            dataMetaInformation.copy(uploadTime = uploadTime),
            "The meta info of the posted eu taxonomy data does not match the retrieved meta infozz.",
        )
    }

    @Test
    fun `post a dummy company and taxonomy data for it and confirm unauthorized meta info access is denied`() {
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(DataTypeEnum.eutaxonomyMinusFinancials to listOfOneNonTeaserTestCompanyInformation), 1,
        )
        val testDataId = listOfUploadInfo[0].actualStoredDataMetaInfo!!.dataId
        val exception = assertThrows<IllegalArgumentException> {
            apiAccessor.unauthorizedMetaDataControllerApi.getDataMetaInfo(testDataId)
        }
        Assertions.assertTrue(exception.message!!.contains("Unauthorized access failed"))
    }

    @Test
    fun `post a dummy company as teaser company and data for it and confirm unauthorized meta info search succeeds`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusFinancials
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(testDataType to listOfOneTeaserTestCompanyInformation), 1,
        )
        val testDataId = listOfUploadInfo[0].actualStoredDataMetaInfo!!.dataId
        val testCompanyId = listOfUploadInfo[0].actualStoredCompany.companyId
        val uploadTime = Instant.now().toEpochMilli()
        val expectedMetaInformation = metaDataControllerTest.buildAcceptedAndActiveDataMetaInformation(
            dataId = testDataId, companyId = testCompanyId,
            testDataType = testDataType,
            uploadTime = uploadTime,
        )
        Assertions.assertTrue(
            apiAccessor.unauthorizedMetaDataControllerApi.getListOfDataMetaInfo(testCompanyId, testDataType)
                .map { it.copy(uploadTime = uploadTime) }.contains(expectedMetaInformation),
            "The meta info of the posted eu taxonomy data that was associated with the teaser company " +
                "does not match the retrieved meta info.",
        )
    }

    @Test
    fun `post a dummy company and taxonomy data for it and confirm unauthorized meta info search is denied`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusFinancials
        val testCompanyId = apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(testDataType to listOfOneNonTeaserTestCompanyInformation), 1,
        )[0].actualStoredCompany.companyId
        val exception = assertThrows<IllegalArgumentException> {
            apiAccessor.unauthorizedMetaDataControllerApi.getListOfDataMetaInfo(testCompanyId, testDataType)
        }
        Assertions.assertTrue(exception.message!!.contains("Unauthorized access failed"))
    }

    @Test
    fun `post two companies with data and check that the access to the uploaderUserId field is restricted`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusFinancials
        val metaInfoOfUploaderUpload = apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(testDataType to listOfOneNonTeaserTestCompanyInformation), 1, TechnicalUser.Uploader,
        )[0].actualStoredDataMetaInfo!!
        val metaInfoOfAdminUpload = apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(testDataType to listOfOneNonTeaserTestCompanyInformation), 1, TechnicalUser.Admin,
        )[0].actualStoredDataMetaInfo!!

        validateReaderAccessToUserId(metaInfoOfUploaderUpload, metaInfoOfAdminUpload)
        validateUploaderAccessToUserId(metaInfoOfUploaderUpload, metaInfoOfAdminUpload)
        validateAdminAccessToUserId(
            metaInfoOfUploaderUpload,
            metaInfoOfAdminUpload,
        )
    }

    private fun expectUserIdToBe(
        dataMetaInformation: DataMetaInformation,
        requestingTechnicalUser: TechnicalUser,
        expectedUploaderId: String?,
        msg: String,
    ) {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(requestingTechnicalUser)

        val uploaderUserIdFromMetaInfo = apiAccessor.metaDataControllerApi.getDataMetaInfo(dataMetaInformation.dataId)
            .uploaderUserId
        Assertions.assertEquals(expectedUploaderId, uploaderUserIdFromMetaInfo, msg)

        val uploaderUserIdFromCompanyInfo = apiAccessor.companyDataControllerApi
            .getCompanyById(dataMetaInformation.companyId)
            .dataRegisteredByDataland.firstOrNull()?.uploaderUserId
        Assertions.assertEquals(uploaderUserIdFromCompanyInfo, uploaderUserIdFromMetaInfo, msg)
    }

    private fun validateAdminAccessToUserId(
        testUploadDataUploaderMetaInfo: DataMetaInformation,
        testUploadDataAdminMetaInfo: DataMetaInformation,
    ) {
        expectUserIdToBe(
            testUploadDataUploaderMetaInfo, TechnicalUser.Admin, TechnicalUser.Uploader.technicalUserId,
            "Admins should be able to view uploaderUserids for all users",
        )
        expectUserIdToBe(
            testUploadDataAdminMetaInfo, TechnicalUser.Admin, TechnicalUser.Admin.technicalUserId,
            "Admins should be able to view uploaderUserids for all users",
        )
    }

    private fun validateUploaderAccessToUserId(
        testUploadDataUploaderMetaInfo: DataMetaInformation,
        testUploadDataAdminMetaInfo: DataMetaInformation,
    ) {
        expectUserIdToBe(
            testUploadDataUploaderMetaInfo, TechnicalUser.Uploader, TechnicalUser.Uploader.technicalUserId,
            "Expected user id to be present if the user requests data about an upload he performed himself",
        )
        expectUserIdToBe(
            testUploadDataAdminMetaInfo, TechnicalUser.Uploader, null,
            "Data Uploaders should not be able to view the user id of uploads of other users",
        )
    }

    private fun validateReaderAccessToUserId(
        testUploadDataUploaderMetaInfo: DataMetaInformation,
        testUploadDataAdminMetaInfo: DataMetaInformation,
    ) {
        expectUserIdToBe(
            testUploadDataUploaderMetaInfo, TechnicalUser.Reader, null,
            "A reader should not see any uploader ids",
        )
        expectUserIdToBe(
            testUploadDataAdminMetaInfo, TechnicalUser.Reader, null,
            "A reader should not see any uploader ids",
        )
    }
}

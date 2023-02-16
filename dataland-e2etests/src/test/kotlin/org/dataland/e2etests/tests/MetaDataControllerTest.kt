package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.DatasetQualityStatus
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException
import java.time.Instant
import kotlin.math.abs

class MetaDataControllerTest {

    private val apiAccessor = ApiAccessor()

    private val numberOfCompaniesToPostPerFramework = 4
    private val numberOfDataSetsToPostPerCompany = 5
    private val totalNumberOfDataSetsPerFramework =
        numberOfCompaniesToPostPerFramework * numberOfDataSetsToPostPerCompany

    private val listOfTestCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getCompanyInformationWithoutIdentifiers(numberOfCompaniesToPostPerFramework)
    private val listOfOneTestCompanyInformation = listOf(listOfTestCompanyInformation[0])
    private val listOfOneNonTeaserTestCompanyInformation =
        listOf(listOfTestCompanyInformation[0].copy(isTeaserCompany = false))
    private val listOfOneTeaserTestCompanyInformation =
        listOf(listOfTestCompanyInformation[0].copy(isTeaserCompany = true))

    @Test
    fun `post dummy company and taxonomy data for it and check if meta info about that data can be retrieved`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials
        val uploadedMetaInfo = apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(testDataType to listOfOneTestCompanyInformation), 1,
        )[0].actualStoredDataMetaInfo!!
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val actualDataMetaInformation = apiAccessor.metaDataControllerApi.getDataMetaInfo(uploadedMetaInfo.dataId)
        val expectedDataMetaInformation = buildDataMetaInformation(uploadedMetaInfo, testDataType)
        assertEquals(
            expectedDataMetaInformation,
            actualDataMetaInformation.copy(uploadTime = 0),
            "The meta info of the posted eu taxonomy data does not match the retrieved meta info.",
        )

        val timeDiffFromUploadToNow = actualDataMetaInformation.uploadTime - Instant.now().epochSecond
        assertTrue(
            abs(timeDiffFromUploadToNow) < 60,
            "The server-upload-time and the local upload time differ too much.",
        )
    }

    private fun buildDataMetaInformation(
        uploadedMetaInfo: DataMetaInformation,
        testDataType: DataTypeEnum,
    ) = DataMetaInformation(
        uploadedMetaInfo.dataId, testDataType, 0,
        uploadedMetaInfo.companyId, DatasetQualityStatus.accepted,
    )

    @Test
    fun `search for a company that does not exist and check that a 404 error is returned`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val clientException = assertThrows<ClientException> {
            apiAccessor.companyDataControllerApi.getCompanyById("this-should-not-exist")
        }
        assertEquals(clientException.statusCode, 404)
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search with empty filters`() {
        val initialSizeOfDataMetaInfo = apiAccessor.getNumberOfDataMetaInfo()
        apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(DataTypeEnum.eutaxonomyMinusNonMinusFinancials to listOfTestCompanyInformation),
            numberOfDataSetsToPostPerCompany,
        )
        val sizeOfListOfDataMetaInfo = apiAccessor.getNumberOfDataMetaInfo()
        val expectedSizeOfDataMetaInfo = initialSizeOfDataMetaInfo + totalNumberOfDataSetsPerFramework
        assertEquals(
            expectedSizeOfDataMetaInfo, sizeOfListOfDataMetaInfo,
            "The list with all data meta info is expected to increase by $totalNumberOfDataSetsPerFramework to " +
                "$expectedSizeOfDataMetaInfo, but has the size $sizeOfListOfDataMetaInfo.",
        )
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search with filter on company ID`() {
        val companyIdOfFirstUploadedCompany = apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(DataTypeEnum.eutaxonomyMinusNonMinusFinancials to listOfTestCompanyInformation),
            numberOfDataSetsToPostPerCompany,
        )[0].actualStoredCompany.companyId
        val listOfDataMetaInfoForFirstCompanyId =
            apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(companyIdOfFirstUploadedCompany)
        assertEquals(
            numberOfDataSetsToPostPerCompany, listOfDataMetaInfoForFirstCompanyId.size,
            "The first posted company is expected to have meta info about $numberOfDataSetsToPostPerCompany " +
                "data sets, but has meta info about ${listOfDataMetaInfoForFirstCompanyId.size} data sets.",
        )
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search with filter on data type`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusFinancials
        val initListSizeDataMetaInfoForEuTaxoFinancials = apiAccessor.getNumberOfDataMetaInfo(dataType = testDataType)
        apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(
                testDataType to listOfTestCompanyInformation,
                DataTypeEnum.eutaxonomyMinusNonMinusFinancials to listOfTestCompanyInformation,
            ),
            numberOfDataSetsToPostPerCompany,
        )
        val listSizeDataMetaInfoForEuTaxoFinancials = apiAccessor.getNumberOfDataMetaInfo(dataType = testDataType)
        val expectedListSizeDataMetaInfoForEuTaxoFinancials = initListSizeDataMetaInfoForEuTaxoFinancials +
            totalNumberOfDataSetsPerFramework
        assertEquals(
            expectedListSizeDataMetaInfoForEuTaxoFinancials, listSizeDataMetaInfoForEuTaxoFinancials,
            "The meta info list for all EU Taxonomy Data for Non-Financials is expected to increase by " +
                "$totalNumberOfDataSetsPerFramework to $expectedListSizeDataMetaInfoForEuTaxoFinancials, " +
                "but has the size $listSizeDataMetaInfoForEuTaxoFinancials.",
        )
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search with filters on company ID and data type`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(testDataType to listOfTestCompanyInformation), numberOfDataSetsToPostPerCompany,
        )
        val sizeOfListOfDataMetaInfoPerCompanyIdAndDataType = apiAccessor.getNumberOfDataMetaInfo(
            listOfUploadInfo[0].actualStoredCompany.companyId,
            testDataType,
        )
        assertEquals(
            numberOfDataSetsToPostPerCompany, sizeOfListOfDataMetaInfoPerCompanyIdAndDataType,
            "The first posted company is expected to have meta info about $numberOfDataSetsToPostPerCompany " +
                "data sets, but has meta info about $sizeOfListOfDataMetaInfoPerCompanyIdAndDataType data sets.",
        )
    }

    @Test
    fun `post a dummy teaser company and data for it and confirm unauthorized meta info access succeeds`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusFinancials
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(testDataType to listOfOneTeaserTestCompanyInformation), 1,
        )
        val testDataId = listOfUploadInfo[0].actualStoredDataMetaInfo!!.dataId
        val dataMetaInformation = apiAccessor.unauthorizedMetaDataControllerApi.getDataMetaInfo(testDataId)
        assertEquals(
            DataMetaInformation(
                testDataId, testDataType, 0,
                listOfUploadInfo[0].actualStoredCompany.companyId, DatasetQualityStatus.accepted,
            ),
            dataMetaInformation.copy(uploadTime = 0),
            "The meta info of the posted eu taxonomy data does not match the retrieved meta info.",
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
        assertTrue(exception.message!!.contains("Unauthorized access failed"))
    }

    @Test
    fun `post a dummy company as teaser company and data for it and confirm unauthorized meta info search succeeds`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusFinancials

        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(testDataType to listOfOneTeaserTestCompanyInformation), 1,
        )
        val testDataId = listOfUploadInfo[0].actualStoredDataMetaInfo!!.dataId
        val testCompanyId = listOfUploadInfo[0].actualStoredCompany.companyId
        val expectedMetaInformation = DataMetaInformation(
            testDataId, testDataType, 0,
            testCompanyId, DatasetQualityStatus.accepted,
        )
        assertTrue(
            apiAccessor.unauthorizedMetaDataControllerApi.getListOfDataMetaInfo(testCompanyId, testDataType)
                .map { it.copy(uploadTime = 0) }
                .contains(expectedMetaInformation),
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
        assertTrue(exception.message!!.contains("Unauthorized access failed"))
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

    private fun expectUserIdToBe(
        dataMetaInformation: DataMetaInformation,
        requestingTechnicalUser: TechnicalUser,
        expectedUploaderId: String?,
        msg: String,
    ) {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(requestingTechnicalUser)

        val uploaderUserIdFromMetaInfo = apiAccessor.metaDataControllerApi.getDataMetaInfo(dataMetaInformation.dataId)
            .uploaderUserId
        assertEquals(expectedUploaderId, uploaderUserIdFromMetaInfo, msg)

        val uploaderUserIdFromCompanyInfo = apiAccessor.companyDataControllerApi
            .getCompanyById(dataMetaInformation.companyId)
            .dataRegisteredByDataland.firstOrNull()?.uploaderUserId
        assertEquals(uploaderUserIdFromCompanyInfo, uploaderUserIdFromMetaInfo, msg)
    }
}

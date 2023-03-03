package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.openApiClient.model.QAStatus
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.UploadInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
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
        val uploadTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        val expectedDataMetaInformation =
            DataMetaInformation(
                uploadedMetaInfo.dataId,
                uploadedMetaInfo.companyId,
                testDataType, uploadTime,
                "",
                true,
                QAStatus.accepted,
                null,
            )
        assertEquals(
            expectedDataMetaInformation,
            actualDataMetaInformation.copy(uploadTime = uploadTime),
            "The meta info of the posted eu taxonomy data does not match the retrieved meta info.",
        )

        val timeDiffFromUploadToNow = actualDataMetaInformation.uploadTime - Instant.now().epochSecond
        assertTrue(
            abs(timeDiffFromUploadToNow) < 60,
            "The server-upload-time and the local upload time differ too much.",
        )
    }

    //TODO check if this function can be rather used than the hardcoded blocks
    private fun buildDataMetaInformation(
        uploadedMetaInfo: DataMetaInformation,
        testDataType: DataTypeEnum,
    ) = DataMetaInformation(
        uploadedMetaInfo.dataId, uploadedMetaInfo.companyId, testDataType, 0, "", false,
        QAStatus.accepted, null,
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
        val initialSizeOfDataMetaInfo = apiAccessor.getNumberOfDataMetaInfo(showOnlyActive = false)
        apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(DataTypeEnum.eutaxonomyMinusNonMinusFinancials to listOfTestCompanyInformation),
            numberOfDataSetsToPostPerCompany,
        )
        val sizeOfListOfDataMetaInfo = apiAccessor.getNumberOfDataMetaInfo(showOnlyActive = false)
        val expectedSizeOfDataMetaInfo = initialSizeOfDataMetaInfo + totalNumberOfDataSetsPerFramework
        assertEquals(
            expectedSizeOfDataMetaInfo, sizeOfListOfDataMetaInfo,
            "The list with all data meta info is expected to increase by $totalNumberOfDataSetsPerFramework to " +
                "$expectedSizeOfDataMetaInfo, but has the size $sizeOfListOfDataMetaInfo.",
        )
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search with filter on company ID`() {
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(DataTypeEnum.eutaxonomyMinusNonMinusFinancials to listOfTestCompanyInformation),
            numberOfDataSetsToPostPerCompany,
        )
        val companyIdOfFirstUploadedCompany = listOfUploadInfo[0].actualStoredCompany.companyId
        val listOfDataMetaInfoForFirstCompanyId =
            apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(
                companyIdOfFirstUploadedCompany, showOnlyActive = false,
            )
        assertEquals(
            numberOfDataSetsToPostPerCompany, listOfDataMetaInfoForFirstCompanyId.size,
            "The first posted company is expected to have meta info about $numberOfDataSetsToPostPerCompany " +
                "data sets, but has meta info about ${listOfDataMetaInfoForFirstCompanyId.size} data sets.",
        )
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search with filter on data type`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusFinancials
        val initListSizeDataMetaInfoForEuTaxoFinancials =
            apiAccessor.getNumberOfDataMetaInfo(dataType = testDataType, showOnlyActive = false)
        apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(
                testDataType to listOfTestCompanyInformation,
                DataTypeEnum.eutaxonomyMinusNonMinusFinancials to listOfTestCompanyInformation,
            ),
            numberOfDataSetsToPostPerCompany,
        )
        val listSizeDataMetaInfoForEuTaxoFinancials =
            apiAccessor.getNumberOfDataMetaInfo(dataType = testDataType, showOnlyActive = false)
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
            false,
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
        val uploadTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        assertEquals(
            DataMetaInformation(
                testDataId,
                listOfUploadInfo[0].actualStoredCompany.companyId,
                testDataType,
                uploadTime,
                "",
                true,
                QAStatus.accepted,
                null,
            ),
            dataMetaInformation.copy(uploadTime = uploadTime),
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
        val uploadTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        val expectedMetaInformation = DataMetaInformation(
            testDataId,
            testCompanyId,
            testDataType,
            uploadTime,
            "",
            true,
            QAStatus.accepted,
            null,
        )
        assertTrue(
            apiAccessor.unauthorizedMetaDataControllerApi.getListOfDataMetaInfo(testCompanyId, testDataType)
                .map { it.copy(uploadTime = uploadTime) }
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

    @Test
    fun `ensure that version history field in metadata endpoint of meta data controller works`() {
//        Get a data set of an arbitrary framework. Upload it multiple times changing ReportingPeriod, uploadTime and
//        a data point in between. Ensure that only the data set with the latest upload_time is returned or all
//        depending on showVersionHistory flag.
        val companyInformation =
            apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getCompanyInformationWithoutIdentifiers(1)[0]
        val frameWorkData = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getTData(1)[0]
        val reportingPeriod1 = "2022"
        val listOfUploadInfo2022first =
            firstUploadForVersionHistory(companyInformation, frameWorkData, reportingPeriod1)
        val companyId = listOfUploadInfo2022first.actualStoredCompany.companyId
//        Wait to ensure that uploadTime changes
        Thread.sleep(1000)
        subsequentUploadForVersionHistory(companyId, frameWorkData, reportingPeriod1)
        Thread.sleep(1000)
//        Override number of employees to identify the final uploaded dataset
        val finalFrameWorkData = frameWorkData.copy(numberOfEmployees = BigDecimal.valueOf(3))
        subsequentUploadForVersionHistory(companyId, finalFrameWorkData, reportingPeriod1)
        val dataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials
        val resultWithoutVersioning =
            apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(companyId, dataType, true, reportingPeriod1)
        val resultWithVersioning =
            apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(companyId, dataType, false, reportingPeriod1)

        val activeDataSet =
            apiAccessor.dataControllerApiForEuTaxonomyNonFinancials.getCompanyAssociatedEuTaxonomyDataForNonFinancials(
                resultWithoutVersioning[0].dataId,
            )

        assertEquals(
            3, resultWithVersioning.size,
            "Metadata of three versions of uploaded datasets should be available, instead its " +
                "${resultWithoutVersioning.size}.",
        )
        assertEquals(
            1,
            resultWithoutVersioning.size,
            "Metadata of a single, active version should be available. Instead its ${resultWithVersioning.size}",
        )
        assertTrue(
            (resultWithoutVersioning[0].uploadTime == resultWithVersioning.maxOfOrNull { it.uploadTime }),
            "The active result should be the one with the highest uploadTime but it isn't.",
        )
        assertTrue(
            (activeDataSet.data!!.numberOfEmployees == BigDecimal.valueOf(3)),
            "The active dataset should have been manipulated to have a numberOfEmployees of three but the " +
                "retrieved active data set does not.",
        )
    }

    @Test
    fun `ensure that reportingPeriod field of metadata endpoint of meta data controller works`() {
//        Upload multiple versions of a dataset under different reporting Periods. Ensure that only a single one is
//        active per reporting period and that the active one is the latest one uploaded for this reporting period
        val companyInformation =
            apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getCompanyInformationWithoutIdentifiers(1)[0]
        val frameWorkData = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getTData(1)[0]
        val reportingPeriod1 = "2022"
        val reportingPeriod2 = "2023"
        val firstUploadInfo = firstUploadForVersionHistory(companyInformation, frameWorkData, reportingPeriod2)
        val companyId = firstUploadInfo.actualStoredCompany.companyId
        Thread.sleep(1000)
        subsequentUploadForVersionHistory(companyId, frameWorkData, reportingPeriod1)
        Thread.sleep(1000)
        val final2022metadata = subsequentUploadForVersionHistory(companyId, frameWorkData, reportingPeriod1)
        Thread.sleep(1000)
        val final2023metadata = subsequentUploadForVersionHistory(companyId, frameWorkData, reportingPeriod2)
        val dataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials
        val result2022WithoutVersioning =
            apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(companyId, dataType, true, reportingPeriod1)
        val result2023WithoutVersioning =
            apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(companyId, dataType, true, reportingPeriod2)
        assertTrue(
            (
                result2023WithoutVersioning.size == 1 &&
                    result2022WithoutVersioning.size == 1 &&
                    result2023WithoutVersioning[0].dataId != result2022WithoutVersioning[0].dataId
                ),
            "Without versioning, metadata of only a single active dataset should be returned per reporting " +
                "period and they should point to different data sets. But this is not the case.",
        )
        assertTrue(
            (
                final2022metadata.dataId == result2022WithoutVersioning[0].dataId &&
                    final2023metadata.dataId == result2023WithoutVersioning[0].dataId
                ),
            "The active data set of the reporting period should be the last uploaded one but this is not the case.",
        )
//        When not specifying a reportingPeriod we should retrieve metadata of datasets for all reporting periods
        val resultsWithVersioning = apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(companyId, dataType, false)
        val resultsWithoutVersioning =
            apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(companyId, dataType, true)
        assertTrue(
            (resultsWithVersioning.size == 4),
            "Without filtering for reporting period and when displaying version history, metadata of all 4 " +
                "datasets should be returned but it isn't.",
        )
        assertTrue(
            (
                resultsWithoutVersioning.size == 2 &&
                    resultsWithoutVersioning[0].dataId != resultsWithoutVersioning[1].dataId
                ),
            "a single current dataset per reporting period should be available and it should be different " +
                "ones for the different reporting periods - but it isn't",
        )
    }

    private fun firstUploadForVersionHistory(
        companyInformation: CompanyInformation,
        frameWorkData: EuTaxonomyDataForNonFinancials,
        reportingPeriod: String,
    ): UploadInfo {
        return apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            listOf(companyInformation),
            listOf(frameWorkData),
            apiAccessor.euTaxonomyNonFinancialsUploaderFunction,
            reportingPeriod = reportingPeriod,
        )[0]
    }

    private fun subsequentUploadForVersionHistory(
        companyId: String,
        frameWorkData: EuTaxonomyDataForNonFinancials,
        reportingPeriod: String,
    ): DataMetaInformation {
        val body =
            CompanyAssociatedDataEuTaxonomyDataForNonFinancials(companyId, reportingPeriod, frameWorkData)
        return apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
            .postCompanyAssociatedEuTaxonomyDataForNonFinancials(
                body,
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

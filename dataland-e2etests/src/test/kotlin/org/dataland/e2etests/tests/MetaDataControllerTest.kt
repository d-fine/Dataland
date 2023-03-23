package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QAStatus
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
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

    private fun buildAcceptedAndActiveDataMetaInformation(
        dataId: String,
        companyId: String,
        testDataType: DataTypeEnum,
        uploadTime: Long,
    ) = DataMetaInformation(
        dataId, companyId, testDataType, uploadTime,
        "", true, QAStatus.accepted, null,
    )

    @Test
    fun `post dummy company and taxonomy data for it and check if meta info about that data can be retrieved`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials
        val uploadedMetaInfo = apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(testDataType to listOfOneTestCompanyInformation), 1,
        )[0].actualStoredDataMetaInfo!!
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val actualDataMetaInfo = apiAccessor.metaDataControllerApi.getDataMetaInfo(uploadedMetaInfo.dataId)
        val expectedDataMetaInfo = buildAcceptedAndActiveDataMetaInformation(
            dataId = uploadedMetaInfo.dataId, companyId = uploadedMetaInfo.companyId,
            testDataType = testDataType, uploadTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
        )
        assertEquals(
            expectedDataMetaInfo, actualDataMetaInfo.copy(uploadTime = expectedDataMetaInfo.uploadTime),
            "The meta info of the posted eu taxonomy data does not match the retrieved meta info.",
        )
        val timeDiffFromUploadToNow = actualDataMetaInfo.uploadTime - Instant.now().epochSecond
        assertTrue(
            abs(timeDiffFromUploadToNow) < 60, "The server-upload-time and the local upload time differ too much.",
        )
    }

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
        val listSizeAfterUploads = apiAccessor.getNumberOfDataMetaInfo(dataType = testDataType, showOnlyActive = false)
        val expectedListSize = initListSizeDataMetaInfoForEuTaxoFinancials + totalNumberOfDataSetsPerFramework
        assertEquals(
            expectedListSize, listSizeAfterUploads,
            "The meta info list for all EU Taxonomy Data for Non-Financials is expected to increase by " +
                "$totalNumberOfDataSetsPerFramework to $expectedListSize, but has the size $listSizeAfterUploads.",
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
            buildAcceptedAndActiveDataMetaInformation(
                testDataId,
                listOfUploadInfo[0].actualStoredCompany.companyId,
                testDataType,
                uploadTime,
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
        val expectedMetaInformation = buildAcceptedAndActiveDataMetaInformation(
            dataId = testDataId, companyId = testCompanyId,
            testDataType = testDataType,
            uploadTime = uploadTime,
        )
        assertTrue(
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

    private fun validateShowActiveFlagAndReturnActiveDataMetaInfo(
        companyId: String,
        dataType: DataTypeEnum,
        reportingPeriod: String,
        expectedNumberOfVersions: Int,
    ): DataMetaInformation {
        val listOfMetaData =
            apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(companyId, dataType, false, reportingPeriod)
        val listOfActiveMetaData =
            apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(companyId, dataType, true, reportingPeriod)
        assertEquals(
            expectedNumberOfVersions, listOfMetaData.size, "The number of versions does not equal the expected one.",
        )
        assertEquals(1, listOfActiveMetaData.size, "Metadata for exactly one active dataset should exist.")
        assertTrue(
            (listOfActiveMetaData[0].uploadTime == listOfMetaData.maxOfOrNull { it.uploadTime }),
            "The active result is not the one with the highest upload time.",
        )
        return listOfActiveMetaData[0]
    }

    @Suppress("kotlin:S138")
    @Test
    fun `ensure that version history field in metadata endpoint of meta data controller works`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId

        val frameworkDataAlpha = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getTData(1)[0]
        val reportingPeriod = "2022"
        val uploadedMetadata: MutableList<DataMetaInformation> = mutableListOf()
        uploadedMetadata.add(
            apiAccessor.uploadWithWait(
                companyId,
                frameworkDataAlpha,
                reportingPeriod,
                apiAccessor.euTaxonomyNonFinancialsUploaderFunction,
            ),
        )
        val newNumberOfEmployees = (frameworkDataAlpha.numberOfEmployees ?: BigDecimal.ZERO) + BigDecimal.ONE
        val frameworkDataBeta = frameworkDataAlpha.copy(numberOfEmployees = newNumberOfEmployees)
        uploadedMetadata.add(
            apiAccessor.euTaxonomyNonFinancialsUploaderFunction(
                companyId,
                frameworkDataBeta,
                reportingPeriod,
            ),
        )
        apiAccessor.ensureQaIsPassed(uploadedMetadata)
        val dataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials
        val activeDataset =
            apiAccessor.dataControllerApiForEuTaxonomyNonFinancials.getCompanyAssociatedEuTaxonomyDataForNonFinancials(
                validateShowActiveFlagAndReturnActiveDataMetaInfo(companyId, dataType, reportingPeriod, 2).dataId,
            )
        assertTrue(
            (activeDataset.data!!.numberOfEmployees == newNumberOfEmployees),
            "The active dataset does not have numberOfEmployees of the old one plus 1.",
        )
    }

    @Test
    fun `ensure that reportingPeriod field of metadata endpoint of meta data controller works`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val frameWorkData = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getTData(1)[0]
        val uploadPairs = listOf(
            Pair(frameWorkData, "2022"), Pair(frameWorkData, "2022"), Pair(frameWorkData, "2023"),
            Pair(frameWorkData, "2023"),
        )
        uploadPairs.forEach { pair ->
            apiAccessor.uploadWithWait(
                companyId, pair.first, pair.second,
                apiAccessor.euTaxonomyNonFinancialsUploaderFunction,
            )
        }
        val dataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials
        val listOfMetaData = apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(companyId, dataType, false)
        val listOfActiveMetaData =
            apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(companyId, dataType, true)
        val listOfActiveMetaData2022 =
            apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(companyId, dataType, true, "2022")
        val listOfActiveMetaData2023 =
            apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(companyId, dataType, true, "2023")
        assertTrue(
            (listOfMetaData.size == 4),
            "The number of meta datasets does not equal the expected one.",
        )
        assertTrue(
            listOfActiveMetaData2022[0].dataId != listOfActiveMetaData2023[0].dataId,
            "The active data meta info for the two different reporting Periods are identical.",
        )
        assertTrue(
            listOfActiveMetaData.size == 2 && listOfActiveMetaData.map { it.dataId }.containsAll(
                setOf(listOfActiveMetaData2022[0].dataId, listOfActiveMetaData2023[0].dataId),
            ),
            "The list of active meta data for all reporting periods does not consist of the expected elements.",
        )
    }
}

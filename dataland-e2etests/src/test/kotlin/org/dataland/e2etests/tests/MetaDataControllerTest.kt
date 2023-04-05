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
import java.math.BigDecimal
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

    fun buildAcceptedAndActiveDataMetaInformation(
        dataId: String,
        companyId: String,
        testDataType: DataTypeEnum,
        uploadTime: Long,
    ) = DataMetaInformation(
        dataId = dataId, companyId = companyId, dataType = testDataType, uploadTime = uploadTime,
        reportingPeriod = "", currentlyActive = true, qaStatus = QAStatus.accepted, uploaderUserId = null,
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
            testDataType = testDataType, uploadTime = Instant.now().toEpochMilli(),
        )
        assertEquals(
            expectedDataMetaInfo, actualDataMetaInfo.copy(uploadTime = expectedDataMetaInfo.uploadTime),
            "The meta info of the posted eu taxonomy data does not match the retrieved meta info.",
        )
        val timeDiffFromUploadToNow = actualDataMetaInfo.uploadTime - Instant.now().toEpochMilli()
        assertTrue(
            abs(timeDiffFromUploadToNow) < 60000, "The server-upload-time and the local upload time differ too much.",
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
    fun `ensure that version history field in metadata endpoint of meta data controller works`() {
        val (companyId, reportingPeriod, newNumberOfEmployees) = uploadTwoDataSetsForACompany()
        ensureThatSecondDatasetIsActive(companyId, reportingPeriod, newNumberOfEmployees)
    }

    private fun ensureThatSecondDatasetIsActive(
        companyId: String,
        reportingPeriod: String,
        newNumberOfEmployees: BigDecimal,
    ) {
        val dataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials
        val allDatasets =
            apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(
                companyId = companyId, dataType = dataType, showOnlyActive = false, reportingPeriod = reportingPeriod,
            )
        val activeDatasets =
            apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(
                companyId = companyId, dataType = dataType, showOnlyActive = true, reportingPeriod = reportingPeriod,
            )
        assertEquals(
            2, allDatasets.size, "The number of versions does not equal the expected one.",
        )
        assertEquals(1, activeDatasets.size, "Metadata for exactly one active dataset should exist.")
        assertTrue(
            (activeDatasets[0].uploadTime == allDatasets.maxOfOrNull { it.uploadTime }),
            "The active result is not the one with the highest upload time.",
        )
        val retrievedDataset =
            apiAccessor.dataControllerApiForEuTaxonomyNonFinancials.getCompanyAssociatedEuTaxonomyDataForNonFinancials(
                activeDatasets[0].dataId,
            )
        assertTrue(
            (retrievedDataset.data!!.numberOfEmployees == newNumberOfEmployees),
            "The active dataset does not have numberOfEmployees of the old one plus 1.",
        )
    }

    private fun uploadTwoDataSetsForACompany(): Triple<String, String, BigDecimal> {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId

        val frameworkDataAlpha = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getTData(1)[0]
        val reportingPeriod = "2022"
        apiAccessor.uploadWithWait(
            companyId = companyId,
            frameworkData = frameworkDataAlpha,
            reportingPeriod = reportingPeriod,
            uploadFunction = apiAccessor.euTaxonomyNonFinancialsUploaderFunction,
        )
        val newNumberOfEmployees = (frameworkDataAlpha.numberOfEmployees ?: BigDecimal.ZERO) + BigDecimal.ONE
        val frameworkDataBeta = frameworkDataAlpha.copy(numberOfEmployees = newNumberOfEmployees)
        apiAccessor.uploadSingleFrameworkDataSet(
            companyId = companyId,
            frameworkData = frameworkDataBeta,
            reportingPeriod = reportingPeriod,
            frameworkDataUploadFunction = apiAccessor.euTaxonomyNonFinancialsUploaderFunction,
        )
        return Triple(companyId, reportingPeriod, newNumberOfEmployees)
    }

    @Test
    fun `ensure that reportingPeriod field of metadata endpoint of meta data controller works`() {
        val companyId = uploadFourDatasetsForACompany()
        ensureCorrectDatsetsAreActiveAfterUpload(companyId)
    }

    private fun ensureCorrectDatsetsAreActiveAfterUpload(companyId: String) {
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
            "The list of active meta data for all reporting periods does not consist of the expected element."
        )
    }

    private fun uploadFourDatasetsForACompany(): String {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val frameWorkData = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getTData(1)[0]
        val uploadPairs = listOf(
            Pair(frameWorkData, "2022"), Pair(frameWorkData, "2022"), Pair(frameWorkData, "2023"),
            Pair(frameWorkData, "2023"),
        )
        uploadPairs.forEach { pair ->
            apiAccessor.uploadWithWait(
                companyId = companyId,
                frameworkData = pair.first,
                reportingPeriod = pair.second,
                uploadFunction = apiAccessor.euTaxonomyNonFinancialsUploaderFunction,
            )
        }
        return companyId
    }
}

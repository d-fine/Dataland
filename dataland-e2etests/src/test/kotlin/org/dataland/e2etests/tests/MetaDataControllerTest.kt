package org.dataland.e2etests.tests

import org.awaitility.Awaitility
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.e2etests.auth.GlobalAuth.jwtHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentControllerApiAccessor
import org.dataland.e2etests.utils.MetaDataUtils
import org.dataland.e2etests.utils.QaApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@Suppress("kotlin:S104")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MetaDataControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentControllerApiAccessor()

    private val numberOfCompaniesToPostPerFramework = 4
    private val numberOfDatasetsToPostPerCompany = 5
    private val totalNumberOfDatasetsPerFramework =
        numberOfCompaniesToPostPerFramework * numberOfDatasetsToPostPerCompany

    private val listOfTestCompanyInformation =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(numberOfCompaniesToPostPerFramework)
    private val listOfOneTestCompanyInformation = listOf(listOfTestCompanyInformation[0])

    @BeforeAll
    fun postTestDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    companion object {
        private const val MAX_AWAITILITY_DURATION_MS = 10000L
        private const val AWAITILITY_POLL_DELAY_MS = 500L
    }

    @Test
    fun `post dummy company and taxonomy data for it and check if meta info about that data can be retrieved`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials
        val uploadedMetaInfo =
            apiAccessor
                .uploadCompanyAndFrameworkDataForMultipleFrameworks(
                    mapOf(testDataType to listOfOneTestCompanyInformation), 1,
                )[0]
                .actualStoredDataMetaInfo!!
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val actualDataMetaInfo = apiAccessor.metaDataControllerApi.getDataMetaInfo(uploadedMetaInfo.dataId)
        val expectedDataMetaInfo =
            MetaDataUtils.buildAcceptedAndActiveDataMetaInformation(
                dataId = uploadedMetaInfo.dataId, companyId = uploadedMetaInfo.companyId,
                testDataType = testDataType, TechnicalUser.Admin,
            )
        MetaDataUtils.assertDataMetaInfoMatches(expectedDataMetaInfo = expectedDataMetaInfo, actualDataMetaInfo = actualDataMetaInfo)
        val timeDiffFromUploadToNow = actualDataMetaInfo.uploadTime - Instant.now().toEpochMilli()
        assertTrue(
            abs(timeDiffFromUploadToNow) < 60000, "The server-upload-time and the local upload time differ too much.",
        )
    }

    @Test
    fun `search for a company that does not exist and check that a 404 error is returned`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val clientException =
            assertThrows<ClientException> {
                apiAccessor.companyDataControllerApi.getCompanyById("this-should-not-exist")
            }
        assertEquals(clientException.statusCode, 404)
    }

    private fun postCompanyWithDataAndVerifyMetaInfoSearchResultSizeUsingFilters(
        dataType: DataTypeEnum,
        expectedSizeOfDataMetaInfoSearchResult: Int,
        errorMessage: String,
        getDataMetaInfoResultSize: (List<String>) -> Int,
    ) {
        val companyIds =
            apiAccessor
                .uploadCompanyAndFrameworkDataForMultipleFrameworks(
                    mapOf(dataType to listOfTestCompanyInformation),
                    numberOfDatasetsToPostPerCompany,
                ).let { uploadInfos -> uploadInfos.map { it.actualStoredCompany.companyId }.distinct() }

        Awaitility
            .await()
            .atMost(MAX_AWAITILITY_DURATION_MS, TimeUnit.MILLISECONDS)
            .pollDelay(AWAITILITY_POLL_DELAY_MS, TimeUnit.MILLISECONDS)
            .untilAsserted {
                val actualSizeOfDataMetaInfoSearchResult = getDataMetaInfoResultSize(companyIds)
                assertEquals(
                    expectedSizeOfDataMetaInfoSearchResult,
                    actualSizeOfDataMetaInfoSearchResult,
                    "$errorMessage The found size is $actualSizeOfDataMetaInfoSearchResult.",
                )
            }
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search including inactive datasets`() {
        val dataTypeUploaded = DataTypeEnum.eutaxonomyMinusNonMinusFinancials
        val dataTypeNotUploaded = DataTypeEnum.eutaxonomyMinusFinancials
        val initialNumberOfDataSets =
            listOf(dataTypeUploaded, dataTypeNotUploaded)
                .associateWith {
                    apiAccessor.getNumberOfDataMetaInfo(
                        showOnlyActive = false,
                        dataType = it,
                    )
                }
        val expectedSizeOfDataMetaInfoForUploadedDataType =
            initialNumberOfDataSets.getValue(dataTypeUploaded) + totalNumberOfDatasetsPerFramework
        postCompanyWithDataAndVerifyMetaInfoSearchResultSizeUsingFilters(
            dataType = dataTypeUploaded,
            expectedSizeOfDataMetaInfoSearchResult = expectedSizeOfDataMetaInfoForUploadedDataType,
            errorMessage =
                "The list with all data meta info is expected to increase by " +
                    "$totalNumberOfDatasetsPerFramework to $expectedSizeOfDataMetaInfoForUploadedDataType.",
        ) {
            apiAccessor.getNumberOfDataMetaInfo(
                showOnlyActive = false,
                dataType = dataTypeUploaded,
            )
        }

        assertEquals(
            initialNumberOfDataSets[dataTypeNotUploaded],
            apiAccessor.getNumberOfDataMetaInfo(
                dataType = dataTypeNotUploaded,
                showOnlyActive = false,
            ),
        )
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search excluding inactive datasets`() {
        val usedDataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials
        val initialSizeOfDataMetaInfo =
            apiAccessor.getNumberOfDataMetaInfo(
                showOnlyActive = true,
                dataType = usedDataType,
            )
        val expectedSizeOfDataMetaInfo = initialSizeOfDataMetaInfo + numberOfCompaniesToPostPerFramework
        postCompanyWithDataAndVerifyMetaInfoSearchResultSizeUsingFilters(
            dataType = usedDataType,
            expectedSizeOfDataMetaInfoSearchResult = expectedSizeOfDataMetaInfo,
            errorMessage =
                "The list with all data meta info is expected to increase by " +
                    "$numberOfCompaniesToPostPerFramework to $expectedSizeOfDataMetaInfo.",
        ) {
            apiAccessor.getNumberOfDataMetaInfo(
                showOnlyActive = true,
                dataType = usedDataType,
            )
        }
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search with filter on company ID`() {
        postCompanyWithDataAndVerifyMetaInfoSearchResultSizeUsingFilters(
            dataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials,
            expectedSizeOfDataMetaInfoSearchResult = numberOfDatasetsToPostPerCompany,
            errorMessage =
                "The first posted company is expected to have meta info " +
                    "about $numberOfDatasetsToPostPerCompany data sets.",
        ) { companyIds ->
            apiAccessor.getNumberOfDataMetaInfo(
                companyId = companyIds[0],
                showOnlyActive = false,
            )
        }
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
            numberOfDatasetsToPostPerCompany,
        )
        val listSizeAfterUploads = apiAccessor.getNumberOfDataMetaInfo(dataType = testDataType, showOnlyActive = false)
        val expectedListSize = initListSizeDataMetaInfoForEuTaxoFinancials + totalNumberOfDatasetsPerFramework
        assertEquals(
            expectedListSize, listSizeAfterUploads,
            "The meta info list for all EU Taxonomy Data for Non-Financials is expected to increase by " +
                "$totalNumberOfDatasetsPerFramework to $expectedListSize, but has the size $listSizeAfterUploads.",
        )
    }

    @Test
    fun `post companies and eu taxonomy data and check meta info search with filters on company ID and data type`() {
        val testDataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials
        postCompanyWithDataAndVerifyMetaInfoSearchResultSizeUsingFilters(
            dataType = testDataType,
            expectedSizeOfDataMetaInfoSearchResult = numberOfDatasetsToPostPerCompany,
            errorMessage =
                "The first posted company is expected to have meta info " +
                    "about $numberOfDatasetsToPostPerCompany data sets.",
        ) { companyIds ->
            apiAccessor.getNumberOfDataMetaInfo(
                companyId = companyIds[0],
                dataType = testDataType,
                showOnlyActive = false,
            )
        }
    }

    @Test
    fun `post companies and eu taxonomy financials data and check that no non financials data sets exist`() {
        val initialNumberOfNonFinancialsDatasets =
            apiAccessor.getNumberOfDataMetaInfo(
                dataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials,
                showOnlyActive = false,
            )
        val postedDataType = DataTypeEnum.eutaxonomyMinusFinancials
        postCompanyWithDataAndVerifyMetaInfoSearchResultSizeUsingFilters(
            dataType = postedDataType,
            expectedSizeOfDataMetaInfoSearchResult = totalNumberOfDatasetsPerFramework,
            errorMessage =
                "The posted companies are expected to have a total number " +
                    "of $totalNumberOfDatasetsPerFramework datasets for eu taxonomy financials.",
        ) { companyIds ->
            companyIds
                .map {
                    apiAccessor.getNumberOfDataMetaInfo(
                        companyId = it,
                        dataType = postedDataType,
                        showOnlyActive = false,
                    )
                }.sum()
        }

        assertEquals(
            initialNumberOfNonFinancialsDatasets,
            apiAccessor.getNumberOfDataMetaInfo(
                dataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials,
                showOnlyActive = false,
            ),
        )
    }

    @Test
    fun `ensure that version history field in metadata endpoint of meta data controller works`() {
        val (companyId, reportingPeriod, newNumberOfEmployees) = uploadTwoDatasetsForACompany()
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
            apiAccessor.dataControllerApiForEuTaxonomyNonFinancials.getCompanyAssociatedEutaxonomyNonFinancialsData(
                activeDatasets[0].dataId,
            )
        assertTrue(
            (
                retrievedDataset.data.general!!
                    .numberOfEmployees!!
                    .value == newNumberOfEmployees
            ),
            "The active dataset does not have numberOfEmployees of the old one plus 1.",
        )
    }

    private fun uploadTwoDatasetsForACompany(): Triple<String, String, BigDecimal> {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId

        val frameworkDataAlpha =
            apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
                .getTData(1)[0]
        val reportingPeriod = "2022"
        apiAccessor.uploadWithWait(
            companyId = companyId,
            frameworkData = frameworkDataAlpha,
            reportingPeriod = reportingPeriod,
            uploadFunction = apiAccessor::euTaxonomyNonFinancialsUploaderFunction,
        )
        val newNumberOfEmployees =
            (frameworkDataAlpha.general!!.numberOfEmployees!!.value ?: BigDecimal.ZERO) +
                BigDecimal.ONE
        val frameworkDataBeta =
            frameworkDataAlpha.copy(
                general =
                    frameworkDataAlpha.general!!.copy(
                        numberOfEmployees =
                            frameworkDataAlpha.general!!.numberOfEmployees!!.copy(
                                value = newNumberOfEmployees,
                            ),
                    ),
            )
        apiAccessor.uploadSingleFrameworkDataset(
            companyId = companyId,
            frameworkData = frameworkDataBeta,
            reportingPeriod = reportingPeriod,
            frameworkDataUploadFunction = apiAccessor::euTaxonomyNonFinancialsUploaderFunction,
        )
        return Triple(companyId, reportingPeriod, newNumberOfEmployees)
    }

    @Test
    fun `ensure that reportingPeriod field of metadata endpoint of meta data controller works`() {
        val companyId = uploadFourDatasetsForACompany()
        ensureCorrectDatasetsAreActiveAfterUpload(companyId)
    }

    private fun ensureCorrectDatasetsAreActiveAfterUpload(companyId: String) {
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
            listOfActiveMetaData.size == 2 &&
                listOfActiveMetaData.map { it.dataId }.containsAll(
                    setOf(listOfActiveMetaData2022[0].dataId, listOfActiveMetaData2023[0].dataId),
                ),
            "The list of active meta data for all reporting periods does not consist of the expected elements.",
        )
    }

    private fun uploadFourDatasetsForACompany(): String {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val frameWorkData = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getTData(1)[0]
        val uploadPairs =
            listOf(
                Pair(frameWorkData, "2022"), Pair(frameWorkData, "2022"), Pair(frameWorkData, "2023"),
                Pair(frameWorkData, "2023"),
            )
        uploadPairs.forEach { pair ->
            apiAccessor.uploadWithWait(
                companyId = companyId,
                frameworkData = pair.first,
                reportingPeriod = pair.second,
                uploadFunction = apiAccessor::euTaxonomyNonFinancialsUploaderFunction,
            )
        }
        return companyId
    }

    @Test
    fun `upload datasets and test the different search filters with different combinations`() {
        val companyId = uploadSearchFiltersData()
        checkSearchFilters(companyId)
    }

    private fun uploadSearchFiltersData(): String {
        val sfdrData = apiAccessor.testDataProviderForSfdrData.getTData(1).first()
        val uploadFunction = apiAccessor::sfdrUploaderFunction

        val reportingPeriods = listOf("2005", "2006")
        val combinations =
            reportingPeriods.flatMap { reportingPeriod ->
                listOf(
                    Triple(reportingPeriod, TechnicalUser.Admin, true),
                    Triple(reportingPeriod, TechnicalUser.Admin, false),
                    Triple(reportingPeriod, TechnicalUser.Uploader, false),
                )
            }

        val waitForQaList = mutableListOf<DataMetaInformation>()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val companyId =
            apiAccessor.companyDataControllerApi.postCompany(listOfOneTestCompanyInformation.first()).companyId

        combinations.forEach { (reportingPeriod, user, bypassQa) ->
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(user)
            val metaData = uploadFunction(companyId, sfdrData, reportingPeriod, bypassQa)
            if (bypassQa) {
                waitForQaList.add(metaData)
            }
        }
        QaApiAccessor().ensureQaIsPassed(waitForQaList, apiAccessor.metaDataControllerApi)
        return companyId
    }

    private fun checkSearchFilters(companyId: String) {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val metaDataList =
            apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(
                companyId, null, false, null, null, null,
            )

        val combinations =
            listOf("2005", "2006").flatMap { reportingPeriod ->
                QaStatus.entries.flatMap { qaStatus ->
                    listOf(
                        Triple(listOf(TechnicalUser.Admin, TechnicalUser.Uploader), qaStatus, reportingPeriod),
                        Triple(listOf(TechnicalUser.Uploader), qaStatus, reportingPeriod),
                        Triple(listOf(TechnicalUser.Admin), qaStatus, reportingPeriod),
                    )
                }
            }

        combinations.forEach { (users, qaStatus, reportingPeriod) ->
            val userIds = users.map { UUID.fromString(it.technicalUserId) }.toSet()
            val filteredMetaDatas =
                metaDataList.filter {
                    it.companyId == companyId &&
                        userIds.contains(UUID.fromString(it.uploaderUserId)) &&
                        it.qaStatus == qaStatus &&
                        it.reportingPeriod == reportingPeriod
                }
            val returnedMetaDatas =
                apiAccessor.metaDataControllerApi.getListOfDataMetaInfo(
                    companyId, null, false, reportingPeriod, userIds, qaStatus,
                )

            assertEquals(filteredMetaDatas.toSet(), returnedMetaDatas.toSet())
        }
    }
}

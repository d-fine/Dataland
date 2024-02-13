package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.model.AggregatedDataRequest
import org.dataland.communitymanager.openApiClient.model.BulkDataRequest
import org.dataland.communitymanager.openApiClient.model.DataRequestCompanyIdentifierType
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.checkThatAllIdentifiersWereAccepted
import org.dataland.e2etests.utils.findAggregatedDataRequestDataTypeForFramework
import org.dataland.e2etests.utils.findRequestControllerApiDataTypeForFramework
import org.dataland.e2etests.utils.generateMapWithOneRandomValueForEachIdentifierType
import org.dataland.e2etests.utils.generateRandomIsin
import org.dataland.e2etests.utils.generateRandomLei
import org.dataland.e2etests.utils.generateRandomPermId
import org.dataland.e2etests.utils.getIdForUploadedCompanyWithIdentifiers
import org.dataland.e2etests.utils.iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AggregatedDataRequestsTest {

    val jwtHelper = JwtAuthenticationHelper()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

    @BeforeAll
    fun authenticateAsReader() { jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader) }

    private fun authenticateSendBulkRequestAndCheckAcceptedIdentifiers(
        technicalUser: TechnicalUser,
        identifiers: List<String>,
        frameworks: List<BulkDataRequest.ListOfFrameworkNames>,
        reportingPeriods: List<String>,
    ) {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
        val responseForReader = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiers, frameworks, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(responseForReader, identifiers.size)
    }

    @Test
    fun `post bulk data requests for different users and check that aggregation works properly`() {
        val leiForCompany = generateRandomLei()
        val companyId = getIdForUploadedCompanyWithIdentifiers(leiForCompany)
        val identifierMap = (
            generateMapWithOneRandomValueForEachIdentifierType() + mapOf(
                DataRequestCompanyIdentifierType.multipleRegexMatches to generateRandomPermId(20),
                DataRequestCompanyIdentifierType.datalandCompanyId to leiForCompany,
            )
            ).toMutableMap()
        val frameworks = enumValues<BulkDataRequest.ListOfFrameworkNames>().toList()
        val reportingPeriods = listOf("2022", "2023")
        TechnicalUser.entries.forEach {
            authenticateSendBulkRequestAndCheckAcceptedIdentifiers(
                it, identifierMap.values.toList(), frameworks, reportingPeriods,
            )
        }
        identifierMap[DataRequestCompanyIdentifierType.datalandCompanyId] = companyId
        val aggregatedDataRequests = requestControllerApi.getAggregatedDataRequests()
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequests, frameworks, reportingPeriods, identifierMap, TechnicalUser.entries.size.toLong(),
        )
    }

    private fun testNonTrivialIdentifierValueFilterOnAggregatedLevel(
        frameworks: List<BulkDataRequest.ListOfFrameworkNames>,
        reportingPeriods: List<String>,
        identifiersToRecognizeMap: Map<DataRequestCompanyIdentifierType, String>,
        differentLei: String,
    ) {
        val aggregatedDataRequestsWithoutFilter = requestControllerApi.getAggregatedDataRequests(identifierValue = null)
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequestsWithoutFilter, frameworks, reportingPeriods,
            identifiersToRecognizeMap + mapOf(DataRequestCompanyIdentifierType.lei to differentLei), 1,
        )
        val aggregatedDataRequestsForEmptyString = requestControllerApi.getAggregatedDataRequests(identifierValue = "")
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequestsForEmptyString, frameworks, reportingPeriods,
            identifiersToRecognizeMap + mapOf(DataRequestCompanyIdentifierType.lei to differentLei), 1,
        )
    }

    @Test
    fun `post bulk data request and check that filter for the identifier value on aggregated level works properly`() {
        val permId = generateRandomPermId(10)
        val identifiersToRecognizeMap = mapOf(
            DataRequestCompanyIdentifierType.permId to permId,
            DataRequestCompanyIdentifierType.lei to permId + generateRandomLei().substring(10),
            DataRequestCompanyIdentifierType.isin to generateRandomIsin().substring(0, 2) + permId,
        )
        val differentLei = generateRandomLei()
        val identifiers = identifiersToRecognizeMap.values.toList() + listOf(differentLei)
        val frameworks = listOf(BulkDataRequest.ListOfFrameworkNames.lksg)
        val reportingPeriods = listOf("2023")
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiers, frameworks, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(response, identifiers.size)
        val aggregatedDataRequests = requestControllerApi.getAggregatedDataRequests(identifierValue = permId)
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequests, frameworks, reportingPeriods, identifiersToRecognizeMap, 1,
        )
        assertFalse(aggregatedDataRequests.any { it.dataRequestCompanyIdentifierValue == differentLei })
        testNonTrivialIdentifierValueFilterOnAggregatedLevel(
            frameworks, reportingPeriods, identifiersToRecognizeMap, differentLei,
        )
    }

    private fun checkAggregationForNonTrivialFrameworkFilter(
        frameworks: List<BulkDataRequest.ListOfFrameworkNames>,
        reportingPeriods: List<String>,
        identifierMap: Map<DataRequestCompanyIdentifierType, String>,
    ) {
        listOf(1, (2 until frameworks.size).random(), frameworks.size).forEach { numberOfRandomFrameworks ->
            val randomFrameworks = frameworks.shuffled().take(numberOfRandomFrameworks)
            val aggregatedDataRequests = requestControllerApi.getAggregatedDataRequests(
                dataTypes = randomFrameworks.map { findRequestControllerApiDataTypeForFramework(it) },
            )
            iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
                aggregatedDataRequests, randomFrameworks, reportingPeriods, identifierMap, 1,
            )
            val frameworksNotToBeFound = frameworks.filter { !randomFrameworks.contains(it) }
            frameworksNotToBeFound.forEach { framework ->
                assertFalse(
                    aggregatedDataRequests.any {
                        it.dataType == findAggregatedDataRequestDataTypeForFramework(framework)
                    },
                )
            }
        }
    }

    @Test
    fun `post bulk requests and check that the filter for frameworks on aggregated level works properly`() {
        val frameworks = enumValues<BulkDataRequest.ListOfFrameworkNames>().toList()
        val reportingPeriods = listOf("2023")
        val identifierMap = mapOf(DataRequestCompanyIdentifierType.lei to generateRandomLei())
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifierMap.values.toList(), frameworks, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(response, identifierMap.size)
        checkAggregationForNonTrivialFrameworkFilter(frameworks, reportingPeriods, identifierMap)
        val aggregatedDataRequestsWithoutFilter = requestControllerApi.getAggregatedDataRequests(dataTypes = null)
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequestsWithoutFilter, frameworks, reportingPeriods, identifierMap, 1,
        )
        val aggregatedDataRequestsForEmptyList = requestControllerApi.getAggregatedDataRequests(dataTypes = emptyList())
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequestsForEmptyList, frameworks, reportingPeriods, identifierMap, 1,
        )
    }

    @Test
    fun `post bulk data request and check that the filter for reporting periods on aggregated level works properly`() {
        val identifierMap = mapOf(DataRequestCompanyIdentifierType.lei to generateRandomLei())
        val frameworks = listOf(BulkDataRequest.ListOfFrameworkNames.lksg)
        val reportingPeriods = listOf("2020", "2021", "2022", "2023")
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifierMap.values.toList(), frameworks, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(response, identifierMap.size)
        val randomReportingPeriod = reportingPeriods.random()
        val aggregatedDataRequests = requestControllerApi.getAggregatedDataRequests(
            reportingPeriod = randomReportingPeriod,
        )
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequests, frameworks, listOf(randomReportingPeriod), identifierMap, 1,
        )
        reportingPeriods.filter { it != randomReportingPeriod }.forEach { filteredReportingPeriod ->
            assertFalse(aggregatedDataRequests.any { it.reportingPeriod == filteredReportingPeriod })
        }
        val aggregatedDataRequestsWithoutFilter = requestControllerApi.getAggregatedDataRequests(reportingPeriod = null)
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequestsWithoutFilter, frameworks, reportingPeriods, identifierMap, 1,
        )
        val aggregatedDataRequestsForEmptyString = requestControllerApi.getAggregatedDataRequests(reportingPeriod = "")
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequestsForEmptyString, frameworks, reportingPeriods, identifierMap, 1,
        )
    }

    @Test
    fun `post bulk data request and check that the filter for request status on aggregated level works properly`() {
        val randomLei = generateRandomLei()
        val identifierMap = mapOf(DataRequestCompanyIdentifierType.lei to randomLei)
        val frameworks = listOf(BulkDataRequest.ListOfFrameworkNames.lksg)
        val reportingPeriods = listOf("2020", "2021")
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifierMap.values.toList(), frameworks, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(response, identifierMap.size)
        val aggregatedDataRequests = requestControllerApi.getAggregatedDataRequests()
        assertNumberOfMatchesOnExclusivelyOpenRequestsEquals(aggregatedDataRequests, randomLei, 2)
        val aggregatedRequestsNoFilter = requestControllerApi.getAggregatedDataRequests()
        assertNumberOfMatchesOnExclusivelyOpenRequestsEquals(aggregatedRequestsNoFilter, randomLei, 2)
    }

    private fun assertNumberOfMatchesOnExclusivelyOpenRequestsEquals(
        aggregatedDataRequests: List<AggregatedDataRequest>,
        companyIdentifier: String,
        countOfOpenRequests: Long,
    ) {
        val allRequestStati = RequestStatus.entries.toSet()
        assertNumberOfMatchesOnRequestStatusEquals(
            aggregatedDataRequests, companyIdentifier, setOf(RequestStatus.open), countOfOpenRequests,
        )
        assertNumberOfMatchesOnRequestStatusEquals(
            aggregatedDataRequests, companyIdentifier, setOf(RequestStatus.answered), 0,
        )
        assertNumberOfMatchesOnRequestStatusEquals(
            aggregatedDataRequests, companyIdentifier, setOf(RequestStatus.closed), 0,
        )
        assertNumberOfMatchesOnRequestStatusEquals(
            aggregatedDataRequests, companyIdentifier, allRequestStati, countOfOpenRequests,
        )
    }

    private fun assertNumberOfMatchesOnRequestStatusEquals(
        aggregatedDataRequests: List<AggregatedDataRequest>,
        companyIdentifier: String,
        stati: Set<RequestStatus>,
        count: Long,
    ) {
        val numberOfStatusMatches = aggregatedDataRequests.filter {
            it.dataRequestCompanyIdentifierValue == companyIdentifier &&
                it.requestStatus in stati
        }.sumOf { it.count }
        assertEquals(count, numberOfStatusMatches)
    }
}

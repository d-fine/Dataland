package org.dataland.e2etests.tests.communityManager
//TODO reactivate tests
/*
import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.model.AggregatedDataRequest
import org.dataland.communitymanager.openApiClient.model.BulkDataRequest
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.communityManager.checkThatAllIdentifiersWereAccepted
import org.dataland.e2etests.utils.communityManager.findAggregatedDataRequestDataTypeForFramework
import org.dataland.e2etests.utils.communityManager.findRequestControllerApiDataTypeForFramework
import org.dataland.e2etests.utils.communityManager.generateCompaniesWithOneRandomValueForEachIdentifierType
import org.dataland.e2etests.utils.communityManager.generateMapWithOneRandomValueForEachIdentifierType
import org.dataland.e2etests.utils.communityManager.generateRandomIsin
import org.dataland.e2etests.utils.communityManager.generateRandomLei
import org.dataland.e2etests.utils.communityManager.generateRandomPermId
import org.dataland.e2etests.utils.communityManager.getUniqueDatalandCompanyIdForIdentifierValue
import org.dataland.e2etests.utils.communityManager.iterateThroughAllThreeSpecificationsAndCheckAggregationWithCount
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AggregatedDataRequestsTest {

    val jwtHelper = JwtAuthenticationHelper()
    val apiAccessor = ApiAccessor()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

    @BeforeAll
    fun authenticateAsReader() { jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader) }

    private fun authenticateSendBulkRequestAndCheckAcceptedIdentifiers(
        technicalUser: TechnicalUser,
        identifiers: Set<String>,
        frameworks: Set<BulkDataRequest.DataTypes>,
        reportingPeriods: Set<String>,
    ) {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
        val responseForReader = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiers, frameworks, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(responseForReader, identifiers.size, 0)
    }

    @Test
    fun `post bulk data requests for different users and check that aggregation works properly`() {
        val identifierMap = generateMapWithOneRandomValueForEachIdentifierType()
        generateCompaniesWithOneRandomValueForEachIdentifierType(identifierMap)
        val frameworks = enumValues<BulkDataRequest.DataTypes>().toSet()
        val reportingPeriods = setOf("2022", "2023")
        TechnicalUser.entries.forEach {
            authenticateSendBulkRequestAndCheckAcceptedIdentifiers(
                it, identifierMap.values.toSet(), frameworks, reportingPeriods,
            )
        }
        val aggregatedDataRequests = requestControllerApi.getAggregatedDataRequests()
        iterateThroughAllThreeSpecificationsAndCheckAggregationWithCount(
            aggregatedDataRequests, frameworks, reportingPeriods,
            identifierMap.values.toSet(), TechnicalUser.entries.size.toLong(),
        )
    }

    private fun testNonTrivialIdentifierValueFilterOnAggregatedLevel(
        frameworks: Set<BulkDataRequest.DataTypes>,
        reportingPeriods: Set<String>,
        identifiersToRecognize: Set<String>,
    ) {
        val aggregatedDataRequestsWithoutFilter = requestControllerApi.getAggregatedDataRequests(identifierValue = null)
        iterateThroughAllThreeSpecificationsAndCheckAggregationWithCount(
            aggregatedDataRequestsWithoutFilter, frameworks, reportingPeriods,
            identifiersToRecognize, 1,
        )
        val aggregatedDataRequestsForEmptyString = requestControllerApi.getAggregatedDataRequests(identifierValue = "")
        iterateThroughAllThreeSpecificationsAndCheckAggregationWithCount(
            aggregatedDataRequestsForEmptyString, frameworks, reportingPeriods,
            identifiersToRecognize, 1,
        )
    }

    @Test
    fun `post bulk data request and check that filter for the identifier value on aggregated level works properly`() {
        val identifiersToMap = mapOf(
            IdentifierType.PermId to generateRandomPermId(10),
            IdentifierType.Lei to generateRandomLei(),
            IdentifierType.Isin to generateRandomIsin(),
        )
        generateCompaniesWithOneRandomValueForEachIdentifierType(identifiersToMap)
        val identifierNotToRecognizeSet = setOf(generateRandomLei())
        val identifiers = identifiersToMap.values.toSet() + identifierNotToRecognizeSet
        val frameworks = setOf(BulkDataRequest.DataTypes.lksg)
        val reportingPeriods = setOf("2023")
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiers, frameworks, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(response, identifiers.size - 1, 1)
        val aggregatedDataRequest = requestControllerApi.getAggregatedDataRequests(
            identifierValue = apiAccessor.companyDataControllerApi.getCompaniesBySearchString(
                identifiersToMap.getValue(IdentifierType.Isin),
            ).first().companyId,
        )
        iterateThroughAllThreeSpecificationsAndCheckAggregationWithCount(
            aggregatedDataRequest, frameworks, reportingPeriods,
            identifiersToMap.filterKeys { it == IdentifierType.Isin }.values.toSet(), 1,
        )
        testNonTrivialIdentifierValueFilterOnAggregatedLevel(
            frameworks, reportingPeriods, identifiersToMap.values.toSet(),
        )
    }

    private fun checkAggregationForNonTrivialFrameworkFilter(
        frameworks: Set<BulkDataRequest.DataTypes>,
        reportingPeriods: Set<String>,
        identifiers: Set<String>,
    ) {
        listOf(1, (2 until frameworks.size).random(), frameworks.size).forEach { numberOfRandomFrameworks ->
            val randomFrameworks = frameworks.shuffled().take(numberOfRandomFrameworks)
            val aggregatedDataRequests = requestControllerApi.getAggregatedDataRequests(
                dataTypes = randomFrameworks.map { findRequestControllerApiDataTypeForFramework(it) },
            )
            iterateThroughAllThreeSpecificationsAndCheckAggregationWithCount(
                aggregatedDataRequests, randomFrameworks.toSet(), reportingPeriods, identifiers, 1,
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
        val frameworks = enumValues<BulkDataRequest.DataTypes>().toSet()
        val reportingPeriods = setOf("2023")
        val identifierMap = mapOf(IdentifierType.Lei to generateRandomLei())
        generateCompaniesWithOneRandomValueForEachIdentifierType(identifierMap)
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifierMap.values.toSet(), frameworks, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(response, identifierMap.size, 0)
        checkAggregationForNonTrivialFrameworkFilter(frameworks, reportingPeriods, identifierMap.values.toSet())
        val aggregatedDataRequestsWithoutFilter = requestControllerApi.getAggregatedDataRequests(dataTypes = null)
        iterateThroughAllThreeSpecificationsAndCheckAggregationWithCount(
            aggregatedDataRequestsWithoutFilter, frameworks, reportingPeriods, identifierMap.values.toSet(), 1,
        )
        val aggregatedDataRequestsForEmptyList = requestControllerApi.getAggregatedDataRequests(dataTypes = emptyList())
        iterateThroughAllThreeSpecificationsAndCheckAggregationWithCount(
            aggregatedDataRequestsForEmptyList, frameworks, reportingPeriods, identifierMap.values.toSet(), 1,
        )
    }

    @Test
    fun `post bulk data request and check that the filter for reporting periods on aggregated level works properly`() {
        val identifierMap = mapOf(IdentifierType.Lei to generateRandomLei())
        val frameworks = setOf(BulkDataRequest.DataTypes.lksg)
        val reportingPeriods = setOf("2020", "2021", "2022", "2023")
        generateCompaniesWithOneRandomValueForEachIdentifierType(identifierMap)
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifierMap.values.toSet(), frameworks, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(response, identifierMap.size, 0)
        val randomReportingPeriod = reportingPeriods.random()
        val aggregatedDataRequests = requestControllerApi.getAggregatedDataRequests(
            reportingPeriod = randomReportingPeriod,
        )
        iterateThroughAllThreeSpecificationsAndCheckAggregationWithCount(
            aggregatedDataRequests, frameworks, setOf(randomReportingPeriod), identifierMap.values.toSet(), 1,
        )
        reportingPeriods.filter { it != randomReportingPeriod }.forEach { filteredReportingPeriod ->
            assertFalse(aggregatedDataRequests.any { it.reportingPeriod == filteredReportingPeriod })
        }
        val aggregatedDataRequestsWithoutFilter = requestControllerApi.getAggregatedDataRequests(reportingPeriod = null)
        iterateThroughAllThreeSpecificationsAndCheckAggregationWithCount(
            aggregatedDataRequestsWithoutFilter, frameworks, reportingPeriods, identifierMap.values.toSet(), 1,
        )
        val aggregatedDataRequestsForEmptyString = requestControllerApi.getAggregatedDataRequests(reportingPeriod = "")
        iterateThroughAllThreeSpecificationsAndCheckAggregationWithCount(
            aggregatedDataRequestsForEmptyString, frameworks, reportingPeriods, identifierMap.values.toSet(), 1,
        )
    }

    @Test
    fun `post bulk data request and check that the filter for request status on aggregated level works properly`() {
        val randomLei = generateRandomLei()
        val identifierMap = mapOf(IdentifierType.Lei to randomLei)
        generateCompaniesWithOneRandomValueForEachIdentifierType(identifierMap)
        val datalandCompanyIDForLei = getUniqueDatalandCompanyIdForIdentifierValue(
            identifierMap.getValue(IdentifierType.Lei),
        )
        val frameworks = setOf(BulkDataRequest.DataTypes.lksg)
        val reportingPeriods = setOf("2020", "2021")
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifierMap.values.toSet(), frameworks, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(response, identifierMap.size, 0)
        val aggregatedDataRequests = requestControllerApi.getAggregatedDataRequests(status = RequestStatus.Open)
        assertNumberOfMatchesOnExclusivelyOpenRequestsEquals(
            aggregatedDataRequests, datalandCompanyIDForLei,
        )
        val aggregatedRequestsNoFilter = requestControllerApi.getAggregatedDataRequests()
        assertNumberOfMatchesOnExclusivelyOpenRequestsEquals(
            aggregatedRequestsNoFilter, datalandCompanyIDForLei,
        )
    }

    private fun assertNumberOfMatchesOnExclusivelyOpenRequestsEquals(
        aggregatedDataRequests: List<AggregatedDataRequest>,
        companyIdentifierValue: String,
        countOfOpenRequests: Long = 2, // currently only used with input 2, default added because of linting
    ) {
        val allRequestStati = RequestStatus.entries.toSet()
        assertNumberOfMatchesOnRequestStatusEquals(
            aggregatedDataRequests, companyIdentifierValue, setOf(RequestStatus.Open), countOfOpenRequests,
        )
        assertNumberOfMatchesOnRequestStatusEquals(
            aggregatedDataRequests, companyIdentifierValue, setOf(RequestStatus.Answered), 0,
        )
        assertNumberOfMatchesOnRequestStatusEquals(
            aggregatedDataRequests, companyIdentifierValue, setOf(RequestStatus.Resolved), 0,
        )
        assertNumberOfMatchesOnRequestStatusEquals(
            aggregatedDataRequests, companyIdentifierValue, allRequestStati, countOfOpenRequests,
        )
    }

    private fun assertNumberOfMatchesOnRequestStatusEquals(
        aggregatedDataRequests: List<AggregatedDataRequest>,
        companyIdentifier: String,
        stati: Set<RequestStatus>,
        count: Long,
    ) {
        val numberOfStatusMatches = aggregatedDataRequests.filter {
            it.datalandCompanyId == companyIdentifier &&
                it.requestStatus in stati
        }.sumOf { it.count }
        assertEquals(count, numberOfStatusMatches)
    }
}
*/

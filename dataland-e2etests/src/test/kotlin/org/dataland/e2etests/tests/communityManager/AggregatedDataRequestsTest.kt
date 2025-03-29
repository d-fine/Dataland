package org.dataland.e2etests.tests.communityManager
import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.model.AggregatedDataRequestWithAggregatedPriority
import org.dataland.communitymanager.openApiClient.model.BulkDataRequest
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.communityManager.checkThatNumberOfRejectedIdentifiersIsAsExpected
import org.dataland.e2etests.utils.communityManager.findAggregatedDataRequestDataTypeForFramework
import org.dataland.e2etests.utils.communityManager.findRequestControllerApiDataTypeForFramework
import org.dataland.e2etests.utils.communityManager.generateCompaniesWithOneRandomValueForEachIdentifierType
import org.dataland.e2etests.utils.communityManager.generateMapWithOneRandomValueForEachIdentifierType
import org.dataland.e2etests.utils.communityManager.generateRandomLei
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
    fun authenticateAsReader() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
    }

    private fun authenticateSendBulkRequestAndCheckAcceptedIdentifiers(
        technicalUser: TechnicalUser,
        identifiers: Set<String>,
        frameworks: Set<BulkDataRequest.DataTypes>,
        reportingPeriods: Set<String>,
    ) {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
        val responseForReader =
            requestControllerApi.postBulkDataRequest(
                BulkDataRequest(identifiers, frameworks, reportingPeriods, emailOnUpdate = false),
            )
        checkThatNumberOfRejectedIdentifiersIsAsExpected(responseForReader, 0)
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
        val aggregatedDataRequests = requestControllerApi.getAggregatedOpenDataRequests()
        iterateThroughAllThreeSpecificationsAndCheckAggregationWithCount(
            aggregatedDataRequests, frameworks, reportingPeriods,
            identifierMap.values.toSet(), TechnicalUser.entries.size.toLong(),
        )
    }

    private fun checkAggregationForNonTrivialFrameworkFilter(
        frameworks: Set<BulkDataRequest.DataTypes>,
        reportingPeriods: Set<String>,
        identifiers: Set<String>,
    ) {
        listOf(1, (2 until frameworks.size).random(), frameworks.size).forEach { numberOfRandomFrameworks ->
            val randomFrameworks = frameworks.shuffled().take(numberOfRandomFrameworks)
            val aggregatedDataRequests =
                requestControllerApi.getAggregatedOpenDataRequests(
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
        val response =
            requestControllerApi.postBulkDataRequest(
                BulkDataRequest(identifierMap.values.toSet(), frameworks, reportingPeriods, emailOnUpdate = false),
            )
        checkThatNumberOfRejectedIdentifiersIsAsExpected(response, 0)
        checkAggregationForNonTrivialFrameworkFilter(frameworks, reportingPeriods, identifierMap.values.toSet())
        val aggregatedDataRequestsWithoutFilter = requestControllerApi.getAggregatedOpenDataRequests(dataTypes = null)
        iterateThroughAllThreeSpecificationsAndCheckAggregationWithCount(
            aggregatedDataRequestsWithoutFilter, frameworks, reportingPeriods, identifierMap.values.toSet(), 1,
        )
        val aggregatedDataRequestsForEmptyList = requestControllerApi.getAggregatedOpenDataRequests(dataTypes = emptyList())
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
        val response =
            requestControllerApi.postBulkDataRequest(
                BulkDataRequest(identifierMap.values.toSet(), frameworks, reportingPeriods, emailOnUpdate = false),
            )
        checkThatNumberOfRejectedIdentifiersIsAsExpected(response, 0)
        val randomReportingPeriod = reportingPeriods.random()
        val aggregatedDataRequests =
            requestControllerApi.getAggregatedOpenDataRequests(
                reportingPeriod = randomReportingPeriod,
            )
        iterateThroughAllThreeSpecificationsAndCheckAggregationWithCount(
            aggregatedDataRequests, frameworks, setOf(randomReportingPeriod), identifierMap.values.toSet(), 1,
        )
        reportingPeriods.filter { it != randomReportingPeriod }.forEach { filteredReportingPeriod ->
            assertFalse(aggregatedDataRequests.any { it.reportingPeriod == filteredReportingPeriod })
        }
        val aggregatedDataRequestsWithoutFilter = requestControllerApi.getAggregatedOpenDataRequests(reportingPeriod = null)
        iterateThroughAllThreeSpecificationsAndCheckAggregationWithCount(
            aggregatedDataRequestsWithoutFilter, frameworks, reportingPeriods, identifierMap.values.toSet(), 1,
        )
        val aggregatedDataRequestsForEmptyString = requestControllerApi.getAggregatedOpenDataRequests(reportingPeriod = "")
        iterateThroughAllThreeSpecificationsAndCheckAggregationWithCount(
            aggregatedDataRequestsForEmptyString, frameworks, reportingPeriods, identifierMap.values.toSet(), 1,
        )
    }

    @Test
    fun `post bulk data request and check that the filter for request status on aggregated level works properly`() {
        val randomLei = generateRandomLei()
        val identifierMap = mapOf(IdentifierType.Lei to randomLei)
        generateCompaniesWithOneRandomValueForEachIdentifierType(identifierMap)
        val datalandCompanyIDForLei =
            getUniqueDatalandCompanyIdForIdentifierValue(
                identifierMap.getValue(IdentifierType.Lei),
            )
        val frameworks = setOf(BulkDataRequest.DataTypes.lksg)
        val reportingPeriods = setOf("2020", "2021")
        val response =
            requestControllerApi.postBulkDataRequest(
                BulkDataRequest(identifierMap.values.toSet(), frameworks, reportingPeriods, emailOnUpdate = false),
            )
        checkThatNumberOfRejectedIdentifiersIsAsExpected(response, 0)
        val aggregatedDataRequests = requestControllerApi.getAggregatedOpenDataRequests()
        assertNumberOfMatchesOnExclusivelyOpenRequestsEquals(
            aggregatedDataRequests, datalandCompanyIDForLei,
        )
    }

    private fun assertNumberOfMatchesOnExclusivelyOpenRequestsEquals(
        aggregatedDataRequests: List<AggregatedDataRequestWithAggregatedPriority>,
        companyIdentifierValue: String,
        countOfOpenRequests: Long = 2, // currently only used with input 2, default added because of linting
    ) {
        assertNumberOfMatchesOnRequestStatusEquals(
            aggregatedDataRequests, companyIdentifierValue, countOfOpenRequests,
        )
    }

    private fun assertNumberOfMatchesOnRequestStatusEquals(
        aggregatedDataRequests: List<AggregatedDataRequestWithAggregatedPriority>,
        companyIdentifier: String,
        count: Long,
    ) {
        val numberOfStatusMatches =
            aggregatedDataRequests
                .filter {
                    it.datalandCompanyId == companyIdentifier
                }.sumOf { it.count }
        assertEquals(count, numberOfStatusMatches)
    }
}

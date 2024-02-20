package org.dataland.e2etests.tests.communityManager

import okhttp3.internal.concurrent.TaskRunner.Companion.logger
import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ClientError
import org.dataland.communitymanager.openApiClient.model.BulkDataRequest
import org.dataland.communitymanager.openApiClient.model.StoredDataRequest
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.causeClientExceptionByBulkDataRequest
import org.dataland.e2etests.utils.check400ClientExceptionErrorMessage
import org.dataland.e2etests.utils.checkErrorMessageForInvalidIdentifiersInBulkRequest
import org.dataland.e2etests.utils.checkThatAllIdentifiersWereAccepted
import org.dataland.e2etests.utils.checkThatMessageIsAsExpected
import org.dataland.e2etests.utils.checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce
import org.dataland.e2etests.utils.checkThatTheAmountOfNewlyStoredRequestsIsAsExpected
import org.dataland.e2etests.utils.checkThatTheNumberOfAcceptedIdentifiersIsAsExpected
import org.dataland.e2etests.utils.checkThatTheNumberOfRejectedIdentifiersIsAsExpected
import org.dataland.e2etests.utils.findAggregatedDataRequestDataTypeForFramework
import org.dataland.e2etests.utils.findRequestControllerApiDataTypeForFramework
import org.dataland.e2etests.utils.generateCompaniesWithOneRandomValueForEachIdentifierType
import org.dataland.e2etests.utils.generateMapWithOneRandomValueForEachIdentifierType
import org.dataland.e2etests.utils.generateRandomIsin
import org.dataland.e2etests.utils.generateRandomLei
import org.dataland.e2etests.utils.generateRandomPermId
import org.dataland.e2etests.utils.getDatalandCompanyIdForIdentifierValue
import org.dataland.e2etests.utils.getIdForUploadedCompanyWithIdentifiers
import org.dataland.e2etests.utils.iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount
import org.dataland.e2etests.utils.retrieveTimeAndWaitOneMillisecond
import org.dataland.e2etests.utils.sendBulkRequestWithEmptyInputAndCheckErrorMessage
import org.hamcrest.collection.IsIn
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BulkDataRequestsTest {

    val jwtHelper = JwtAuthenticationHelper()
    val apiAccessor = ApiAccessor()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

    private fun getNewlyStoredRequestsAfterTimestamp(timestamp: Long): List<StoredDataRequest> {
        return requestControllerApi.getDataRequestsForUser().filter { storedDataRequest ->
            storedDataRequest.creationTimestamp > timestamp
        }
    }

    @BeforeAll
    fun authenticateAsReader() { jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader) }

    @Test
    fun `post bulk data request for all frameworks and different valid identifiers and check stored requests`() {
        val uniqueIdentifiersMap = generateMapWithOneRandomValueForEachIdentifierType()
        val identifiers = uniqueIdentifiersMap.values.toList()
        val frameworks = enumValues<BulkDataRequest.ListOfFrameworkNames>().toList()
        val reportingPeriods = listOf("2022", "2023")
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        generateCompaniesWithOneRandomValueForEachIdentifierType(uniqueIdentifiersMap)
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiers, frameworks, reportingPeriods),
        )

        checkThatAllIdentifiersWereAccepted(response,identifiers.size,0)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(
            newlyStoredRequests, identifiers.size * frameworks.size * reportingPeriods.size,
        )

        val randomUniqueDataRequestCompanyIdentifierType = uniqueIdentifiersMap.keys.random()
        uniqueIdentifiersMap[randomUniqueDataRequestCompanyIdentifierType]?.let {
            checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce(
                newlyStoredRequests, frameworks.random(), reportingPeriods.random(),
                getDatalandCompanyIdForIdentifierValue(it)
            )
        }

    }

    @Test
    fun `post a bulk data request with at least one invalid identifier and check that this gives no stored request`() {
        val uniqueIdentifiersMap = generateMapWithOneRandomValueForEachIdentifierType()
        val validIdentifiers = uniqueIdentifiersMap.values.toList()
        val invalidIdentifiers = listOf(
            generateRandomLei() + "F", generateRandomIsin() + "F", generateRandomPermId() + "F",
        )
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        generateCompaniesWithOneRandomValueForEachIdentifierType(uniqueIdentifiersMap)
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(
                validIdentifiers + invalidIdentifiers,
                listOf(BulkDataRequest.ListOfFrameworkNames.lksg),
                listOf("2023"),
            ),
        )
        checkThatTheNumberOfAcceptedIdentifiersIsAsExpected(response, validIdentifiers.size)
        checkThatTheNumberOfRejectedIdentifiersIsAsExpected(response, invalidIdentifiers.size)
        checkThatMessageIsAsExpected(response, validIdentifiers.size, invalidIdentifiers.size)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(newlyStoredRequests, validIdentifiers.size)
        assertFalse(
            newlyStoredRequests.any { invalidIdentifiers.contains(it.datalandCompanyId) },
        )
    }

    @Test
    fun `post bulk data request with at least one company duplicate and check that only one request is stored`() {
        val leiForCompany = generateRandomLei()
        val isinForCompany = generateRandomIsin()
        val companyId = getIdForUploadedCompanyWithIdentifiers(leiForCompany, listOf(isinForCompany))
        val identifierValueForUnknownCompany = generateRandomLei()
        val identifiersForBulkRequest = listOf(leiForCompany, isinForCompany, identifierValueForUnknownCompany)
        val frameworksForBulkRequest = listOf(BulkDataRequest.ListOfFrameworkNames.lksg)
        val reportingPeriodsForBulkRequest = listOf("2023")
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiersForBulkRequest, frameworksForBulkRequest, reportingPeriodsForBulkRequest),
        )
        checkThatAllIdentifiersWereAccepted(response, (identifiersForBulkRequest.size-1),1)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(
            newlyStoredRequests,
            (identifiersForBulkRequest.size - 2) * frameworksForBulkRequest.size * reportingPeriodsForBulkRequest.size,
        )
        checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce(
            newlyStoredRequests, frameworksForBulkRequest[0], reportingPeriodsForBulkRequest[0],companyId,
        )
    }


    private fun checkThatBothRequestsExistExactlyOnceAfterBulkRequest(
        requestsStoredAfterBulkRequest: List<StoredDataRequest>,
        framework: BulkDataRequest.ListOfFrameworkNames,
        reportingPeriod: String,
        companyId: String,
        identifierValueForUnknownCompany: String,
    ) {
        checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce(
            requestsStoredAfterBulkRequest,
            framework,
            reportingPeriod,
            companyId,
        )
        checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce(
            requestsStoredAfterBulkRequest,
            framework,
            reportingPeriod,
            identifierValueForUnknownCompany,
        )
    }

    private fun checkThatAlreadyExistingRequestsAreNeitherStoredForKnownNorForUnknownCompanies(
        frameworks: List<BulkDataRequest.ListOfFrameworkNames>,
        reportingPeriods: List<String>,
        companyId: String,
        identifierMapForUnknownCompany: String,
        firstIdentifiers: List<String>,
        secondIdentifiers: List<String>,
    ) {
        val timeBeforeFirstBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val firstResponse = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(firstIdentifiers, frameworks, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(firstResponse, firstIdentifiers.size,0)
        val newRequestsAfter1stBulkRequest = getNewlyStoredRequestsAfterTimestamp(timeBeforeFirstBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(
            newRequestsAfter1stBulkRequest, firstIdentifiers.size * frameworks.size * reportingPeriods.size,
        )

        checkThatBothRequestsExistExactlyOnceAfterBulkRequest(
            newRequestsAfter1stBulkRequest, frameworks[0], reportingPeriods[0], companyId,
            identifierMapForUnknownCompany,
        )

        val timestampBeforeSecondBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val secondResponse = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(secondIdentifiers, frameworks, reportingPeriods),
        )

        checkThatAllIdentifiersWereAccepted(secondResponse, secondIdentifiers.size,0)
        val newRequestsAfter2ndBulkRequest = getNewlyStoredRequestsAfterTimestamp(timestampBeforeSecondBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(newRequestsAfter2ndBulkRequest, 0)
        val newRequestsAfter1stAnd2ndBulkRequest = getNewlyStoredRequestsAfterTimestamp(timeBeforeFirstBulkRequest)
        checkThatBothRequestsExistExactlyOnceAfterBulkRequest(
            newRequestsAfter1stAnd2ndBulkRequest, frameworks[0], reportingPeriods[0], companyId,
            identifierMapForUnknownCompany,
        )

    }

    @Test
    fun `post a bulk data request with at least one already existing request and check that this one is ignored`() {
        val leiForCompany = generateRandomLei()
        val isinForCompany = generateRandomIsin()
        val companyId = getIdForUploadedCompanyWithIdentifiers(leiForCompany, listOf(isinForCompany))
        val identifierMapForUnknownCompany = mapOf(IdentifierType.lei to generateRandomLei())
        generateCompaniesWithOneRandomValueForEachIdentifierType(identifierMapForUnknownCompany)
        val companyIdForUnknownCompany = getDatalandCompanyIdForIdentifierValue(identifierMapForUnknownCompany.getValue(IdentifierType.lei))
        val frameworks = listOf(BulkDataRequest.ListOfFrameworkNames.lksg)
        val reportingPeriods = listOf("2023")
        val firstIdentifiers = listOf(leiForCompany, identifierMapForUnknownCompany.values.toList()[0])
        val secondIdentifiers = listOf(isinForCompany, identifierMapForUnknownCompany.values.toList()[0])
        checkThatAlreadyExistingRequestsAreNeitherStoredForKnownNorForUnknownCompanies(
            frameworks,
            reportingPeriods,
            companyId,
            companyIdForUnknownCompany,
            firstIdentifiers,
            secondIdentifiers,
        )

    }

    @Test
    fun `check the expected exception is thrown when frameworks are empty or identifiers are empty or invalid only`() {
        val validIdentifiers = listOf(generateRandomLei(), generateRandomIsin(), generateRandomPermId())
        val frameworks = enumValues<BulkDataRequest.ListOfFrameworkNames>().toList()
        val reportingPeriods = listOf("2023")
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(validIdentifiers, frameworks, emptyList())
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(validIdentifiers, emptyList(), reportingPeriods)
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(emptyList(), frameworks, reportingPeriods)
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(validIdentifiers, emptyList(), emptyList())
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(emptyList(), frameworks, emptyList())
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(emptyList(), emptyList(), reportingPeriods)
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(emptyList(), emptyList(), emptyList())
        val invalidIdentifiers = listOf(
            generateRandomLei() + "F", generateRandomIsin() + "F", generateRandomPermId() + "F",
        )
        val clientException = causeClientExceptionByBulkDataRequest(invalidIdentifiers, frameworks, reportingPeriods)
        checkErrorMessageForInvalidIdentifiersInBulkRequest(clientException)
    }

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
        checkThatAllIdentifiersWereAccepted(responseForReader, identifiers.size,0)
    }

    @Test
    fun `post bulk data requests for different users and check that aggregation works properly`() {
        val identifierMap = generateMapWithOneRandomValueForEachIdentifierType()
        generateCompaniesWithOneRandomValueForEachIdentifierType(identifierMap)
        val frameworks = enumValues<BulkDataRequest.ListOfFrameworkNames>().toList()
        val reportingPeriods = listOf("2022", "2023")
        TechnicalUser.entries.forEach {
            authenticateSendBulkRequestAndCheckAcceptedIdentifiers(
                it, identifierMap.values.toList(), frameworks, reportingPeriods,
            )
        }
        val aggregatedDataRequests = requestControllerApi.getAggregatedDataRequests()
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequests, frameworks, reportingPeriods, identifierMap, TechnicalUser.entries.size.toLong(),
        )
    }

    private fun testNonTrivialIdentifierValueFilterOnAggregatedLevel(
        frameworks: List<BulkDataRequest.ListOfFrameworkNames>,
        reportingPeriods: List<String>,
        identifiersToRecognizeMap: Map<IdentifierType, String>,
        differentLei: String,
    ) {
        val aggregatedDataRequestsWithoutFilter = requestControllerApi.getAggregatedDataRequests(identifierValue = null)
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequestsWithoutFilter, frameworks, reportingPeriods,
            identifiersToRecognizeMap + mapOf(IdentifierType.lei to differentLei), 1,
        )
        val aggregatedDataRequestsForEmptyString = requestControllerApi.getAggregatedDataRequests(identifierValue = "")
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequestsForEmptyString, frameworks, reportingPeriods,
            identifiersToRecognizeMap + mapOf(IdentifierType.lei to differentLei), 1,
        )
    }
    /**toDo permId is part of other Ids, which leads to multiple matches -> solved by new logic?
    @Test
    fun `post bulk data request and check that filter for the identifier value on aggregated level works properly`() {
        val permId = generateRandomPermId(10)
        val identifiersToRecognizeMap = mapOf(
            IdentifierType.permId to permId,
            IdentifierType.lei to permId + generateRandomLei().substring(10),
            IdentifierType.isin to generateRandomIsin().substring(0, 2) + permId,
        )
        val differentLei = generateRandomLei()
        val identifiers = identifiersToRecognizeMap + mapOf(IdentifierType.lei to differentLei)
        val frameworks = listOf(BulkDataRequest.ListOfFrameworkNames.lksg)
        val reportingPeriods = listOf("2023")
        generateCompaniesWithOneRandomValueForEachIdentifierType(identifiers)
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiers.values.toList(), frameworks, reportingPeriods),
        )
        logger.info("identifiersToRecognizeMap " + identifiersToRecognizeMap)
        logger.info("identifiers " + identifiers)
        logger.info("response " + response)
        checkThatAllIdentifiersWereAccepted(response, identifiers.values.toList().size, 0)
        val aggregatedDataRequests = requestControllerApi.getAggregatedDataRequests(identifierValue = permId)
        iterateThroughFrameworksReportingPeriodsAndIdentifiersAndCheckAggregationWithCount(
            aggregatedDataRequests, frameworks, reportingPeriods, identifiersToRecognizeMap, 1,
        )
        assertFalse(aggregatedDataRequests.any { it.datalandCompanyId == differentLei })
        testNonTrivialIdentifierValueFilterOnAggregatedLevel(
            frameworks, reportingPeriods, identifiersToRecognizeMap, differentLei,
        )
    }
    */
    private fun checkIfSetAndGetCompanyIdsAreTheSame (
        setCompanyCompanyID: String,
        getCompanyCompanyID: String,
    ) {
        Assertions.assertEquals(
            setCompanyCompanyID,
            getCompanyCompanyID,
            "The received company Ids ${setCompanyCompanyID} and ${setCompanyCompanyID} do not match",
        )
    }

    private fun checkAggregationForNonTrivialFrameworkFilter(
        frameworks: List<BulkDataRequest.ListOfFrameworkNames>,
        reportingPeriods: List<String>,
        identifierMap: Map<IdentifierType, String>,
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
        val identifierMap = mapOf(IdentifierType.lei to generateRandomLei())
        generateCompaniesWithOneRandomValueForEachIdentifierType(identifierMap)
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifierMap.values.toList(), frameworks, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(response, identifierMap.size, 0)
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
        val identifierMap = mapOf(IdentifierType.lei to generateRandomLei())
        val frameworks = listOf(BulkDataRequest.ListOfFrameworkNames.lksg)
        val reportingPeriods = listOf("2020", "2021", "2022", "2023")
        generateCompaniesWithOneRandomValueForEachIdentifierType(identifierMap)
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifierMap.values.toList(), frameworks, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(response, identifierMap.size, 0)
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
    fun `post bulk data request and verify that only unique identifier are accepted `() {
        val permId = generateRandomPermId(20)
        val leiId = permId
        val identifiersMap = mapOf(
            IdentifierType.permId to permId,
            IdentifierType.lei to leiId,)
        val frameworks = listOf(BulkDataRequest.ListOfFrameworkNames.lksg)
        val reportingPeriods = listOf("2023")
        val companyOne = CompanyInformation(
            companyName = "companyOne",
            headquarters = "HQ",
            identifiers = mapOf(IdentifierType.permId.value to listOf(permId)),
            countryCode = "DE",
        )
        val companyTwo = companyOne.copy(
            companyName = "companyTwo",
            identifiers = mapOf(IdentifierType.lei.value  to listOf(leiId))
        )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.companyDataControllerApi.postCompany(companyOne)
        apiAccessor.companyDataControllerApi.postCompany(companyTwo)

        val clientException = causeClientExceptionByBulkDataRequest(identifiersMap.values.toList(), frameworks,
            reportingPeriods)
        checkErrorMessageForInvalidIdentifiersInBulkRequest(clientException)

    }
}

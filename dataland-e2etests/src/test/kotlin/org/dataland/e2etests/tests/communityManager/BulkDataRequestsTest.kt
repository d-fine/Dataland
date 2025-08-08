package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.model.BulkDataRequest
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.communityManager.checkThatDataRequestExistsExactlyOnceInRecentlyStored
import org.dataland.e2etests.utils.communityManager.checkThatTheAmountOfNewlyStoredRequestsIsAsExpected
import org.dataland.e2etests.utils.communityManager.checkThatTheNumberOfAcceptedDataRequestsIsAsExpected
import org.dataland.e2etests.utils.communityManager.checkThatTheNumberOfAlreadyExistingDatasetsIsAsExpected
import org.dataland.e2etests.utils.communityManager.checkThatTheNumberOfAlreadyExistingNonFinalRequestsIsAsExpected
import org.dataland.e2etests.utils.communityManager.checkThatTheNumberOfRejectedCompanyIdentifiersIsAsExpected
import org.dataland.e2etests.utils.communityManager.generateCompaniesWithOneRandomValueForEachIdentifierType
import org.dataland.e2etests.utils.communityManager.generateMapWithOneRandomValueForEachIdentifierType
import org.dataland.e2etests.utils.communityManager.generateRandomIsin
import org.dataland.e2etests.utils.communityManager.generateRandomLei
import org.dataland.e2etests.utils.communityManager.generateRandomPermId
import org.dataland.e2etests.utils.communityManager.getIdForUploadedCompanyWithIdentifiers
import org.dataland.e2etests.utils.communityManager.getNewlyStoredRequestsAfterTimestamp
import org.dataland.e2etests.utils.communityManager.getUniqueDatalandCompanyIdForIdentifierValue
import org.dataland.e2etests.utils.communityManager.retrieveDataRequestIdForReportingPeriodAndUpdateStatus
import org.dataland.e2etests.utils.communityManager.retrieveTimeAndWaitOneMillisecond
import org.dataland.e2etests.utils.communityManager.sendBulkRequestWithEmptyInputAndCheckErrorMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BulkDataRequestsTest {
    val jwtHelper = JwtAuthenticationHelper()
    val apiAccessor = ApiAccessor()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

    @BeforeAll
    fun authenticateAsReader() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
    }

    @Test
    fun `post bulk data request for all frameworks and different valid identifiers and check stored requests`() {
        val uniqueIdentifiersMap = generateMapWithOneRandomValueForEachIdentifierType()
        val identifiers = uniqueIdentifiersMap.values.toSet()
        val dataTypes = enumValues<BulkDataRequest.DataTypes>().toSet()
        val reportingPeriods = setOf("2022", "2023")
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        generateCompaniesWithOneRandomValueForEachIdentifierType(uniqueIdentifiersMap)
        val response =
            requestControllerApi.postBulkDataRequest(
                BulkDataRequest(identifiers, dataTypes, reportingPeriods, notifyMeImmediately = false),
            )
        checkThatTheNumberOfAcceptedDataRequestsIsAsExpected(
            response,
            identifiers.size * dataTypes.size * reportingPeriods.size,
        )
        checkThatTheNumberOfAlreadyExistingNonFinalRequestsIsAsExpected(response, 0)
        checkThatTheNumberOfAlreadyExistingDatasetsIsAsExpected(response, 0)
        checkThatTheNumberOfRejectedCompanyIdentifiersIsAsExpected(response, 0)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(
            newlyStoredRequests, identifiers.size * dataTypes.size * reportingPeriods.size,
        )
        val randomUniqueDataRequestCompanyIdentifierType = uniqueIdentifiersMap.keys.random()
        uniqueIdentifiersMap[randomUniqueDataRequestCompanyIdentifierType]?.let {
            checkThatDataRequestExistsExactlyOnceInRecentlyStored(
                newlyStoredRequests, dataTypes.random().value, reportingPeriods.random(),
                getUniqueDatalandCompanyIdForIdentifierValue(it),
            )
        }
    }

    @Test
    fun `post a bulk data request with at least one invalid identifier and check that this gives no stored request`() {
        val uniqueIdentifiersMap = generateMapWithOneRandomValueForEachIdentifierType()
        val validIdentifiers = uniqueIdentifiersMap.values.toSet()
        val invalidIdentifiers =
            setOf(
                generateRandomLei() + "F", generateRandomIsin() + "F", generateRandomPermId() + "F",
            )
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        generateCompaniesWithOneRandomValueForEachIdentifierType(uniqueIdentifiersMap)
        val response =
            requestControllerApi.postBulkDataRequest(
                BulkDataRequest(
                    validIdentifiers + invalidIdentifiers,
                    setOf(BulkDataRequest.DataTypes.lksg),
                    setOf("2023"),
                    false,
                ),
            )
        checkThatTheNumberOfAcceptedDataRequestsIsAsExpected(response, validIdentifiers.size)
        checkThatTheNumberOfAlreadyExistingNonFinalRequestsIsAsExpected(response, 0)
        checkThatTheNumberOfAlreadyExistingDatasetsIsAsExpected(response, 0)
        checkThatTheNumberOfRejectedCompanyIdentifiersIsAsExpected(response, invalidIdentifiers.size)
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
        val identifiersForBulkRequest =
            setOf(
                leiForCompany, isinForCompany, identifierValueForUnknownCompany,
            )
        val frameworksForBulkRequest = listOf(BulkDataRequest.DataTypes.lksg)
        val reportingPeriod = "2023"
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val response =
            requestControllerApi.postBulkDataRequest(
                BulkDataRequest(
                    identifiersForBulkRequest,
                    frameworksForBulkRequest.toSet(),
                    setOf(reportingPeriod),
                    notifyMeImmediately = false,
                ),
            )
        checkThatTheNumberOfAcceptedDataRequestsIsAsExpected(response, 1)
        checkThatTheNumberOfAlreadyExistingNonFinalRequestsIsAsExpected(response, 0)
        checkThatTheNumberOfAlreadyExistingDatasetsIsAsExpected(response, 0)
        checkThatTheNumberOfRejectedCompanyIdentifiersIsAsExpected(response, 1)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(
            newlyStoredRequests,
            (identifiersForBulkRequest.size - 2) * frameworksForBulkRequest.size,
        )
        checkThatDataRequestExistsExactlyOnceInRecentlyStored(
            newlyStoredRequests, frameworksForBulkRequest[0].value, reportingPeriod, companyId,
        )
    }

    @Test
    fun `post a bulk data request with and check that duplicates are only stored if previous in final status`() {
        val leiForCompany = generateRandomLei()
        val isinForCompany = generateRandomIsin()
        apiAccessor.uploadOneCompanyWithIdentifiers(lei = leiForCompany, isins = listOf(isinForCompany))
        val reportingPeriods = setOf("2021", "2022", "2023")
        val bulkDataRequest =
            BulkDataRequest(
                setOf(leiForCompany, isinForCompany),
                setOf(BulkDataRequest.DataTypes.lksg),
                reportingPeriods,
                notifyMeImmediately = false,
            )
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val response = requestControllerApi.postBulkDataRequest(bulkDataRequest)
        checkThatTheNumberOfAcceptedDataRequestsIsAsExpected(response, reportingPeriods.size)
        checkThatTheNumberOfAlreadyExistingNonFinalRequestsIsAsExpected(response, 0)
        checkThatTheNumberOfAlreadyExistingDatasetsIsAsExpected(response, 0)
        checkThatTheNumberOfRejectedCompanyIdentifiersIsAsExpected(response, 0)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(newlyStoredRequests, reportingPeriods.size)
        retrieveDataRequestIdForReportingPeriodAndUpdateStatus(newlyStoredRequests, "2022", RequestStatus.Answered)
        retrieveDataRequestIdForReportingPeriodAndUpdateStatus(newlyStoredRequests, "2023", RequestStatus.Resolved)
        val timestampBeforeDuplicates = retrieveTimeAndWaitOneMillisecond()
        val responseAfterDuplicates = requestControllerApi.postBulkDataRequest(bulkDataRequest)
        checkThatTheNumberOfAcceptedDataRequestsIsAsExpected(responseAfterDuplicates, 0)
        checkThatTheNumberOfAlreadyExistingNonFinalRequestsIsAsExpected(responseAfterDuplicates, 3)
        checkThatTheNumberOfAlreadyExistingDatasetsIsAsExpected(responseAfterDuplicates, 0)
        checkThatTheNumberOfRejectedCompanyIdentifiersIsAsExpected(responseAfterDuplicates, 0)
        val newlyStoredRequestsAfterDuplicates = getNewlyStoredRequestsAfterTimestamp(timestampBeforeDuplicates)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(newlyStoredRequestsAfterDuplicates, 0)
        assertEquals(
            "2023",
            newlyStoredRequestsAfterDuplicates[0].reportingPeriod,
            "The reporting period of the one newly stored request is not as expected.",
        )
    }

    @Test
    fun `check the expected exception is thrown when frameworks are empty or identifiers are empty`() {
        val validIdentifiers = setOf(generateRandomLei(), generateRandomIsin(), generateRandomPermId())
        val dataTypes = enumValues<BulkDataRequest.DataTypes>().toSet()
        val reportingPeriods = setOf("2023")
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(validIdentifiers, dataTypes, emptySet())
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(validIdentifiers, emptySet(), reportingPeriods)
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(emptySet(), dataTypes, reportingPeriods)
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(validIdentifiers, emptySet(), emptySet())
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(emptySet(), dataTypes, emptySet())
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(emptySet(), emptySet(), reportingPeriods)
        sendBulkRequestWithEmptyInputAndCheckErrorMessage(emptySet(), emptySet(), emptySet())
    }
}

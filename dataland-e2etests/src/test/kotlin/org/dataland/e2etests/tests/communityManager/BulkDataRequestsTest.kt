package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.model.BulkDataRequest
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.communitymanager.openApiClient.model.StoredDataRequest
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.causeClientExceptionByBulkDataRequest
import org.dataland.e2etests.utils.checkErrorMessageForAmbivalentIdentifiersInBulkRequest
import org.dataland.e2etests.utils.checkErrorMessageForInvalidIdentifiersInBulkRequest
import org.dataland.e2etests.utils.checkThatAllIdentifiersWereAccepted
import org.dataland.e2etests.utils.checkThatMessageIsAsExpected
import org.dataland.e2etests.utils.checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce
import org.dataland.e2etests.utils.checkThatTheAmountOfNewlyStoredRequestsIsAsExpected
import org.dataland.e2etests.utils.checkThatTheNumberOfAcceptedIdentifiersIsAsExpected
import org.dataland.e2etests.utils.checkThatTheNumberOfRejectedIdentifiersIsAsExpected
import org.dataland.e2etests.utils.generateCompaniesWithOneRandomValueForEachIdentifierType
import org.dataland.e2etests.utils.generateMapWithOneRandomValueForEachIdentifierType
import org.dataland.e2etests.utils.generateRandomIsin
import org.dataland.e2etests.utils.generateRandomLei
import org.dataland.e2etests.utils.generateRandomPermId
import org.dataland.e2etests.utils.getIdForUploadedCompanyWithIdentifiers
import org.dataland.e2etests.utils.getNewlyStoredRequestsAfterTimestamp
import org.dataland.e2etests.utils.getUniqueDatalandCompanyIdForIdentifierValue
import org.dataland.e2etests.utils.patchDataRequestAndAssertNewStatusAndLastModifiedUpdated
import org.dataland.e2etests.utils.retrieveTimeAndWaitOneMillisecond
import org.dataland.e2etests.utils.sendBulkRequestWithEmptyInputAndCheckErrorMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BulkDataRequestsTest {

    val jwtHelper = JwtAuthenticationHelper()
    val apiAccessor = ApiAccessor()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

    @BeforeAll
    fun authenticateAsReader() { jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader) }

    @Test
    fun `post bulk data request for all frameworks and different valid identifiers and check stored requests`() {
        val uniqueIdentifiersMap = generateMapWithOneRandomValueForEachIdentifierType()
        val identifiers = uniqueIdentifiersMap.values.toSet()
        val dataTypes = enumValues<BulkDataRequest.DataTypes>().toSet()
        val reportingPeriods = setOf("2022", "2023")
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        generateCompaniesWithOneRandomValueForEachIdentifierType(uniqueIdentifiersMap)
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiers, dataTypes, reportingPeriods),
        )
        checkThatAllIdentifiersWereAccepted(response, identifiers.size, 0)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(
            newlyStoredRequests, identifiers.size * dataTypes.size * reportingPeriods.size,
        )
        val randomUniqueDataRequestCompanyIdentifierType = uniqueIdentifiersMap.keys.random()
        uniqueIdentifiersMap[randomUniqueDataRequestCompanyIdentifierType]?.let {
            checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce(
                newlyStoredRequests, dataTypes.random().value, reportingPeriods.random(),
                getUniqueDatalandCompanyIdForIdentifierValue(it),
            )
        }
    }

    @Test
    fun `post a bulk data request with at least one invalid identifier and check that this gives no stored request`() {
        val uniqueIdentifiersMap = generateMapWithOneRandomValueForEachIdentifierType()
        val validIdentifiers = uniqueIdentifiersMap.values.toSet()
        val invalidIdentifiers = setOf(
            generateRandomLei() + "F", generateRandomIsin() + "F", generateRandomPermId() + "F",
        )
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        generateCompaniesWithOneRandomValueForEachIdentifierType(uniqueIdentifiersMap)
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(
                validIdentifiers + invalidIdentifiers,
                setOf(BulkDataRequest.DataTypes.lksg),
                setOf("2023"),
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
        val identifiersForBulkRequest = setOf(
            leiForCompany, isinForCompany, identifierValueForUnknownCompany,
        )
        val frameworksForBulkRequest = listOf(BulkDataRequest.DataTypes.lksg)
        val reportingPeriod = "2023"
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val response = requestControllerApi.postBulkDataRequest(
            BulkDataRequest(identifiersForBulkRequest, frameworksForBulkRequest.toSet(), setOf(reportingPeriod)),
        )
        checkThatAllIdentifiersWereAccepted(response, (identifiersForBulkRequest.size - 1), 1)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(
            newlyStoredRequests,
            (identifiersForBulkRequest.size - 2) * frameworksForBulkRequest.size,
        )
        checkThatRequestForFrameworkReportingPeriodAndIdentifierExistsExactlyOnce(
            newlyStoredRequests, frameworksForBulkRequest[0].value, reportingPeriod, companyId,
        )
    }

    @Test
    fun `post a bulk data request with and check that duplicates are only stored if previous in final status`() {
        val leiForCompany = generateRandomLei()
        val isinForCompany = generateRandomIsin()
        apiAccessor.uploadOneCompanyWithIdentifiers(lei = leiForCompany, isins = listOf(isinForCompany))
        val reportingPeriods = setOf("2021", "2022", "2023")
        val bulkDataRequest = BulkDataRequest(
            setOf(leiForCompany, isinForCompany),
            setOf(BulkDataRequest.DataTypes.lksg),
            reportingPeriods,
        )
        val timestampBeforeBulkRequest = retrieveTimeAndWaitOneMillisecond()
        val response = requestControllerApi.postBulkDataRequest(bulkDataRequest)
        checkThatAllIdentifiersWereAccepted(response, 2, 0)
        val newlyStoredRequests = getNewlyStoredRequestsAfterTimestamp(timestampBeforeBulkRequest)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(newlyStoredRequests, reportingPeriods.size)
        retrieveDataRequestIdForReportingPeriodAndUpdateStatus(newlyStoredRequests, "2022", RequestStatus.answered)
        retrieveDataRequestIdForReportingPeriodAndUpdateStatus(newlyStoredRequests, "2023", RequestStatus.closed)
        val timestampBeforeDuplicates = retrieveTimeAndWaitOneMillisecond()
        val responseAfterDuplicates = requestControllerApi.postBulkDataRequest(bulkDataRequest)
        checkThatAllIdentifiersWereAccepted(responseAfterDuplicates, 2, 0)
        val newlyStoredRequestsAfterDuplicates = getNewlyStoredRequestsAfterTimestamp(timestampBeforeDuplicates)
        checkThatTheAmountOfNewlyStoredRequestsIsAsExpected(newlyStoredRequestsAfterDuplicates, 1)
        assertEquals(
            "2023",
            newlyStoredRequestsAfterDuplicates[0].reportingPeriod,
            "The reporting period of the one newly stored request is not as expected.",
        )
    }

    fun retrieveDataRequestIdForReportingPeriodAndUpdateStatus(
        dataRequests: List<StoredDataRequest>,
        reportingPeriod: String,
        newStatus: RequestStatus,
    ) {
        val dataRequestId = UUID.fromString(
            dataRequests.filter { it.reportingPeriod == reportingPeriod }[0].dataRequestId,
        )
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, newStatus)
    }

    @Test
    fun `check the expected exception is thrown when frameworks are empty or identifiers are empty or invalid only`() {
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
        val invalidIdentifiers = setOf(
            generateRandomLei() + "F", generateRandomIsin() + "F", generateRandomPermId() + "F",
        )
        val clientException = causeClientExceptionByBulkDataRequest(invalidIdentifiers, dataTypes, reportingPeriods)
        checkErrorMessageForInvalidIdentifiersInBulkRequest(clientException)
    }

    @Test
    fun `post bulk data request and verify that only unique identifiers are accepted `() {
        val permId = generateRandomPermId(20)
        val leiId = permId
        val identifiersMap = mapOf(
            IdentifierType.permId to permId,
            IdentifierType.lei to leiId,
        )
        val frameworks = setOf(BulkDataRequest.DataTypes.lksg)
        val reportingPeriods = setOf("2023")
        val companyOne = CompanyInformation(
            companyName = "companyOne",
            headquarters = "HQ",
            identifiers = mapOf(IdentifierType.permId.value to listOf(permId)),
            countryCode = "DE",
        )
        val companyTwo = companyOne.copy(
            companyName = "companyTwo",
            identifiers = mapOf(IdentifierType.lei.value to listOf(leiId)),
        )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.companyDataControllerApi.postCompany(companyOne)
        apiAccessor.companyDataControllerApi.postCompany(companyTwo)

        val clientException = causeClientExceptionByBulkDataRequest(
            identifiersMap.values.toSet(), frameworks,
            reportingPeriods,
        )
        checkErrorMessageForAmbivalentIdentifiersInBulkRequest(clientException)
    }
}

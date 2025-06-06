package org.dataland.e2etests.tests

import org.dataland.datalandapikeymanager.openApiClient.model.ApiKeyMetaInfo
import org.dataland.datalandapikeymanager.openApiClient.model.RevokeApiKeyResponse
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyNonFinancialsData
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandbackendutils.utils.JsonComparator
import org.dataland.e2etests.MAX_NUMBER_OF_DAYS_SELECTABLE_FOR_API_KEY_VALIDITY
import org.dataland.e2etests.auth.ApiKeyAuthenticationHelper
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DatesHandler
import org.dataland.e2etests.utils.DocumentControllerApiAccessor
import org.dataland.e2etests.utils.assertEqualsByJsonComparator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.dataland.datalandapikeymanager.openApiClient.infrastructure.ClientException as ApiKeyManagerClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException as BackendClientException

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataRetrievalViaApiKeyTest {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentControllerApiAccessor()

    private val apiKeyHelper = ApiKeyAuthenticationHelper()

    private val datesHandler = DatesHandler()

    @BeforeAll
    fun postTestDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    private fun buildApiKeyMetaInfoForFailedValidation(validationMessage: String): ApiKeyMetaInfo =
        ApiKeyMetaInfo(null, null, null, false, validationMessage)

    private fun assertUserId(
        technicalUser: TechnicalUser,
        receivedApiKeyMetaInfoFromValidation: ApiKeyMetaInfo,
    ) {
        val expectedUserIdForUserType = technicalUser.technicalUserId
        val userIdInReceivedApiKeyMetaInfo = receivedApiKeyMetaInfoFromValidation.keycloakUserId
        assertEquals(
            expectedUserIdForUserType,
            userIdInReceivedApiKeyMetaInfo,
            "The Keycloak user ID in the received API key meta info was $userIdInReceivedApiKeyMetaInfo and " +
                "does not equal the expected Keycloak user ID $expectedUserIdForUserType for the technical " +
                "user type $technicalUser.",
        )
    }

    private fun assertRoles(
        technicalUser: TechnicalUser,
        receivedApiKeyMetaInfoFromValidation: ApiKeyMetaInfo,
    ) {
        val expectedRolesForUserType = technicalUser.roles
        val rolesInReceivedApiKeyMetaInfo = receivedApiKeyMetaInfoFromValidation.keycloakRoles
        assertEquals(
            expectedRolesForUserType,
            rolesInReceivedApiKeyMetaInfo,
            "The Keycloak roles in the received API key meta info were $rolesInReceivedApiKeyMetaInfo and " +
                "do not equal the expected Keycloak roles $expectedRolesForUserType for the technical" +
                "user type $technicalUser",
        )
    }

    private fun assertExpiryDate(
        daysValid: Int? = null,
        receivedApiKeyMetaInfoFromValidation: ApiKeyMetaInfo,
    ) {
        val expectedExpiryDateForApiKey = datesHandler.calculateExpectedExpiryDateSimpleFormatted(daysValid)
        val expiryDateInReceivedApiKeyMetaInfo =
            datesHandler.convertUnixTimeToSimpleFormattedDate(
                receivedApiKeyMetaInfoFromValidation.expiryDate,
            )
        assertEquals(
            expectedExpiryDateForApiKey,
            expiryDateInReceivedApiKeyMetaInfo,
            "The expiry date in the received API key meta info was $expiryDateInReceivedApiKeyMetaInfo and " +
                "does not equal the expected expiry date $expectedExpiryDateForApiKey.",
        )
    }

    private fun doAssertionsAfterApiKeyValidation(
        technicalUser: TechnicalUser,
        daysValid: Int? = null,
        receivedApiKeyMetaInfoFromValidation: ApiKeyMetaInfo,
    ) {
        assertUserId(technicalUser, receivedApiKeyMetaInfoFromValidation)
        assertRoles(technicalUser, receivedApiKeyMetaInfoFromValidation)
        assertExpiryDate(daysValid, receivedApiKeyMetaInfoFromValidation)
        assertTrue(receivedApiKeyMetaInfoFromValidation.active!!)
        assertEquals(
            "The API key you provided was successfully validated.",
            receivedApiKeyMetaInfoFromValidation.validationMessage,
            "The received validation message does not match the expected one.",
        )
    }

    @AfterEach
    fun `delete the API key from Backend client to ensure clean state`() {
        GlobalAuth.setBearerToken(null)
    }

    @Test
    fun `create a non teaser company generate an API key and get the non teaser company with it`() {
        val uploadInfo = apiAccessor.uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(false)
        val companyId = uploadInfo.actualStoredCompany.companyId
        val expectedStoredCompany = StoredCompany(companyId, uploadInfo.inputCompanyInformation, emptyList())

        apiKeyHelper.authenticateApiCallsWithApiKeyForTechnicalUser(TechnicalUser.Reader, 1)
        val downloadedStoredCompany = apiAccessor.companyDataControllerApi.getCompanyById(companyId)

        assertEquals(
            expectedStoredCompany.copy(
                companyInformation =
                    expectedStoredCompany
                        .companyInformation
                        .copy(
                            companyContactDetails =
                                expectedStoredCompany
                                    .companyInformation.companyContactDetails
                                    ?.sorted(),
                        ),
            ),
            downloadedStoredCompany,
            "The received company $downloadedStoredCompany does not equal the expected company $expectedStoredCompany",
        )
    }

    @Test
    fun `create a non teaser company upload framework data for it generate an API key and get the data with it`() {
        val testDataEuTaxonomyNonFinancials =
            apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
                .getTData(1)
                .first()
        val testCompanyInformationNonTeaser =
            apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
                .getCompanyInformationWithoutIdentifiers(1)
                .first()
                .copy(isTeaserCompany = false)
        val mapOfIds =
            apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
                testCompanyInformationNonTeaser,
                testDataEuTaxonomyNonFinancials,
            )
        apiKeyHelper.authenticateApiCallsWithApiKeyForTechnicalUser(TechnicalUser.Reader, 1)
        val downloadedCompanyAssociatedEuTaxoDataNonFinancials =
            apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
                .getCompanyAssociatedEutaxonomyNonFinancialsData(mapOfIds.getValue("dataId"))

        val ignoredKeys = setOf("publicationDate")
        assertEqualsByJsonComparator(
            CompanyAssociatedDataEutaxonomyNonFinancialsData(
                companyId = mapOfIds.getValue("companyId"),
                reportingPeriod = "", data = testDataEuTaxonomyNonFinancials,
            ),
            downloadedCompanyAssociatedEuTaxoDataNonFinancials,
            JsonComparator.JsonComparisonOptions(ignoredKeys),
        )
    }

    @Test
    fun `create a non teaser company get it with a valid API key revoke the API key and try to get the company`() {
        val technicalUser = TechnicalUser.Reader
        val uploadInfo = apiAccessor.uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(false)
        val companyId = uploadInfo.actualStoredCompany.companyId
        val expectedStoredCompany = StoredCompany(companyId, uploadInfo.inputCompanyInformation, emptyList())

        val apiKey = apiKeyHelper.obtainApikeyForTechnicalUser(technicalUser, 1)
        GlobalAuth.setBearerToken(apiKey.apiKey)
        val downloadedStoredCompany = apiAccessor.companyDataControllerApi.getCompanyById(companyId)
        assertEquals(
            expectedStoredCompany.copy(
                companyInformation =
                    expectedStoredCompany
                        .companyInformation
                        .copy(
                            companyContactDetails =
                                expectedStoredCompany
                                    .companyInformation.companyContactDetails
                                    ?.sorted(),
                        ),
            ),
            downloadedStoredCompany,
        )

        apiKeyHelper.revokeApiKeyForTechnicalUserAndResetAuthentication(technicalUser)
        GlobalAuth.setBearerToken(apiKey.apiKey)
        val exception =
            assertThrows<BackendClientException> {
                apiAccessor.companyDataControllerApi.getCompanyById(companyId).companyId
            }
        assertEquals(
            "Client error : 401 ", exception.message,
        )
    }

    @Test
    fun `generate an API key with the current max value for daysValid then validate it`() {
        val daysValidMax = MAX_NUMBER_OF_DAYS_SELECTABLE_FOR_API_KEY_VALIDITY
        val technicalUser = TechnicalUser.Reader
        val apiKeyToValidate =
            apiKeyHelper
                .authenticateApiCallsWithApiKeyForTechnicalUser(technicalUser, daysValidMax)
                .apiKey
        val apiKeyMetaInfo = apiKeyHelper.resetAuthenticationAndValidateApiKey(apiKeyToValidate)
        doAssertionsAfterApiKeyValidation(technicalUser, daysValidMax, apiKeyMetaInfo)
    }

    @Test
    fun `generate an API key with a too small value for daysValid and assert that exception is thrown`() {
        val daysValidTooSmall = 0
        val technicalUser = TechnicalUser.Reader
        val exception =
            assertThrows<ApiKeyManagerClientException> {
                apiKeyHelper.obtainApikeyForTechnicalUser(technicalUser, daysValidTooSmall)
            }
        assertEquals(
            "Client error : 400 ",
            exception.message,
        )
    }

    @Test
    fun `generate an API key with a too large value for daysValid and assert that exception is thrown`() {
        val daysValidTooLarge = MAX_NUMBER_OF_DAYS_SELECTABLE_FOR_API_KEY_VALIDITY + 1
        val technicalUser = TechnicalUser.Reader
        val exception =
            assertThrows<ApiKeyManagerClientException> {
                apiKeyHelper.obtainApikeyForTechnicalUser(technicalUser, daysValidTooLarge)
            }
        assertEquals(
            "Client error : 400 ",
            exception.message,
        )
    }

    @Test
    fun `generate an API key which is valid forever then validate it`() {
        val daysValid = null
        val technicalUser = TechnicalUser.Reader
        val apiKeyToValidate =
            apiKeyHelper
                .authenticateApiCallsWithApiKeyForTechnicalUser(technicalUser, daysValid)
                .apiKey
        val apiKeyMetaInfo = apiKeyHelper.resetAuthenticationAndValidateApiKey(apiKeyToValidate)
        doAssertionsAfterApiKeyValidation(technicalUser, daysValid, apiKeyMetaInfo)
    }

    @Test
    fun `validate a non existing API key`() {
        val technicalUser = TechnicalUser.Reader
        val apiKeyToRevokeAndValidate =
            apiKeyHelper
                .authenticateApiCallsWithApiKeyForTechnicalUser(technicalUser, 1)
                .apiKey
        apiKeyHelper.revokeApiKeyForTechnicalUserAndResetAuthentication(technicalUser)
        val apiKeyMetaInfo = apiKeyHelper.resetAuthenticationAndValidateApiKey(apiKeyToRevokeAndValidate)
        val expectedValidationMessage = "Your Dataland account has no API key registered. Please generate one."
        val expectedApiKeyMetaInfo = buildApiKeyMetaInfoForFailedValidation(expectedValidationMessage)
        assertEquals(
            expectedApiKeyMetaInfo,
            apiKeyMetaInfo,
            "Message for a non existing API key was not as expected",
        )
    }

    @Test
    fun `validate an API key which has the right format but a wrong secret `() {
        apiKeyHelper.authenticateApiCallsWithApiKeyForTechnicalUser(TechnicalUser.Reader, 1)
        val apiKeyWithWrongSecret =
            "MThiNjdlY2MtMTE3Ni00NTA2LTg0MTQtMWU4MTY2MTAxN2Nh_" +
                "f7d037b92dd8c15022a9761853bcd88d014aab6d34c53705d61d6174a4589ee464c5adee09c9494e_3573499914"
        val apiKeyMetaInfo = apiKeyHelper.resetAuthenticationAndValidateApiKey(apiKeyWithWrongSecret)
        val expectedValidationMessage = "The API key you provided for your Dataland account is not correct."
        val expectedApiKeyMetaInfo = buildApiKeyMetaInfoForFailedValidation(expectedValidationMessage)
        assertEquals(
            expectedApiKeyMetaInfo,
            apiKeyMetaInfo,
            "Message for an API key with a wrong secret was not as expected",
        )
    }

    @Test
    fun `generate an API key and then revoke it`() {
        val technicalUser = TechnicalUser.Reader
        apiKeyHelper.authenticateApiCallsWithApiKeyForTechnicalUser(technicalUser, 1)
        val actualRevokeResponse = apiKeyHelper.revokeApiKeyForTechnicalUserAndResetAuthentication(technicalUser)
        val expectedRevokeMessage = "The API key for your Dataland account was successfully revoked."
        val expectedRevokeResponse = RevokeApiKeyResponse(true, expectedRevokeMessage)
        assertEquals(
            expectedRevokeResponse,
            actualRevokeResponse,
            "The API key was somehow not successfully revoked.",
        )
    }

    @Test
    fun `generate an API key revoke it once so that it is gone then revoke it a second time`() {
        val technicalUser = TechnicalUser.Reader
        apiKeyHelper.authenticateApiCallsWithApiKeyForTechnicalUser(technicalUser, 1)
        apiKeyHelper.revokeApiKeyForTechnicalUserAndResetAuthentication(technicalUser)
        val actualRevokeResponse = apiKeyHelper.revokeApiKeyForTechnicalUserAndResetAuthentication(technicalUser)
        val expectedRevokeMessage =
            "Your Dataland account has no API key registered. " +
                "Therefore no revokement took place."
        val expectedRevokeResponse = RevokeApiKeyResponse(false, expectedRevokeMessage)
        assertEquals(
            expectedRevokeResponse,
            actualRevokeResponse,
            "Revoking the API key somehow did not result in a fail message.",
        )
    }

    @Test
    fun `generate an API key per technical user and get the meta info about that API key for that user`() {
        TechnicalUser.entries.forEach { userType ->
            val apiKeyAndMetaInfo = apiKeyHelper.authenticateApiCallsWithApiKeyForTechnicalUser(userType)
            val apiKeyMetaInfoFromEndpoint = apiKeyHelper.getApiKeyMetaInformationForTechnicalUser(userType)
            assertEquals(
                apiKeyAndMetaInfo.apiKeyMetaInfo,
                apiKeyMetaInfoFromEndpoint,
                "The API key meta info from the generation process does not equal the API key meta info that is " +
                    "returned for the respective keycloak user.",
            )
        }
    }
}

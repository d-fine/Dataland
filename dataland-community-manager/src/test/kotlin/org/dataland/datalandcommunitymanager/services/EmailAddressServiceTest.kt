package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmailAddressServiceTest {
    private val mockCompanyDataControllerApi = mock<CompanyDataControllerApi>()
    private val mockKeycloakUserService = mock<KeycloakUserService>()
    private lateinit var emailAddressService: EmailAddressService

    private val subdomain = "my-wonderful-company"
    private val headquarters = "Wonderland"
    private val countryCode = "US"

    private val keycloakUserInfoList =
        listOf(
            KeycloakUserInfo(
                email = "jane.doe@$subdomain.com",
                userId = UUID.randomUUID().toString(),
                firstName = "Jane",
                lastName = "Doe",
            ),
            KeycloakUserInfo(
                email = "john.doe@$subdomain.com",
                userId = UUID.randomUUID().toString(),
                firstName = "John",
                lastName = "Doe",
            ),
        )
    private val keycloakUserInfo = keycloakUserInfoList.first()
    private val knownEmailAddress = keycloakUserInfo.email!!

    private val unknownEmailAddress = "unknown@example.com"
    private val invalidCompanyId = UUID.randomUUID()

    private val companyIdWithNullAssociatedSubdomains = UUID.randomUUID()
    private val storedCompanyWithNullAssociatedSubdomains =
        StoredCompany(
            companyId = companyIdWithNullAssociatedSubdomains.toString(),
            companyInformation =
                CompanyInformation(
                    companyName = "Company with null associatedSubdomains",
                    headquarters = headquarters,
                    identifiers = emptyMap(),
                    countryCode = countryCode,
                    associatedSubdomains = null,
                ),
            dataRegisteredByDataland = emptyList(),
        )

    private val companyIdWithEmptyAssociatedSubdomains = UUID.randomUUID()
    private val storedCompanyWithEmptyAssociatedSubdomains =
        StoredCompany(
            companyId = companyIdWithEmptyAssociatedSubdomains.toString(),
            companyInformation =
                CompanyInformation(
                    companyName = "Company with empty associatedSubdomains",
                    headquarters = headquarters,
                    identifiers = emptyMap(),
                    countryCode = countryCode,
                    associatedSubdomains = emptyList(),
                ),
            dataRegisteredByDataland = emptyList(),
        )

    private val companyIdWithOnlyBlankAssociatedSubdomains = UUID.randomUUID()
    private val storedCompanyWithOnlyBlankAssociatedSubdomains =
        StoredCompany(
            companyId = companyIdWithOnlyBlankAssociatedSubdomains.toString(),
            companyInformation =
                CompanyInformation(
                    companyName = "Company with only blank associated subdomains",
                    headquarters = headquarters,
                    identifiers = emptyMap(),
                    countryCode = countryCode,
                    associatedSubdomains = listOf("   ", ""),
                ),
            dataRegisteredByDataland = emptyList(),
        )

    private val companyIdWithNontrivialAssociatedSubdomains = UUID.randomUUID()
    private val storedCompanyWithNontrivialAssociatedSubdomains =
        StoredCompany(
            companyId = companyIdWithNontrivialAssociatedSubdomains.toString(),
            companyInformation =
                CompanyInformation(
                    companyName = "Company with nontrivial associated subdomains",
                    headquarters = headquarters,
                    identifiers = emptyMap(),
                    countryCode = countryCode,
                    associatedSubdomains = listOf(subdomain),
                ),
            dataRegisteredByDataland = emptyList(),
        )

    @BeforeEach
    fun setUp() {
        reset(
            mockCompanyDataControllerApi,
            mockKeycloakUserService,
        )

        doReturn(null).whenever(mockKeycloakUserService).findUserByEmail(unknownEmailAddress)
        doReturn(keycloakUserInfo).whenever(mockKeycloakUserService).findUserByEmail(knownEmailAddress)
        doReturn(keycloakUserInfoList).whenever(mockKeycloakUserService).searchUsersByEmailSubdomain(subdomain)

        doThrow(ClientException()).whenever(mockCompanyDataControllerApi).getCompanyById(invalidCompanyId.toString())
        doReturn(storedCompanyWithNullAssociatedSubdomains)
            .whenever(mockCompanyDataControllerApi)
            .getCompanyById(companyIdWithNullAssociatedSubdomains.toString())
        doReturn(storedCompanyWithEmptyAssociatedSubdomains)
            .whenever(mockCompanyDataControllerApi)
            .getCompanyById(companyIdWithEmptyAssociatedSubdomains.toString())
        doReturn(storedCompanyWithOnlyBlankAssociatedSubdomains)
            .whenever(mockCompanyDataControllerApi)
            .getCompanyById(companyIdWithOnlyBlankAssociatedSubdomains.toString())
        doReturn(storedCompanyWithNontrivialAssociatedSubdomains)
            .whenever(mockCompanyDataControllerApi)
            .getCompanyById(companyIdWithNontrivialAssociatedSubdomains.toString())

        emailAddressService =
            EmailAddressService(
                mockCompanyDataControllerApi,
                mockKeycloakUserService,
            )
    }

    @Test
    fun `check that an unknown email address results in a ResourceNotFoundApiException`() {
        assertThrows<ResourceNotFoundApiException> {
            emailAddressService.validateEmailAddress(unknownEmailAddress)
        }
    }

    @Test
    fun `check that a known email address is processed as expected`() {
        val returnedKeycloakUserInfo =
            assertDoesNotThrow {
                emailAddressService.validateEmailAddress(knownEmailAddress)
            }

        assertEquals(
            keycloakUserInfo,
            returnedKeycloakUserInfo,
        )
    }

    @Test
    fun `check that an invalid company ID results in a ResourceNotFoundApiException`() {
        assertThrows<ResourceNotFoundApiException> {
            emailAddressService.getUsersByCompanyAssociatedSubdomains(invalidCompanyId)
        }
    }

    @Test
    fun `check that an empty list is returned for a company with null associatedSubdomains`() {
        assertEquals(
            emptyList<KeycloakUserInfo>(),
            emailAddressService.getUsersByCompanyAssociatedSubdomains(companyIdWithNullAssociatedSubdomains),
        )
    }

    @Test
    fun `check that an empty list is returned for a company with empty associatedSubdomains`() {
        assertEquals(
            emptyList<KeycloakUserInfo>(),
            emailAddressService.getUsersByCompanyAssociatedSubdomains(companyIdWithEmptyAssociatedSubdomains),
        )
    }

    @Test
    fun `check that an empty list is returned for a company where all associated subdomains are blank`() {
        assertEquals(
            emptyList<KeycloakUserInfo>(),
            emailAddressService.getUsersByCompanyAssociatedSubdomains(companyIdWithOnlyBlankAssociatedSubdomains),
        )
    }

    @Test
    fun `check that search by associated subdomains works as expected for a company with nontrivial associated subdomains`() {
        assertEquals(
            keycloakUserInfoList,
            emailAddressService.getUsersByCompanyAssociatedSubdomains(companyIdWithNontrivialAssociatedSubdomains),
        )
    }
}

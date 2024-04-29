package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.services.KeycloakUserControllerApiService
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions
import org.mockito.Mockito
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

class DataRequestResonseEmailSenderUtils {
    private val reportingPeriod = "2022"
    private val companyId = "59f05156-e1ba-4ea8-9d1e-d4833f6c7afc"
    private val userId = "1234-221-1111elf"
    private val userEmail = "$userId@example.com"
    private val creationTimestamp = 1709820187875
    private val creationTimestampAsDate = "07 Mar 2024, 15:03"
    private val companyName = "Test Inc."
    fun setupAuthentication() {
        val mockSecurityContext = Mockito.mock(SecurityContext::class.java)
        val authenticationMock = AuthenticationMock.mockJwtAuthentication(
            userEmail,
            userId,
            setOf(DatalandRealmRole.ROLE_USER),
        )
        Mockito.`when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        Mockito.`when`(authenticationMock.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)

    }
    fun getKeycloakControllerApiService(): KeycloakUserControllerApiService {
        val keycloakUserControllerApiService = Mockito.mock(KeycloakUserControllerApiService::class.java)
        Mockito.`when`(keycloakUserControllerApiService.getEmailAddress(userId)).thenReturn(userEmail)
        return keycloakUserControllerApiService
    }
    fun getDataRequestEntityWithDataType(dataType: String): DataRequestEntity {
        return DataRequestEntity(
            userId = userId,
            creationTimestamp = creationTimestamp,
            dataType = dataType,
            reportingPeriod = reportingPeriod,
            datalandCompanyId = companyId,
        )
    }
    fun checkPropertiesOfDataRequestResponseEmail(
        dataRequestId: String,
        properties: Map<String, String?>,
        dataType: String,
        dataTypeDescription: String,
        staleDaysThreshold: String,
    ) {
        Assertions.assertEquals(companyId, properties.getValue("companyId"))
        Assertions.assertEquals(companyName, properties.getValue("companyName"))
        Assertions.assertEquals(dataType, properties.getValue("dataType"))
        Assertions.assertEquals(dataTypeDescription, properties.getValue("dataTypeDescription"))
        Assertions.assertEquals(reportingPeriod, properties.getValue("reportingPeriod"))
        Assertions.assertEquals(creationTimestampAsDate, properties.getValue("creationDate"))
        Assertions.assertEquals(dataRequestId, properties.getValue("dataRequestId"))
        Assertions.assertEquals(staleDaysThreshold, properties.getValue("closedInDays"))
    }
    fun getCompanyDataControllerMock(): CompanyDataControllerApi {
        val companyDataControllerMock = Mockito.mock(CompanyDataControllerApi::class.java)
        Mockito.`when`(companyDataControllerMock.getCompanyInfo(companyId))
            .thenReturn(
                CompanyInformation(
                    companyName = companyName,
                    headquarters = "",
                    identifiers = emptyMap(),
                    countryCode = "",
                ),
            )
        return companyDataControllerMock
    }

    fun getListOfAllDataTypes(): List<List<String>> {
        return listOf(
            listOf("p2p", "WWF Pathways to Paris"),
            listOf("eutaxonomy-financials", "EU Taxonomy for financial companies"),
            listOf("eutaxonomy-non-financials", "EU Taxonomy for non-financial companies"),
            listOf("lksg", "LkSG"),
            listOf("sfdr", "SFDR"),
            listOf("sme", "SME"),
            listOf("esg-questionnaire", "ESG Questionnaire"),
            listOf("heimathafen", "Heimathafen"),
        )
    }
    fun checkUserEmail(receiver : String){
        Assertions.assertEquals(userEmail, receiver)
    }
}

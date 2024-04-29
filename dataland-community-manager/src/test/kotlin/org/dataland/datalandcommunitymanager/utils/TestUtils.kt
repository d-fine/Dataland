package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

class TestUtils {
    private val reportingPeriod = "2022"
    private val companyId = "59f05156-e1ba-4ea8-9d1e-d4833f6c7afc"
    private val userId = "1234-221-1111elf"
    private val creationTimestamp = 1709820187875
    private val creationTimestampAsDate = "07 Mar 2024, 15:03"
    private val companyName = "Test Inc."
    fun mockSecurityContext() {
        val mockAuthentication = AuthenticationMock.mockJwtAuthentication(
            "mocked_uploader",
            "dummy-id",
            setOf(DatalandRealmRole.ROLE_PREMIUM_USER),
        )
        val mockSecurityContext = mock(SecurityContext::class.java)
        `when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
        SecurityContextHolder.setContext(mockSecurityContext)
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
        val companyDataControllerMock = mock(CompanyDataControllerApi::class.java)
        `when`(companyDataControllerMock.getCompanyInfo(companyId))
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
}

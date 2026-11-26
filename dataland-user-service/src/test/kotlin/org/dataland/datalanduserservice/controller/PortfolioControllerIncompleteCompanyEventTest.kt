package org.dataland.datalanduserservice.controller

import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifierValidationResult
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalanduserservice.model.PortfolioUpload
import org.dataland.datalanduserservice.model.enums.NotificationFrequency
import org.dataland.datalanduserservice.service.IncompleteCompanyEventService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class PortfolioControllerIncompleteCompanyEventTest : AbstractPortfolioControllerTest() {
    @MockitoBean
    private lateinit var incompleteCompanyEventService: IncompleteCompanyEventService

    private val newCompanyId = "newCompanyId"

    @BeforeEach
    fun setupEventTest() {
        reset(incompleteCompanyEventService)
        setMockSecurityContext(dummyAdminAuthentication)
        doNothing().whenever(companyDataController).isCompanyIdValid(any())
    }

    private fun uploadBodyWith(companyIds: Set<String>): String =
        defaultObjectMapper.writeValueAsString(
            PortfolioUpload(
                portfolioName = "Event Portfolio",
                identifiers = companyIds,
                isMonitored = false,
                monitoredFrameworks = emptySet(),
                notificationFrequency = NotificationFrequency.Weekly,
                timeWindowThreshold = null,
                sharedUserIds = emptySet(),
            ),
        )

    @Test
    fun `creating a portfolio checks all resolved companies`() {
        whenever(companyDataController.postCompanyValidation(any())).thenReturn(
            listOf(
                CompanyIdentifierValidationResult(
                    identifier = "some-lei",
                    companyInformation =
                        BasicCompanyInformation(
                            companyId = newCompanyId,
                            companyName = "Name",
                            headquarters = "HQ",
                            countryCode = "DE",
                        ),
                ),
            ),
        )

        performCreatePortfolioAndExpect(uploadBodyWith(setOf("some-lei")), MockMvcResultMatchers.status().isCreated)

        verify(incompleteCompanyEventService).publishEventsForIncompleteCompanies(eq(setOf(newCompanyId)), any())
    }

    @Test
    fun `replacing a portfolio checks only newly added companies`() {
        performReplacePortfolioAndExpect(
            uploadBodyWith(setOf(dummyCompanyId, newCompanyId)),
            MockMvcResultMatchers.status().isOk,
        )

        verify(incompleteCompanyEventService).publishEventsForIncompleteCompanies(eq(setOf(newCompanyId)), any())
    }

    @Test
    fun `replacing a portfolio without new companies checks no company`() {
        performReplacePortfolioAndExpect(uploadBodyWith(setOf(dummyCompanyId)), MockMvcResultMatchers.status().isOk)

        verify(incompleteCompanyEventService).publishEventsForIncompleteCompanies(eq(emptySet()), any())
    }

    @Test
    fun `a rejected create does not publish events`() {
        whenever(companyDataController.postCompanyValidation(any())).thenReturn(
            listOf(CompanyIdentifierValidationResult(identifier = "unknown", companyInformation = null)),
        )

        performCreatePortfolioAndExpect(uploadBodyWith(setOf("unknown")), MockMvcResultMatchers.status().isNotFound)

        verify(incompleteCompanyEventService, never()).publishEventsForIncompleteCompanies(any(), any())
    }
}

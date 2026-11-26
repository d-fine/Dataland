package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.ServerException
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.amqp.AmqpException

class IncompleteCompanyEventServiceTest {
    private val companyDataControllerApi = mock<CompanyDataControllerApi>()
    private val messageQueuePublisherService = mock<MessageQueuePublisherService>()
    private val service = IncompleteCompanyEventService(companyDataControllerApi, messageQueuePublisherService)
    private val correlationId = "correlation-id"

    @BeforeEach
    fun setup() {
        reset(companyDataControllerApi, messageQueuePublisherService)
    }

    private fun stubCompany(
        id: String,
        fiscalYearEnd: String?,
        sector: String?,
    ) {
        whenever(companyDataControllerApi.getCompanyById(id)).thenReturn(
            StoredCompany(
                companyId = id,
                companyInformation =
                    CompanyInformation(
                        companyName = "Name $id",
                        headquarters = "HQ",
                        identifiers = emptyMap(),
                        countryCode = "DE",
                        fiscalYearEnd = fiscalYearEnd,
                        sector = sector,
                    ),
                dataRegisteredByDataland = emptyList(),
            ),
        )
    }

    @Test
    fun `complete company publishes no event`() {
        stubCompany("c1", "31-Mar", "Energy")
        service.publishEventsForIncompleteCompanies(setOf("c1"), correlationId)
        verify(messageQueuePublisherService, never()).publishCompanyInformationIncomplete(any(), any())
    }

    @Test
    fun `company missing fiscal year end publishes event`() {
        stubCompany("c1", null, "Energy")
        service.publishEventsForIncompleteCompanies(setOf("c1"), correlationId)
        verify(messageQueuePublisherService).publishCompanyInformationIncomplete("c1", correlationId)
    }

    @Test
    fun `company missing sector publishes event`() {
        stubCompany("c1", "31-Mar", null)
        service.publishEventsForIncompleteCompanies(setOf("c1"), correlationId)
        verify(messageQueuePublisherService).publishCompanyInformationIncomplete("c1", correlationId)
    }

    @Test
    fun `blank values count as missing`() {
        stubCompany("c1", " ", "")
        service.publishEventsForIncompleteCompanies(setOf("c1"), correlationId)
        verify(messageQueuePublisherService).publishCompanyInformationIncomplete("c1", correlationId)
    }

    @Test
    fun `only incomplete companies publish and one failure does not stop the others`() {
        stubCompany("complete", "31-Mar", "Energy")
        stubCompany("incomplete", null, null)
        whenever(companyDataControllerApi.getCompanyById("broken")).thenThrow(ServerException("backend down"))

        service.publishEventsForIncompleteCompanies(setOf("broken", "complete", "incomplete"), correlationId)

        verify(messageQueuePublisherService).publishCompanyInformationIncomplete("incomplete", correlationId)
        verify(messageQueuePublisherService, never()).publishCompanyInformationIncomplete("complete", correlationId)
        verify(messageQueuePublisherService, never()).publishCompanyInformationIncomplete("broken", correlationId)
    }

    @Test
    fun `backend client error and message queue error are isolated per company`() {
        stubCompany("incomplete", null, null)
        whenever(companyDataControllerApi.getCompanyById("deleted")).thenThrow(ClientException("not found", 404))
        stubCompany("unsendable", null, null)
        whenever(messageQueuePublisherService.publishCompanyInformationIncomplete("unsendable", correlationId))
            .thenThrow(AmqpException("rabbit down"))

        service.publishEventsForIncompleteCompanies(setOf("deleted", "unsendable", "incomplete"), correlationId)

        verify(messageQueuePublisherService).publishCompanyInformationIncomplete("incomplete", correlationId)
    }

    @Test
    fun `unexpected exceptions are propagated`() {
        whenever(companyDataControllerApi.getCompanyById("c1")).thenThrow(IllegalStateException("bug"))

        assertThrows<IllegalStateException> {
            service.publishEventsForIncompleteCompanies(setOf("c1"), correlationId)
        }
    }
}

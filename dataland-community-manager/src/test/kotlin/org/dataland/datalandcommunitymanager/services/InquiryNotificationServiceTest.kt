package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.InvalidEmailFormatApiException
import org.dataland.datalandcommunitymanager.model.inquiry.InquiryData
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class InquiryNotificationServiceTest {
    private val mockCloudEventMessageHandler = mock<CloudEventMessageHandler>()
    private val mockObjectMapper = mock<ObjectMapper>()
    private val service = InquiryNotificationService(mockCloudEventMessageHandler, mockObjectMapper)

    private val validInquiry =
        InquiryData(
            contactName = "Jane Doe",
            organisation = "Acme Corp",
            contactEmail = "jane.doe@example.com",
            message = "I would like to learn more about Dataland.",
        )

    @BeforeEach
    fun setup() {
        reset(mockCloudEventMessageHandler, mockObjectMapper)
        whenever(mockObjectMapper.writeValueAsString(any())).thenReturn("{}")
    }

    @Test
    fun `valid payload with organisation triggers notification`() {
        assertDoesNotThrow { service.processInquiry(validInquiry) }
        verify(mockCloudEventMessageHandler).buildCEMessageAndSendToQueue(any(), any(), any(), any(), any())
    }

    @Test
    fun `valid payload without organisation triggers notification`() {
        assertDoesNotThrow { service.processInquiry(validInquiry.copy(organisation = null)) }
        verify(mockCloudEventMessageHandler).buildCEMessageAndSendToQueue(any(), any(), any(), any(), any())
    }

    @Test
    fun `contactName containing newline throws InvalidInputApiException`() {
        assertThrows<InvalidInputApiException> {
            service.processInquiry(validInquiry.copy(contactName = "Jane\nDoe"))
        }
    }

    @Test
    fun `blank contactName throws InvalidInputApiException`() {
        assertThrows<InvalidInputApiException> {
            service.processInquiry(validInquiry.copy(contactName = "  "))
        }
    }

    @Test
    fun `contactName exceeding 200 chars throws InvalidInputApiException`() {
        assertThrows<InvalidInputApiException> {
            service.processInquiry(validInquiry.copy(contactName = "a".repeat(201)))
        }
    }

    @Test
    fun `contactName exactly 200 chars is accepted`() {
        assertDoesNotThrow { service.processInquiry(validInquiry.copy(contactName = "a".repeat(200))) }
    }

    @Test
    fun `blank organisation throws InvalidInputApiException`() {
        assertThrows<InvalidInputApiException> {
            service.processInquiry(validInquiry.copy(organisation = "   "))
        }
    }

    @Test
    fun `organisation exceeding 200 chars throws InvalidInputApiException`() {
        assertThrows<InvalidInputApiException> {
            service.processInquiry(validInquiry.copy(organisation = "a".repeat(201)))
        }
    }

    @Test
    fun `organisation exactly 200 chars is accepted`() {
        assertDoesNotThrow { service.processInquiry(validInquiry.copy(organisation = "a".repeat(200))) }
    }

    @Test
    fun `invalid contactEmail format throws InvalidEmailFormatApiException`() {
        assertThrows<InvalidEmailFormatApiException> {
            service.processInquiry(validInquiry.copy(contactEmail = "not-an-email"))
        }
    }

    @Test
    fun `contactEmail exceeding 320 chars throws InvalidInputApiException`() {
        val longLocalPart = "a".repeat(310)
        assertThrows<InvalidInputApiException> {
            service.processInquiry(validInquiry.copy(contactEmail = "$longLocalPart@x.com"))
        }
    }

    @Test
    fun `contactEmail exactly 320 chars is accepted`() {
        // local(314) + "@ab.cd"(6) = 320 chars exactly
        val email320 = "${"a".repeat(314)}@ab.cd"
        assertDoesNotThrow { service.processInquiry(validInquiry.copy(contactEmail = email320)) }
    }

    @Test
    fun `blank message throws InvalidInputApiException`() {
        assertThrows<InvalidInputApiException> {
            service.processInquiry(validInquiry.copy(message = "  "))
        }
    }

    @Test
    fun `message exceeding 5000 chars throws InvalidInputApiException`() {
        assertThrows<InvalidInputApiException> {
            service.processInquiry(validInquiry.copy(message = "a".repeat(5001)))
        }
    }

    @Test
    fun `message exactly 5000 chars is accepted`() {
        assertDoesNotThrow { service.processInquiry(validInquiry.copy(message = "a".repeat(5000))) }
    }

    @Test
    fun `notification dispatch failure throws InternalServerErrorApiException and not 200`() {
        doThrow(RuntimeException("queue unavailable"))
            .whenever(mockCloudEventMessageHandler)
            .buildCEMessageAndSendToQueue(any(), any(), any(), any(), any())
        assertThrows<InternalServerErrorApiException> {
            service.processInquiry(validInquiry)
        }
    }
}
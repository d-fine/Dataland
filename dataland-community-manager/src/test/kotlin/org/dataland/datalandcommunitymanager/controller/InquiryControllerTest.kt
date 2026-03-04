package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandcommunitymanager.model.inquiry.InquiryData
import org.dataland.datalandcommunitymanager.services.InquiryNotificationService
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

class InquiryControllerTest {
    private val mockInquiryNotificationService = mock<InquiryNotificationService>()
    private val inquiryController = InquiryController(mockInquiryNotificationService)

    private val validInquiryData =
        InquiryData(
            contactName = "Jane Doe",
            organisation = "Acme Corp",
            contactEmail = "jane.doe@example.com",
            message = "I would like to learn more about Dataland.",
        )

    @BeforeEach
    fun setup() {
        reset(mockInquiryNotificationService)
    }

    @Test
    fun `valid payload with all fields delegates to service and returns 200`() {
        assertDoesNotThrow {
            inquiryController.postInquiry(validInquiryData)
        }
        verify(mockInquiryNotificationService).processInquiry(validInquiryData)
    }

    @Test
    fun `valid payload without organisation delegates to service and returns 200`() {
        val inquiryWithoutOrganisation = validInquiryData.copy(organisation = null)
        assertDoesNotThrow {
            inquiryController.postInquiry(inquiryWithoutOrganisation)
        }
        verify(mockInquiryNotificationService).processInquiry(inquiryWithoutOrganisation)
    }

    @Test
    fun `notification dispatch failure propagates as InternalServerErrorApiException`() {
        doThrow(InternalServerErrorApiException("dispatch failed"))
            .whenever(mockInquiryNotificationService)
            .processInquiry(any())
        assertThrows<InternalServerErrorApiException> {
            inquiryController.postInquiry(validInquiryData)
        }
    }
}
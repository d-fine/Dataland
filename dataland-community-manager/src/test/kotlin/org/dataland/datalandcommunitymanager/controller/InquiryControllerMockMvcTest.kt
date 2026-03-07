package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandcommunitymanager.DatalandCommunityManager
import org.dataland.datalandcommunitymanager.services.InquiryNotificationService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = [DatalandCommunityManager::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureMockMvc
class InquiryControllerMockMvcTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var mockInquiryNotificationService: InquiryNotificationService

    private val validPayload =
        """{"contactName":"Jane Doe","contactEmail":"jane@example.com","message":"Hello Dataland"}"""

    @BeforeEach
    fun setup() {
        reset(mockInquiryNotificationService)
    }

    @Test
    fun `post inquiry without authorization header returns 201 not 401`() {
        mockMvc
            .perform(
                post("/inquiry")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validPayload),
            ).andExpect(status().isCreated)
    }

    @Test
    fun `notification service failure returns 500 not 201`() {
        doThrow(InternalServerErrorApiException("dispatch failed"))
            .whenever(mockInquiryNotificationService)
            .processInquiry(any())

        mockMvc
            .perform(
                post("/inquiry")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validPayload),
            ).andExpect(status().isInternalServerError)
    }
}
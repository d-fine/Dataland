package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandcommunitymanager.api.InquiryApi
import org.dataland.datalandcommunitymanager.model.inquiry.InquiryData
import org.dataland.datalandcommunitymanager.services.InquiryNotificationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the inquiry endpoint. Publicly accessible — no authentication required.
 */
@RestController
class InquiryController
    @Autowired
    constructor(
        private val inquiryNotificationService: InquiryNotificationService,
    ) : InquiryApi {
        override fun postInquiry(inquiryData: InquiryData): ResponseEntity<Unit> {
            inquiryNotificationService.processInquiry(inquiryData)
            return ResponseEntity.ok().build()
        }
    }
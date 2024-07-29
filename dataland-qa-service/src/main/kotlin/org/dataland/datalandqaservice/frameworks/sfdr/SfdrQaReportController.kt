package org.dataland.datalandqaservice.org.dataland.datalandqaservice.frameworks.sfdr

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandqaservice.frameworks.sfdr.model.SfdrData
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller.QaReportController
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReportManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * A REST controller for the SFDR QA report API.
 */
@RequestMapping("/data/sfdr")
@RestController
class SfdrQaReportController(
    @Autowired objectMapper: ObjectMapper,
    @Autowired qaReportManager: QaReportManager,
) : QaReportController<SfdrData>(objectMapper, qaReportManager, SfdrData::class.java, "sfdr")

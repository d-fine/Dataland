package org.dataland.datalandqaservice.org.dataland.datalandqaservice.frameworks

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandqaservice.frameworks.sfdr.model.SfdrData
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller.QaReportController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/data/sfdr")
@RestController
class SfdrQaReportController(
    @Autowired objectMapper: ObjectMapper,
)
    : QaReportController<SfdrData>(objectMapper, SfdrData::class.java) {
}
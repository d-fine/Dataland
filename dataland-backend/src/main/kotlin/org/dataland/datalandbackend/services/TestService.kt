package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.eutaxonomy.financials.EuTaxonomyDataForFinancials
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Controller


@Controller
class TestService {
    @Autowired
    //lateinit var repo : DataMetaInformationRepository

    @EventListener(ApplicationReadyEvent::class)
    fun doSomethingAfterStartup() {

    }
}
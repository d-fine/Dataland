package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Controller

@Controller
class TestService {
    @Autowired
    lateinit var repo: StoredCompanyRepository

    @EventListener(ApplicationReadyEvent::class)
    fun doSomethingAfterStartup() {
        // println(repo.getAllByDataRegisteredByDatalandDataTypeIn(DataTypesExtractor().getAllDataTypes()).map { it.companyName })
        // println(repo.getTest(StoredCompanySearchFilter(listOf("Teichmann and Bethke"))).map { it.companyName })
        // println(repo.getTest(StoredCompanySearchFilter(listOf(), listOf(), "08cneyd746x9", false)).map { it.companyName })
    }
}

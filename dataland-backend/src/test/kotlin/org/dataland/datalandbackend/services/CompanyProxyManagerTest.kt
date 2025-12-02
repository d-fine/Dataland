package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.model.proxies.CompanyProxy
import org.dataland.datalandbackend.repositories.CompanyProxyRepository
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest(
    classes = [DatalandBackend::class],
    properties = ["spring.profiles.active=containerized-db"],
)
class CompanyProxyManagerTest
    @Autowired
    constructor(
        private val companyDataProxyRuleRepository: CompanyProxyRepository,
        private val companyProxyManager: CompanyProxyManager,
    ) : BaseIntegrationTest() {
        @Test
        fun `test that addProxyRelation creates and persists a new proxy relation`() {
            val proxiedCompanyId = UUID.randomUUID()
            val proxyCompanyId = UUID.randomUUID()
            val proxyRelation =
                CompanyProxy(
                    proxiedCompanyId = proxiedCompanyId,
                    proxyCompanyId = proxyCompanyId,
                    framework = "sfdr",
                    reportingPeriod = "2025",
                )
            val savedEntity = companyProxyManager.addProxyRelation(proxyRelation)
            val retrievedEntity = companyDataProxyRuleRepository.findByProxyId(proxyCompanyId)

            assertNotNull(retrievedEntity)
            assertEquals(savedEntity, retrievedEntity)
        }
    }

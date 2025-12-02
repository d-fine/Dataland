package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.CompanyProxyEntity
import org.dataland.datalandbackend.model.proxies.CompanyProxy
import org.dataland.datalandbackend.model.proxies.StoredCompanyProxy
import org.dataland.datalandbackend.repositories.CompanyProxyRepository
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest(
    classes = [DatalandBackend::class],
    properties = ["spring.profiles.active=containerized-db"],
)
@DefaultMocks
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
            val retrievedEntity = companyDataProxyRuleRepository.findAllByProxiedCompanyId(proxiedCompanyId).first()

            assertNotNull(retrievedEntity)
            assertEquals(savedEntity, retrievedEntity)
        }

        private fun callFilterFunction(
            proxied: UUID? = null,
            proxy: UUID? = null,
            fws: Set<String>? = null,
            periods: Set<String>? = null,
            size: Int = 100,
            idx: Int = 0,
        ) = companyProxyManager.getCompanyProxiesByFilters(
            proxiedCompanyId = proxied,
            proxyCompanyId = proxy,
            frameworks = fws,
            reportingPeriods = periods,
            chunkSize = size,
            chunkIndex = idx,
        )

        private fun createProxyEntity(
            proxiedCompanyId: UUID,
            proxyCompanyId: UUID,
            framework: String,
            reportingPeriod: String,
        ) = companyDataProxyRuleRepository.save(
            CompanyProxyEntity(
                proxiedCompanyId = proxiedCompanyId,
                proxyCompanyId = proxyCompanyId,
                framework = framework,
                reportingPeriod = reportingPeriod,
            ),
        )

        private fun proxyIds(result: List<StoredCompanyProxy>) = result.map { UUID.fromString(it.proxyId) }.toSet()

        @Test
        fun `getCompanyProxiesByFilters supports all filters and pagination`() {
            val proxiedCompanyIdA = UUID.randomUUID()
            val proxiedCompanyIdB = UUID.randomUUID()
            val proxyCompanyIdX = UUID.randomUUID()
            val proxyCompanyIdY = UUID.randomUUID()

            val entity1 = createProxyEntity(proxiedCompanyIdA, proxyCompanyIdX, "sfdr", "2023")
            val entity2 = createProxyEntity(proxiedCompanyIdA, proxyCompanyIdY, "sfdr", "2022")
            val entity3 = createProxyEntity(proxiedCompanyIdA, proxyCompanyIdX, "eutaxonomy-financials", "2023")
            val entity4 = createProxyEntity(proxiedCompanyIdB, proxyCompanyIdY, "eutaxonomy-financials", "2021")

            assertEquals(
                setOf(entity1.proxyId, entity2.proxyId, entity3.proxyId),
                proxyIds(callFilterFunction(proxied = proxiedCompanyIdA)),
            )

            assertEquals(
                setOf(entity1.proxyId, entity2.proxyId),
                proxyIds(callFilterFunction(proxied = proxiedCompanyIdA, fws = setOf("sfdr"))),
            )

            assertEquals(
                setOf(entity1.proxyId, entity3.proxyId),
                proxyIds(callFilterFunction(proxied = proxiedCompanyIdA, periods = setOf("2023"))),
            )

            assertEquals(
                setOf(entity1.proxyId),
                proxyIds(
                    callFilterFunction(
                        proxied = proxiedCompanyIdA, proxy = proxyCompanyIdX, fws = setOf("sfdr"), periods = setOf("2023"),
                    ),
                ),
            )

            assertTrue(
                callFilterFunction(
                    proxied = proxiedCompanyIdA,
                    proxy = proxyCompanyIdY,
                    fws = setOf("eutaxonomy-financials"),
                    periods = setOf("2023"),
                ).isEmpty(),
            )

            val page0 = callFilterFunction(proxied = proxiedCompanyIdA, size = 1, idx = 0)
            val page1 = callFilterFunction(proxied = proxiedCompanyIdA, size = 1, idx = 1)
            assertEquals(1, page0.size)
            assertEquals(1, page1.size)
            assertNotEquals(page0.first().proxyId, page1.first().proxyId)

            assertEquals(2, callFilterFunction(proxied = proxiedCompanyIdA, size = 2, idx = 0).size)

            assertEquals(setOf(entity4.proxyId), proxyIds(callFilterFunction(proxied = proxiedCompanyIdB)))
        }

        @Test
        fun `getCompanyProxiesByFilters returns empty list if nothing matches`() {
            val result =
                companyProxyManager.getCompanyProxiesByFilters(
                    proxiedCompanyId = UUID.randomUUID(),
                    proxyCompanyId = null,
                    frameworks = null,
                    reportingPeriods = null,
                    chunkSize = 10,
                    chunkIndex = 0,
                )
            assertTrue(result.isEmpty())
        }
    }

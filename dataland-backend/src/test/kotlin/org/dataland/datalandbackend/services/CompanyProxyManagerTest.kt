package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.CompanyProxyEntity
import org.dataland.datalandbackend.model.proxies.CompanyProxy
import org.dataland.datalandbackend.model.proxies.StoredCompanyProxy
import org.dataland.datalandbackend.repositories.CompanyProxyRepository
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
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
        private val defaultProxiedCompanyId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val defaultProxyCompanyId = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private val defaultFramework = "sfdr"
        private val defaultReportingPeriod = "2025"
        private val altProxiedCompanyId = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val altProxyCompanyId = UUID.fromString("00000000-0000-0000-0000-000000000004")
        private val altFramework = "eutaxonomy-financials"
        private val altReportingPeriod = "2026"
        private val defaultCompanyProxy =
            CompanyProxy(
                proxiedCompanyId = defaultProxiedCompanyId,
                proxyCompanyId = defaultProxyCompanyId,
                framework = defaultFramework,
                reportingPeriod = defaultReportingPeriod,
            )
        private val altCompanyProxy =
            CompanyProxy(
                proxiedCompanyId = altProxiedCompanyId,
                proxyCompanyId = altProxyCompanyId,
                framework = altFramework,
                reportingPeriod = altReportingPeriod,
            )

        @Test
        fun `test that addProxyRelation creates and persists a new proxy relation`() {
            val proxyRelation = defaultCompanyProxy
            val savedEntity = companyProxyManager.addProxyRelation(proxyRelation)
            val retrievedEntity = companyDataProxyRuleRepository.findAllByProxiedCompanyId(defaultProxiedCompanyId).first()

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
            proxiedCompanyId: UUID = defaultProxiedCompanyId,
            proxyCompanyId: UUID = defaultProxyCompanyId,
            framework: String = defaultFramework,
            reportingPeriod: String = defaultReportingPeriod,
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
            val entity1 = createProxyEntity(defaultProxiedCompanyId, defaultProxyCompanyId, defaultFramework, "2023")
            val entity2 = createProxyEntity(defaultProxiedCompanyId, altProxyCompanyId, defaultFramework, "2022")
            val entity3 = createProxyEntity(defaultProxiedCompanyId, defaultProxyCompanyId, altFramework, "2023")
            val entity4 = createProxyEntity(altProxiedCompanyId, altProxyCompanyId, altFramework, "2021")

            assertEquals(
                setOf(entity1.proxyId, entity2.proxyId, entity3.proxyId),
                proxyIds(callFilterFunction(proxied = defaultProxiedCompanyId)),
            )

            assertEquals(
                setOf(entity1.proxyId, entity2.proxyId),
                proxyIds(callFilterFunction(proxied = defaultProxiedCompanyId, fws = setOf(defaultFramework))),
            )

            assertEquals(
                setOf(entity1.proxyId, entity3.proxyId),
                proxyIds(callFilterFunction(proxied = defaultProxiedCompanyId, periods = setOf("2023"))),
            )

            assertEquals(
                setOf(entity1.proxyId),
                proxyIds(
                    callFilterFunction(
                        proxied = defaultProxiedCompanyId,
                        proxy = defaultProxyCompanyId,
                        fws = setOf(defaultFramework),
                        periods = setOf("2023"),
                    ),
                ),
            )

            assertTrue(
                callFilterFunction(
                    proxied = defaultProxiedCompanyId,
                    proxy = altProxyCompanyId,
                    fws = setOf(altFramework),
                    periods = setOf("2023"),
                ).isEmpty(),
            )

            val page0 = callFilterFunction(proxied = defaultProxiedCompanyId, size = 1, idx = 0)
            val page1 = callFilterFunction(proxied = defaultProxiedCompanyId, size = 1, idx = 1)
            assertEquals(1, page0.size)
            assertEquals(1, page1.size)
            assertNotEquals(page0.first().proxyId, page1.first().proxyId)

            assertEquals(2, callFilterFunction(proxied = defaultProxiedCompanyId, size = 2, idx = 0).size)

            assertEquals(setOf(entity4.proxyId), proxyIds(callFilterFunction(proxied = altProxiedCompanyId)))
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

        @Test
        fun `getCompanyProxyById returns the correct StoredCompanyProxy when found`() {
            val proxyRelation = defaultCompanyProxy
            val savedEntity = companyProxyManager.addProxyRelation(proxyRelation)
            val result = companyProxyManager.getCompanyProxyById(savedEntity.proxyId)
            assertEquals(savedEntity.toStoredCompanyProxy(), result)
        }

        @Test
        fun `getCompanyProxyById throws ResourceNotFoundApiException when not found`() {
            val randomProxyId = UUID.randomUUID()
            assertThrows<ResourceNotFoundApiException> {
                companyProxyManager.getCompanyProxyById(randomProxyId)
            }
        }

        @Test
        fun `editCompanyProxy updates the proxy relation and returns the updated StoredCompanyProxy`() {
            val proxyRelation = defaultCompanyProxy
            val savedEntity = companyProxyManager.addProxyRelation(proxyRelation)

            val updatedStoredProxy = companyProxyManager.editCompanyProxy(savedEntity.proxyId, altCompanyProxy)

            val persisted = companyProxyManager.getCompanyProxyById(savedEntity.proxyId)

            val expected =
                StoredCompanyProxy(
                    proxyId = savedEntity.proxyId.toString(),
                    proxiedCompanyId = altProxiedCompanyId.toString(),
                    proxyCompanyId = altProxyCompanyId.toString(),
                    framework = altFramework,
                    reportingPeriod = altReportingPeriod,
                )
            assertEquals(expected, updatedStoredProxy)
            assertEquals(expected, persisted)
        }

        @Test
        fun `editCompanyProxy throws ResourceNotFoundApiException when proxy does not exist`() {
            val randomProxyId = UUID.randomUUID()
            assertThrows<ResourceNotFoundApiException> {
                companyProxyManager.editCompanyProxy(randomProxyId, altCompanyProxy)
            }
        }

        @Test
        fun `test that deleteProxyRelation deletes a proxy relation`() {
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
            val retrievedEntity = companyProxyManager.getCompanyProxyById(savedEntity.proxyId)
            assertNotNull(retrievedEntity)

            companyProxyManager.deleteProxyRelation(savedEntity.proxyId)
            assertThrows<ResourceNotFoundApiException> {
                companyProxyManager.getCompanyProxyById(savedEntity.proxyId)
            }
        }
    }

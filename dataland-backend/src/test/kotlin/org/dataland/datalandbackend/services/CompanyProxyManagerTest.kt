package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.CompanyProxyEntity
import org.dataland.datalandbackend.model.proxies.CompanyProxy
import org.dataland.datalandbackend.model.proxies.StoredCompanyProxy
import org.dataland.datalandbackend.repositories.CompanyProxyRepository
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
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
        private val companyProxyRepository: CompanyProxyRepository,
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

        private val nullFrameworkProxiedCompanyId = UUID.fromString("00000000-0000-0000-0000-000000000005")
        private val nullFrameworkProxyCompanyId = UUID.fromString("00000000-0000-0000-0000-000000000006")
        private val nullFrameworkReportingPeriod = "2023"

        private val nullReportingPeriodProxiedCompanyId = UUID.fromString("00000000-0000-0000-0000-000000000007")
        private val nullReportingPeriodProxyCompanyId = UUID.fromString("00000000-0000-0000-0000-000000000008")
        private val nullReportingPeriodFramework = "nuclear-and-gas"

        private val defaultCompanyProxy =
            CompanyProxy<UUID>(
                proxiedCompanyId = defaultProxiedCompanyId,
                proxyCompanyId = defaultProxyCompanyId,
                framework = defaultFramework,
                reportingPeriod = defaultReportingPeriod,
            )
        private val altCompanyProxy =
            CompanyProxy<UUID>(
                proxiedCompanyId = altProxiedCompanyId,
                proxyCompanyId = altProxyCompanyId,
                framework = altFramework,
                reportingPeriod = altReportingPeriod,
            )

        private val nullFrameworkCompanyProxy =
            CompanyProxy<UUID>(
                proxiedCompanyId = nullFrameworkProxiedCompanyId,
                proxyCompanyId = nullFrameworkProxyCompanyId,
                framework = null,
                reportingPeriod = nullFrameworkReportingPeriod,
            )

        private val nullReportingPeriodCompanyProxy =
            CompanyProxy<UUID>(
                proxiedCompanyId = nullReportingPeriodProxiedCompanyId,
                proxyCompanyId = nullReportingPeriodProxyCompanyId,
                framework = nullReportingPeriodFramework,
                reportingPeriod = null,
            )

        @Test
        fun `test that addProxyRelation creates and persists a new proxy relation`() {
            val proxyRelation = defaultCompanyProxy
            val savedEntity = companyProxyManager.addProxyRelation(proxyRelation)
            val retrievedEntity = companyProxyRepository.findAllByProxiedCompanyId(defaultProxiedCompanyId).first()

            assertNotNull(retrievedEntity)
            assertEquals(proxyRelation.proxiedCompanyId, retrievedEntity.proxiedCompanyId)
            assertEquals(proxyRelation.proxyCompanyId, retrievedEntity.proxyCompanyId)
            assertEquals(proxyRelation.framework, retrievedEntity.framework)
            assertEquals(proxyRelation.reportingPeriod, retrievedEntity.reportingPeriod)
            assertEquals(savedEntity, retrievedEntity)
        }

        @Test
        fun `addProxyRelation throws on duplicate proxy relation`() {
            companyProxyManager.addProxyRelation(defaultCompanyProxy)

            val ex =
                assertThrows(InvalidInputApiException::class.java) {
                    companyProxyManager.addProxyRelation(defaultCompanyProxy)
                }
            assertTrue(ex.summary.contains("Conflicting proxy relation exists"))
            assertTrue(ex.message.contains("Conflicting proxyIds:"))
        }

        @Test
        fun `addProxyRelation throws on conflicting proxy relation`() {
            val proxyId = companyProxyManager.addProxyRelation(defaultCompanyProxy).proxyId

            val conflictingProxy =
                CompanyProxy<UUID>(
                    proxiedCompanyId = defaultProxiedCompanyId,
                    proxyCompanyId = altProxyCompanyId,
                    framework = defaultFramework,
                    reportingPeriod = defaultReportingPeriod,
                )
            var ex =
                assertThrows(InvalidInputApiException::class.java) {
                    companyProxyManager.addProxyRelation(conflictingProxy)
                }
            assertTrue(ex.summary.contains("Conflicting proxy relation exists"))
            assertTrue(ex.message.contains("Conflicting proxyIds:"))
            assertTrue(ex.message.contains(proxyId.toString()))

            val conflictingProxyWithNullValues =
                CompanyProxy<UUID>(
                    proxiedCompanyId = defaultProxiedCompanyId,
                    proxyCompanyId = altProxyCompanyId,
                    framework = null,
                    reportingPeriod = null,
                )
            ex =
                assertThrows(InvalidInputApiException::class.java) {
                    companyProxyManager.addProxyRelation(conflictingProxyWithNullValues)
                }
            assertTrue(ex.message.contains(proxyId.toString()))
        }

        @Test
        fun `addProxyRelation does not match on different framework or reporting period`() {
            companyProxyManager.addProxyRelation(defaultCompanyProxy)

            val differentFramework = defaultCompanyProxy.copy(framework = altFramework)
            val entity2 = companyProxyManager.addProxyRelation(differentFramework)
            assertNotEquals(defaultCompanyProxy.framework, entity2.framework)

            val differentPeriod = defaultCompanyProxy.copy(reportingPeriod = "2022")
            val entity3 = companyProxyManager.addProxyRelation(differentPeriod)
            assertNotEquals(defaultCompanyProxy.reportingPeriod, entity3.reportingPeriod)
        }

        @Test
        fun `addProxyRelation allows to add non conflicting relation for framework = null`() {
            companyProxyManager.addProxyRelation(nullFrameworkCompanyProxy)

            val differentReportingPeriod = nullFrameworkCompanyProxy.copy(reportingPeriod = "2024")
            assertDoesNotThrow {
                companyProxyManager.addProxyRelation(differentReportingPeriod)
            }

            val differentFrameworkAndReportingPeriod =
                nullFrameworkCompanyProxy.copy(
                    reportingPeriod = "2025",
                    framework = "sfdr",
                )

            assertDoesNotThrow {
                companyProxyManager.addProxyRelation(differentFrameworkAndReportingPeriod)
            }
        }

        @Test
        fun `addProxyRelation allows to add non conflicting relation for reportingPeriod = null`() {
            companyProxyManager.addProxyRelation(nullReportingPeriodCompanyProxy)

            val differentFramework = nullReportingPeriodCompanyProxy.copy(framework = "sfdr")
            assertDoesNotThrow {
                companyProxyManager.addProxyRelation(differentFramework)
            }

            val differentFrameworkAndReportingPeriod =
                nullFrameworkCompanyProxy.copy(
                    reportingPeriod = "2025",
                    framework = "eu-taxonomy-financials",
                )

            assertDoesNotThrow {
                companyProxyManager.addProxyRelation(differentFrameworkAndReportingPeriod)
            }
        }

        @Test
        fun `addProxyRelation with null framework and reportingPeriod rejects if any for proxiedId exists`() {
            val anotherProxyId = UUID.randomUUID()

            val proxyId = companyProxyManager.addProxyRelation(defaultCompanyProxy).proxyId

            val general =
                CompanyProxy<UUID>(
                    proxiedCompanyId = defaultProxiedCompanyId,
                    proxyCompanyId = anotherProxyId,
                    framework = null,
                    reportingPeriod = null,
                )
            val ex =
                assertThrows(InvalidInputApiException::class.java) {
                    companyProxyManager.addProxyRelation(general)
                }
            assertTrue(ex.message.contains(proxyId.toString()))
        }

        @Test
        fun `addProxyRelation with null framework allows adding relations with different reporting periods`() {
            val proxiedCompanyId = UUID.randomUUID()
            val proxyCompanyId = UUID.randomUUID()
            val base =
                CompanyProxy<UUID>(
                    proxiedCompanyId = proxiedCompanyId,
                    proxyCompanyId = proxyCompanyId,
                    framework = null,
                    reportingPeriod = "2023",
                )
            val proxyId = companyProxyManager.addProxyRelation(base).proxyId

            val conflict =
                CompanyProxy<UUID>(
                    proxiedCompanyId = proxiedCompanyId,
                    proxyCompanyId = proxyCompanyId,
                    framework = null,
                    reportingPeriod = "2023",
                )
            val ex =
                assertThrows(InvalidInputApiException::class.java) {
                    companyProxyManager.addProxyRelation(conflict)
                }
            assertTrue(ex.message.contains(proxyId.toString()))
            val diffPeriod = base.copy(reportingPeriod = "2024")
            assertDoesNotThrow {
                companyProxyManager.addProxyRelation(diffPeriod)
            }
        }

        @Test
        fun `addProxyRelation with null reportingPeriod allows adding relation with different framework `() {
            val proxiedCompanyId = UUID.randomUUID()
            val proxyCompanyId = UUID.randomUUID()

            val base =
                CompanyProxy<UUID>(
                    proxiedCompanyId = proxiedCompanyId,
                    proxyCompanyId = proxyCompanyId,
                    framework = "sfdr",
                    reportingPeriod = null,
                )
            val proxyId = companyProxyManager.addProxyRelation(base).proxyId

            val conflict =
                CompanyProxy<UUID>(
                    proxiedCompanyId = proxiedCompanyId,
                    proxyCompanyId = proxyCompanyId,
                    framework = "sfdr",
                    reportingPeriod = null,
                )
            val ex =
                assertThrows(InvalidInputApiException::class.java) {
                    companyProxyManager.addProxyRelation(conflict)
                }
            assertTrue(ex.message.contains(proxyId.toString()))

            val otherFw = base.copy(framework = altFramework)
            assertDoesNotThrow {
                companyProxyManager.addProxyRelation(otherFw)
            }
        }

        @Test
        fun `addProxyRelation rejects a ProxyRelation if there already as on for the company pair with only null values`() {
            val proxiedId = UUID.randomUUID()

            val generic =
                CompanyProxy<UUID>(
                    proxiedCompanyId = proxiedId,
                    proxyCompanyId = UUID.randomUUID(),
                    framework = null,
                    reportingPeriod = null,
                )
            val proxyId = companyProxyManager.addProxyRelation(generic).proxyId

            val specific =
                generic.copy(
                    proxyCompanyId = UUID.randomUUID(),
                    framework = "sfdr",
                    reportingPeriod = "2023",
                )
            val ex =
                assertThrows(InvalidInputApiException::class.java) {
                    companyProxyManager.addProxyRelation(specific)
                }
            assertTrue(ex.message.contains(proxyId.toString()))
        }

        private fun callFilterFunction(
            proxiedCompanyId: UUID? = null,
            proxyCompanyId: UUID? = null,
            frameworks: Set<String>? = null,
            reportingPeriods: Set<String>? = null,
            size: Int = 100,
            idx: Int = 0,
        ) = companyProxyManager.getCompanyProxiesByFilters(
            proxiedCompanyId = proxiedCompanyId,
            proxyCompanyId = proxyCompanyId,
            frameworks = frameworks,
            reportingPeriods = reportingPeriods,
            chunkSize = size,
            chunkIndex = idx,
        )

        private fun createProxyEntity(
            proxiedCompanyId: UUID = defaultProxiedCompanyId,
            proxyCompanyId: UUID = defaultProxyCompanyId,
            framework: String = defaultFramework,
            reportingPeriod: String = defaultReportingPeriod,
        ) = companyProxyRepository.save(
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
                proxyIds(callFilterFunction(proxiedCompanyId = defaultProxiedCompanyId)),
            )

            assertEquals(
                setOf(entity1.proxyId, entity2.proxyId),
                proxyIds(callFilterFunction(proxiedCompanyId = defaultProxiedCompanyId, frameworks = setOf(defaultFramework))),
            )

            assertEquals(
                setOf(entity1.proxyId, entity3.proxyId),
                proxyIds(callFilterFunction(proxiedCompanyId = defaultProxiedCompanyId, reportingPeriods = setOf("2023"))),
            )

            assertEquals(
                setOf(entity1.proxyId),
                proxyIds(
                    callFilterFunction(
                        proxiedCompanyId = defaultProxiedCompanyId,
                        proxyCompanyId = defaultProxyCompanyId,
                        frameworks = setOf(defaultFramework),
                        reportingPeriods = setOf("2023"),
                    ),
                ),
            )

            val page0 = callFilterFunction(proxiedCompanyId = defaultProxiedCompanyId, size = 1, idx = 0)
            val page1 = callFilterFunction(proxiedCompanyId = defaultProxiedCompanyId, size = 1, idx = 1)
            assertEquals(1, page0.size)
            assertEquals(1, page1.size)
            assertNotEquals(page0.first().proxyId, page1.first().proxyId)

            assertEquals(2, callFilterFunction(proxiedCompanyId = defaultProxiedCompanyId, size = 2, idx = 0).size)

            assertEquals(setOf(entity4.proxyId), proxyIds(callFilterFunction(proxiedCompanyId = altProxiedCompanyId)))
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
            val savedEntity = companyProxyManager.addProxyRelation(defaultCompanyProxy)
            val result = companyProxyManager.getCompanyProxyById(savedEntity.proxyId)
            assertEquals(savedEntity.toStoredCompanyProxy(), result)
        }

        @Test
        fun `getCompanyProxyById throws ResourceNotFoundApiException when not found`() {
            val randomProxyId = UUID.randomUUID()
            assertThrows(ResourceNotFoundApiException::class.java) {
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
            assertThrows(ResourceNotFoundApiException::class.java) {
                companyProxyManager.editCompanyProxy(randomProxyId, altCompanyProxy)
            }
        }

        @Test
        fun `test that deleteProxyRelation deletes a proxy relation`() {
            val savedEntity = companyProxyManager.addProxyRelation(defaultCompanyProxy)
            val retrievedEntity = companyProxyManager.getCompanyProxyById(savedEntity.proxyId)
            assertNotNull(retrievedEntity)

            companyProxyManager.deleteProxyRelation(savedEntity.proxyId)
            assertThrows(ResourceNotFoundApiException::class.java) {
                companyProxyManager.getCompanyProxyById(savedEntity.proxyId)
            }
        }

        @Test
        fun `deleteProxyRelation throws ResourceNotFoundApiException when proxy does not exist`() {
            val randomProxyId = UUID.randomUUID()
            assertThrows(ResourceNotFoundApiException::class.java) {
                companyProxyManager.deleteProxyRelation(randomProxyId)
            }
        }
    }

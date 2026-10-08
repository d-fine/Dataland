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
        private val testUserId = "test-user-id"
        private val defaultChildCompanyId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val defaultParentCompanyId = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private val defaultFramework = "sfdr"
        private val defaultReportingPeriod = "2025"
        private val altChildCompanyId = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val altParentCompanyId = UUID.fromString("00000000-0000-0000-0000-000000000004")
        private val altFramework = "eutaxonomy-financials"
        private val altReportingPeriod = "2026"

        private val nullFrameworkChildCompanyId = UUID.fromString("00000000-0000-0000-0000-000000000005")
        private val nullFrameworkParentCompanyId = UUID.fromString("00000000-0000-0000-0000-000000000006")
        private val nullFrameworkReportingPeriod = "2023"

        private val nullReportingPeriodChildCompanyId = UUID.fromString("00000000-0000-0000-0000-000000000007")
        private val nullReportingPeriodParentCompanyId = UUID.fromString("00000000-0000-0000-0000-000000000008")
        private val nullReportingPeriodFramework = "nuclear-and-gas"

        private val defaultCompanyProxy =
            CompanyProxy<UUID>(
                childCompanyId = defaultChildCompanyId,
                parentCompanyId = defaultParentCompanyId,
                framework = defaultFramework,
                reportingPeriod = defaultReportingPeriod,
            )
        private val altCompanyProxy =
            CompanyProxy<UUID>(
                childCompanyId = altChildCompanyId,
                parentCompanyId = altParentCompanyId,
                framework = altFramework,
                reportingPeriod = altReportingPeriod,
            )

        private val nullFrameworkCompanyProxy =
            CompanyProxy<UUID>(
                childCompanyId = nullFrameworkChildCompanyId,
                parentCompanyId = nullFrameworkParentCompanyId,
                framework = null,
                reportingPeriod = nullFrameworkReportingPeriod,
            )

        private val nullReportingPeriodCompanyProxy =
            CompanyProxy<UUID>(
                childCompanyId = nullReportingPeriodChildCompanyId,
                parentCompanyId = nullReportingPeriodParentCompanyId,
                framework = nullReportingPeriodFramework,
                reportingPeriod = null,
            )

        @Test
        fun `test that addProxyRelation creates and persists a new proxy relation`() {
            val proxyRelation = defaultCompanyProxy
            val savedEntity = companyProxyManager.addProxyRelation(proxyRelation, testUserId)
            val retrievedEntity = companyProxyRepository.findAllByChildCompanyId(defaultChildCompanyId).first()

            assertNotNull(retrievedEntity)
            assertEquals(proxyRelation.childCompanyId, retrievedEntity.childCompanyId)
            assertEquals(proxyRelation.parentCompanyId, retrievedEntity.parentCompanyId)
            assertEquals(proxyRelation.framework, retrievedEntity.framework)
            assertEquals(proxyRelation.reportingPeriod, retrievedEntity.reportingPeriod)
            assertEquals(savedEntity, retrievedEntity)
        }

        @Test
        fun `addProxyRelation throws on duplicate proxy relation`() {
            companyProxyManager.addProxyRelation(defaultCompanyProxy, testUserId)

            val ex =
                assertThrows(InvalidInputApiException::class.java) {
                    companyProxyManager.addProxyRelation(defaultCompanyProxy, testUserId)
                }
            assertTrue(ex.summary.contains("Conflicting proxy relation exists"))
            assertTrue(ex.message.contains("Conflicting proxyIds:"))
        }

        @Test
        fun `addProxyRelation throws on conflicting proxy relation`() {
            val proxyId = companyProxyManager.addProxyRelation(defaultCompanyProxy, testUserId).proxyId

            val conflictingProxy =
                CompanyProxy<UUID>(
                    childCompanyId = defaultChildCompanyId,
                    parentCompanyId = altParentCompanyId,
                    framework = defaultFramework,
                    reportingPeriod = defaultReportingPeriod,
                )
            var ex =
                assertThrows(InvalidInputApiException::class.java) {
                    companyProxyManager.addProxyRelation(conflictingProxy, testUserId)
                }
            assertTrue(ex.summary.contains("Conflicting proxy relation exists"))
            assertTrue(ex.message.contains("Conflicting proxyIds:"))
            assertTrue(ex.message.contains(proxyId.toString()))

            val conflictingProxyWithNullValues =
                CompanyProxy<UUID>(
                    childCompanyId = defaultChildCompanyId,
                    parentCompanyId = altParentCompanyId,
                    framework = null,
                    reportingPeriod = null,
                )
            ex =
                assertThrows(InvalidInputApiException::class.java) {
                    companyProxyManager.addProxyRelation(conflictingProxyWithNullValues, testUserId)
                }
            assertTrue(ex.message.contains(proxyId.toString()))
        }

        @Test
        fun `addProxyRelation does not match on different framework or reporting period`() {
            companyProxyManager.addProxyRelation(defaultCompanyProxy, testUserId)

            val differentFramework = defaultCompanyProxy.copy(framework = altFramework)
            val entity2 = companyProxyManager.addProxyRelation(differentFramework, testUserId)
            assertNotEquals(defaultCompanyProxy.framework, entity2.framework)

            val differentPeriod = defaultCompanyProxy.copy(reportingPeriod = "2022")
            val entity3 = companyProxyManager.addProxyRelation(differentPeriod, testUserId)
            assertNotEquals(defaultCompanyProxy.reportingPeriod, entity3.reportingPeriod)
        }

        @Test
        fun `addProxyRelation allows to add non conflicting relation for framework is null`() {
            companyProxyManager.addProxyRelation(nullFrameworkCompanyProxy, testUserId)

            val differentReportingPeriod = nullFrameworkCompanyProxy.copy(reportingPeriod = "2024")
            assertDoesNotThrow {
                companyProxyManager.addProxyRelation(differentReportingPeriod, testUserId)
            }

            val differentFrameworkAndReportingPeriod =
                nullFrameworkCompanyProxy.copy(
                    reportingPeriod = "2025",
                    framework = "sfdr",
                )

            assertDoesNotThrow {
                companyProxyManager.addProxyRelation(differentFrameworkAndReportingPeriod, testUserId)
            }
        }

        @Test
        fun `addProxyRelation allows to add non conflicting relation for reportingPeriod is null`() {
            companyProxyManager.addProxyRelation(nullReportingPeriodCompanyProxy, testUserId)

            val differentFramework = nullReportingPeriodCompanyProxy.copy(framework = "sfdr")
            assertDoesNotThrow {
                companyProxyManager.addProxyRelation(differentFramework, testUserId)
            }

            val differentFrameworkAndReportingPeriod =
                nullFrameworkCompanyProxy.copy(
                    reportingPeriod = "2025",
                    framework = "eu-taxonomy-financials",
                )

            assertDoesNotThrow {
                companyProxyManager.addProxyRelation(differentFrameworkAndReportingPeriod, testUserId)
            }
        }

        @Test
        fun `addProxyRelation with null framework and reportingPeriod rejects if any for childId exists`() {
            val anotherProxyId = UUID.randomUUID()

            val proxyId = companyProxyManager.addProxyRelation(defaultCompanyProxy, testUserId).proxyId

            val general =
                CompanyProxy<UUID>(
                    childCompanyId = defaultChildCompanyId,
                    parentCompanyId = anotherProxyId,
                    framework = null,
                    reportingPeriod = null,
                )
            val ex =
                assertThrows(InvalidInputApiException::class.java) {
                    companyProxyManager.addProxyRelation(general, testUserId)
                }
            assertTrue(ex.message.contains(proxyId.toString()))
        }

        @Test
        fun `addProxyRelation with null framework allows adding relations with different reporting periods`() {
            val childCompanyId = UUID.randomUUID()
            val parentCompanyId = UUID.randomUUID()
            val base =
                CompanyProxy<UUID>(
                    childCompanyId = childCompanyId,
                    parentCompanyId = parentCompanyId,
                    framework = null,
                    reportingPeriod = "2023",
                )
            val proxyId = companyProxyManager.addProxyRelation(base, testUserId).proxyId

            val conflict =
                CompanyProxy<UUID>(
                    childCompanyId = childCompanyId,
                    parentCompanyId = parentCompanyId,
                    framework = null,
                    reportingPeriod = "2023",
                )
            val ex =
                assertThrows(InvalidInputApiException::class.java) {
                    companyProxyManager.addProxyRelation(conflict, testUserId)
                }
            assertTrue(ex.message.contains(proxyId.toString()))
            val diffPeriod = base.copy(reportingPeriod = "2024")
            assertDoesNotThrow {
                companyProxyManager.addProxyRelation(diffPeriod, testUserId)
            }
        }

        @Test
        fun `addProxyRelation with null reportingPeriod allows adding relation with different framework `() {
            val childCompanyId = UUID.randomUUID()
            val parentCompanyId = UUID.randomUUID()

            val base =
                CompanyProxy<UUID>(
                    childCompanyId = childCompanyId,
                    parentCompanyId = parentCompanyId,
                    framework = "sfdr",
                    reportingPeriod = null,
                )
            val proxyId = companyProxyManager.addProxyRelation(base, testUserId).proxyId

            val conflict =
                CompanyProxy<UUID>(
                    childCompanyId = childCompanyId,
                    parentCompanyId = parentCompanyId,
                    framework = "sfdr",
                    reportingPeriod = null,
                )
            val ex =
                assertThrows(InvalidInputApiException::class.java) {
                    companyProxyManager.addProxyRelation(conflict, testUserId)
                }
            assertTrue(ex.message.contains(proxyId.toString()))

            val otherFw = base.copy(framework = altFramework)
            assertDoesNotThrow {
                companyProxyManager.addProxyRelation(otherFw, testUserId)
            }
        }

        @Test
        fun `addProxyRelation rejects a ProxyRelation if there already as on for the company pair with only null values`() {
            val childId = UUID.randomUUID()

            val generic =
                CompanyProxy<UUID>(
                    childCompanyId = childId,
                    parentCompanyId = UUID.randomUUID(),
                    framework = null,
                    reportingPeriod = null,
                )
            val proxyId = companyProxyManager.addProxyRelation(generic, testUserId).proxyId

            val specific =
                generic.copy(
                    parentCompanyId = UUID.randomUUID(),
                    framework = "sfdr",
                    reportingPeriod = "2023",
                )
            val ex =
                assertThrows(InvalidInputApiException::class.java) {
                    companyProxyManager.addProxyRelation(specific, testUserId)
                }
            assertTrue(ex.message.contains(proxyId.toString()))
        }

        private fun callFilterFunction(
            childCompanyId: UUID? = null,
            parentCompanyId: UUID? = null,
            frameworks: Set<String>? = null,
            reportingPeriods: Set<String>? = null,
            size: Int = 100,
            idx: Int = 0,
        ) = companyProxyManager.getCompanyProxiesByFilters(
            childCompanyId = childCompanyId,
            parentCompanyId = parentCompanyId,
            frameworks = frameworks,
            reportingPeriods = reportingPeriods,
            chunkSize = size,
            chunkIndex = idx,
        )

        private fun createProxyEntity(
            childCompanyId: UUID = defaultChildCompanyId,
            parentCompanyId: UUID = defaultParentCompanyId,
            framework: String = defaultFramework,
            reportingPeriod: String = defaultReportingPeriod,
        ) = companyProxyRepository.save(
            CompanyProxyEntity(
                childCompanyId = childCompanyId,
                parentCompanyId = parentCompanyId,
                framework = framework,
                reportingPeriod = reportingPeriod,
                uploadTimestamp = 0L,
                uploaderUserId = testUserId,
            ),
        )

        private fun proxyIds(result: List<StoredCompanyProxy>) = result.map { UUID.fromString(it.proxyId) }.toSet()

        @Test
        fun `getCompanyProxiesByFilters supports all filters and pagination`() {
            val entity1 = createProxyEntity(defaultChildCompanyId, defaultParentCompanyId, defaultFramework, "2023")
            val entity2 = createProxyEntity(defaultChildCompanyId, altParentCompanyId, defaultFramework, "2022")
            val entity3 = createProxyEntity(defaultChildCompanyId, defaultParentCompanyId, altFramework, "2023")
            val entity4 = createProxyEntity(altChildCompanyId, altParentCompanyId, altFramework, "2021")

            assertEquals(
                setOf(entity1.proxyId, entity2.proxyId, entity3.proxyId),
                proxyIds(callFilterFunction(childCompanyId = defaultChildCompanyId)),
            )

            assertEquals(
                setOf(entity1.proxyId, entity2.proxyId),
                proxyIds(callFilterFunction(childCompanyId = defaultChildCompanyId, frameworks = setOf(defaultFramework))),
            )

            assertEquals(
                setOf(entity1.proxyId, entity3.proxyId),
                proxyIds(callFilterFunction(childCompanyId = defaultChildCompanyId, reportingPeriods = setOf("2023"))),
            )

            assertEquals(
                setOf(entity1.proxyId),
                proxyIds(
                    callFilterFunction(
                        childCompanyId = defaultChildCompanyId,
                        parentCompanyId = defaultParentCompanyId,
                        frameworks = setOf(defaultFramework),
                        reportingPeriods = setOf("2023"),
                    ),
                ),
            )

            val page0 = callFilterFunction(childCompanyId = defaultChildCompanyId, size = 1, idx = 0)
            val page1 = callFilterFunction(childCompanyId = defaultChildCompanyId, size = 1, idx = 1)
            assertEquals(1, page0.size)
            assertEquals(1, page1.size)
            assertNotEquals(page0.first().proxyId, page1.first().proxyId)

            assertEquals(2, callFilterFunction(childCompanyId = defaultChildCompanyId, size = 2, idx = 0).size)

            assertEquals(setOf(entity4.proxyId), proxyIds(callFilterFunction(childCompanyId = altChildCompanyId)))
        }

        @Test
        fun `getCompanyProxiesByFilters returns empty list if nothing matches`() {
            val result =
                companyProxyManager.getCompanyProxiesByFilters(
                    childCompanyId = UUID.randomUUID(),
                    parentCompanyId = null,
                    frameworks = null,
                    reportingPeriods = null,
                    chunkSize = 10,
                    chunkIndex = 0,
                )
            assertTrue(result.isEmpty())
        }

        @Test
        fun `getCompanyProxyById returns the correct StoredCompanyProxy when found`() {
            val savedEntity = companyProxyManager.addProxyRelation(defaultCompanyProxy, testUserId)
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
            val savedEntity = companyProxyManager.addProxyRelation(proxyRelation, testUserId)

            val updatedStoredProxy = companyProxyManager.editCompanyProxy(savedEntity.proxyId, altCompanyProxy, testUserId)

            val persisted = companyProxyManager.getCompanyProxyById(savedEntity.proxyId)

            val expected =
                StoredCompanyProxy(
                    proxyId = savedEntity.proxyId.toString(),
                    childCompanyId = altChildCompanyId.toString(),
                    parentCompanyId = altParentCompanyId.toString(),
                    framework = altFramework,
                    reportingPeriod = altReportingPeriod,
                    uploadTimestamp = updatedStoredProxy.uploadTimestamp,
                    uploaderUserId = testUserId,
                )
            assertEquals(expected, updatedStoredProxy)
            assertEquals(expected, persisted)
        }

        @Test
        fun `editCompanyProxy throws ResourceNotFoundApiException when proxy does not exist`() {
            val randomProxyId = UUID.randomUUID()
            assertThrows(ResourceNotFoundApiException::class.java) {
                companyProxyManager.editCompanyProxy(randomProxyId, altCompanyProxy, testUserId)
            }
        }

        @Test
        fun `test that deleteProxyRelation deletes a proxy relation`() {
            val savedEntity = companyProxyManager.addProxyRelation(defaultCompanyProxy, testUserId)
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

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
import org.junit.jupiter.api.Assertions.assertNull
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
        private val defaultValidFrom = "2023"
        private val defaultValidTo = "2025"
        private val altChildCompanyId = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val altParentCompanyId = UUID.fromString("00000000-0000-0000-0000-000000000004")
        private val altValidFrom = "2026"
        private val altValidTo: String? = null

        private val defaultCompanyProxy =
            CompanyProxy<UUID>(
                childCompanyId = defaultChildCompanyId,
                parentCompanyId = defaultParentCompanyId,
                validFromReportingPeriod = defaultValidFrom,
                validToReportingPeriod = defaultValidTo,
            )
        private val altCompanyProxy =
            CompanyProxy<UUID>(
                childCompanyId = altChildCompanyId,
                parentCompanyId = altParentCompanyId,
                validFromReportingPeriod = altValidFrom,
                validToReportingPeriod = altValidTo,
            )

        @Test
        fun `test that addProxyRelation creates and persists a new proxy relation`() {
            val proxyRelation = defaultCompanyProxy
            val savedEntity = companyProxyManager.addProxyRelation(proxyRelation, testUserId)
            val retrievedEntity = companyProxyRepository.findAllByChildCompanyId(defaultChildCompanyId).first()

            assertNotNull(retrievedEntity)
            assertEquals(proxyRelation.childCompanyId, retrievedEntity.childCompanyId)
            assertEquals(proxyRelation.parentCompanyId, retrievedEntity.parentCompanyId)
            assertEquals(proxyRelation.validFromReportingPeriod, retrievedEntity.validFromReportingPeriod)
            assertEquals(proxyRelation.validToReportingPeriod, retrievedEntity.validToReportingPeriod)
            assertEquals(testUserId, retrievedEntity.uploaderUserId)
            assertEquals(savedEntity, retrievedEntity)
        }

        @Test
        fun `addProxyRelation persists null validity bounds`() {
            val unbounded = defaultCompanyProxy.copy(validFromReportingPeriod = null, validToReportingPeriod = null)
            val saved = companyProxyManager.addProxyRelation(unbounded, testUserId)
            val retrieved = companyProxyManager.getCompanyProxyById(saved.proxyId)
            assertNull(retrieved.validFromReportingPeriod)
            assertNull(retrieved.validToReportingPeriod)
        }

        private fun callFilterFunction(
            childCompanyId: UUID? = null,
            parentCompanyId: UUID? = null,
            size: Int = 100,
            idx: Int = 0,
        ) = companyProxyManager.getCompanyProxiesByFilters(
            childCompanyId = childCompanyId,
            parentCompanyId = parentCompanyId,
            chunkSize = size,
            chunkIndex = idx,
        )

        private fun createProxyEntity(
            childCompanyId: UUID = defaultChildCompanyId,
            parentCompanyId: UUID = defaultParentCompanyId,
            validFromReportingPeriod: String? = defaultValidFrom,
            validToReportingPeriod: String? = defaultValidTo,
        ) = companyProxyRepository.save(
            CompanyProxyEntity(
                childCompanyId = childCompanyId,
                parentCompanyId = parentCompanyId,
                validFromReportingPeriod = validFromReportingPeriod,
                validToReportingPeriod = validToReportingPeriod,
                uploadTimestamp = 0L,
                uploaderUserId = testUserId,
            ),
        )

        private fun proxyIds(result: List<StoredCompanyProxy>) = result.map { UUID.fromString(it.proxyId) }.toSet()

        @Test
        fun `getCompanyProxiesByFilters supports all filters and pagination`() {
            val entity1 = createProxyEntity(defaultChildCompanyId, defaultParentCompanyId, "2023", "2023")
            val entity2 = createProxyEntity(defaultChildCompanyId, altParentCompanyId, "2022", "2022")
            val entity3 = createProxyEntity(defaultChildCompanyId, defaultParentCompanyId, "2024", "2024")
            val entity4 = createProxyEntity(altChildCompanyId, altParentCompanyId, "2021", "2021")

            assertEquals(
                setOf(entity1.proxyId, entity2.proxyId, entity3.proxyId),
                proxyIds(callFilterFunction(childCompanyId = defaultChildCompanyId)),
            )

            assertEquals(
                setOf(entity1.proxyId, entity3.proxyId),
                proxyIds(callFilterFunction(childCompanyId = defaultChildCompanyId, parentCompanyId = defaultParentCompanyId)),
            )

            assertEquals(
                setOf(entity2.proxyId, entity4.proxyId),
                proxyIds(callFilterFunction(parentCompanyId = altParentCompanyId)),
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
                    validFromReportingPeriod = altValidFrom,
                    validToReportingPeriod = altValidTo,
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

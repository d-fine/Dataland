package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.metainformation.NonSourceabilityRequest
import org.dataland.datalandbackend.repositories.NonSourceabilityDataRepository
import org.dataland.datalandbackend.services.CompanyAlterationManager
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.test.annotation.DirtiesContext
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
@DefaultMocks
class MetaDataControllerNonSourceableTest
    @Autowired
    constructor(
        private val metaDataController: MetaDataController,
        private val companyManager: CompanyAlterationManager,
        private val nonSourceabilityDataRepository: NonSourceabilityDataRepository,
    ) {
        private lateinit var storedCompany: StoredCompanyEntity
        private val adminRoles = DatalandRealmRole.entries.toSet()
        private val uploaderRoles = setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER)
        private val dataType = DataType("eutaxonomy-financials")
        private val reportingPeriod = "2023"

        @BeforeEach
        fun setup() {
            nonSourceabilityDataRepository.deleteAll()
            AuthenticationMock.mockSecurityContext("uploader", "uploaderId", uploaderRoles)
            val companyInfo =
                CompanyInformation(
                    companyName = "TestCo",
                    headquarters = "DE",
                    headquartersPostalCode = "10115",
                    countryCode = "DE",
                    companyContactDetails = emptyList(),
                    companyLegalForm = null,
                    sector = null,
                    website = null,
                    identifiers = emptyMap(),
                    companyAlternativeNames = null,
                    isTeaserCompany = false,
                    parentCompanyLei = null,
                )
            storedCompany = companyManager.addCompany(companyInfo)
        }

        @Test
        fun `POST nonSourceable creates entry with qaStatus Pending when bypassQa is false`() {
            val request =
                NonSourceabilityRequest(
                    companyId = storedCompany.companyId,
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    reason = "No public source",
                    bypassQa = false,
                )
            val response = metaDataController.postNonSourceabilityOfADataset(request)
            assertEquals(QaStatus.Pending, response.body?.qaStatus)
            assertFalse(response.body?.currentlyActive ?: true)
        }

        @Test
        fun `POST nonSourceable creates entry with qaStatus Accepted when bypassQa is true and caller is admin`() {
            AuthenticationMock.mockSecurityContext("admin", "adminId", adminRoles)

            val request =
                NonSourceabilityRequest(
                    companyId = storedCompany.companyId,
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    reason = "Admin bypass",
                    bypassQa = true,
                )
            val response = metaDataController.postNonSourceabilityOfADataset(request)
            assertEquals(QaStatus.Accepted, response.body?.qaStatus)
            assertTrue(response.body?.currentlyActive ?: false)
        }

        @Test
        fun `POST nonSourceable with bypassQa true throws AccessDeniedException for non-admin`() {
            val request =
                NonSourceabilityRequest(
                    companyId = storedCompany.companyId,
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    reason = "Attempting bypass",
                    bypassQa = true,
                )
            assertThrows<AccessDeniedException> {
                metaDataController.postNonSourceabilityOfADataset(request)
            }
        }

        @Test
        fun `GET nonSourceable returns entries filtered by qaStatus`() {
            AuthenticationMock.mockSecurityContext("admin", "adminId", adminRoles)
            metaDataController.postNonSourceabilityOfADataset(
                NonSourceabilityRequest(
                    companyId = storedCompany.companyId,
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    reason = "Test",
                    bypassQa = true,
                ),
            )

            val allResults =
                metaDataController.getInfoOnNonSourceabilityOfDatasets(null, null, null, null)
            val acceptedResults =
                metaDataController.getInfoOnNonSourceabilityOfDatasets(null, null, null, QaStatus.Accepted)
            val pendingResults =
                metaDataController.getInfoOnNonSourceabilityOfDatasets(null, null, null, QaStatus.Pending)

            assertEquals(1, allResults.body?.size)
            assertEquals(1, acceptedResults.body?.size)
            assertEquals(0, pendingResults.body?.size)
        }

        @Test
        fun `HEAD nonSourceable returns 200 for active entry`() {
            AuthenticationMock.mockSecurityContext("admin", "adminId", adminRoles)
            metaDataController.postNonSourceabilityOfADataset(
                NonSourceabilityRequest(
                    companyId = storedCompany.companyId,
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    reason = "Active",
                    bypassQa = true,
                ),
            )
            metaDataController.isDataNonSourceable(storedCompany.companyId, dataType, reportingPeriod)
        }
    }

package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.metainformation.NonSourceabilityRequest
import org.dataland.datalandbackend.repositories.NonSourceabilityDataRepository
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean

/**
 * Tests for NonSourceabilityInformationManager.
 * Covers all four (bypassQa, currentlyActive) combinations, constraint checks,
 * reversal logic, and event emission behaviour.
 */
@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@DefaultMocks
@MockitoBean(types = [CloudEventMessageHandler::class])
class NonSourceabilityInformationManagerTest(
    @Autowired private val nonSourceabilityDataRepository: NonSourceabilityDataRepository,
    @Autowired private val manager: NonSourceabilityInformationManager,
    @Autowired private val companyAlterationManager: CompanyAlterationManager,
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
) {
    private lateinit var companyId: String
    private val dataType = DataType("eutaxonomy-financials")
    private val reportingPeriod = "2023"
    private val uploaderRoles = setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER)
    private val adminRoles = DatalandRealmRole.entries.toSet()

    @BeforeEach
    fun setUp() {
        AuthenticationMock.mockSecurityContext("uploader", "uploaderId", uploaderRoles)
        val storedCompany =
            companyAlterationManager.addCompany(
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
                ),
            )
        companyId = storedCompany.companyId
    }

    private fun request(
        bypassQa: Boolean = false,
        currentlyActive: Boolean = bypassQa,
    ) = NonSourceabilityRequest(
        companyId = companyId,
        dataType = dataType,
        reportingPeriod = reportingPeriod,
        reason = "No source",
        bypassQa = bypassQa,
        currentlyActive = currentlyActive,
    )

    // --- Existing tests (updated) ---

    @Test
    fun `creates pending entry when bypassQa is false`() {
        val result = manager.processNonSourceabilityRequest(request())
        require(result is NonSourceabilityInformationManager.ProcessNonSourceabilityResult.Success)
        val response = result.response
        assertEquals(QaStatus.Pending, response.qaStatus)
        assertFalse(response.currentlyActive)
    }

    @Test
    fun `creates accepted active entry when bypassQa is true`() {
        AuthenticationMock.mockSecurityContext("admin", "adminId", adminRoles)
        val result = manager.processNonSourceabilityRequest(request(bypassQa = true))
        require(result is NonSourceabilityInformationManager.ProcessNonSourceabilityResult.Success)
        val response = result.response
        assertEquals(QaStatus.Accepted, response.qaStatus)
        assertTrue(response.currentlyActive)
    }

    @Test
    fun `duplicate request for Pending entry throws ConflictApiException`() {
        manager.processNonSourceabilityRequest(request())
        assertThrows<ConflictApiException> { manager.processNonSourceabilityRequest(request()) }
    }

    @Test
    fun `duplicate request for Accepted entry throws ConflictApiException`() {
        AuthenticationMock.mockSecurityContext("admin", "adminId", adminRoles)
        manager.processNonSourceabilityRequest(request(bypassQa = true))
        assertThrows<ConflictApiException> { manager.processNonSourceabilityRequest(request(bypassQa = true)) }
    }

    @Test
    fun `new request allowed after Rejected entry FR013 edge case`() {
        val firstResult = manager.processNonSourceabilityRequest(request())
        require(firstResult is NonSourceabilityInformationManager.ProcessNonSourceabilityResult.Success)
        val entity = nonSourceabilityDataRepository.findByFilters(companyId, dataType, reportingPeriod, QaStatus.Pending).first()
        entity.qaStatus = QaStatus.Rejected
        nonSourceabilityDataRepository.save(entity)

        val secondResult = manager.processNonSourceabilityRequest(request())
        require(secondResult is NonSourceabilityInformationManager.ProcessNonSourceabilityResult.Success)
        assertEquals(QaStatus.Pending, secondResult.response.qaStatus)
    }

    @Test
    fun `isCurrentlyActive returns false when no active entry exists`() {
        assertFalse(manager.isCurrentlyActive(companyId, dataType, reportingPeriod))
    }

    @Test
    fun `isCurrentlyActive returns true after admin bypass entry`() {
        AuthenticationMock.mockSecurityContext("admin", "adminId", adminRoles)
        manager.processNonSourceabilityRequest(request(bypassQa = true))
        assertTrue(manager.isCurrentlyActive(companyId, dataType, reportingPeriod))
    }

    @Test
    fun `sc005 guard NonSourceabilityDataRepository is canonical runtime source not SourceabilityDataRepository`() {
        AuthenticationMock.mockSecurityContext("admin", "adminId", adminRoles)
        manager.processNonSourceabilityRequest(request(bypassQa = true))
        val entries = nonSourceabilityDataRepository.findByFilters(companyId, dataType, reportingPeriod, null)
        assertTrue(entries.isNotEmpty(), "NonSourceabilityDataRepository must be the runtime source (SC-005)")
    }

    // --- T004 US4: invalid combination ---

    @Test
    fun `invalid combination bypassQa false currentlyActive true throws InvalidInputApiException`() {
        assertThrows<InvalidInputApiException> {
            manager.processNonSourceabilityRequest(request(bypassQa = false, currentlyActive = true))
        }
    }

    // --- T006-T007 US3: standard QA path constraint checks ---

    @Test
    fun `bypassQa false currentlyActive false rejected when active entry exists throws ConflictApiException`() {
        AuthenticationMock.mockSecurityContext("admin", "adminId", adminRoles)
        manager.processNonSourceabilityRequest(request(bypassQa = true, currentlyActive = true))
        AuthenticationMock.mockSecurityContext("uploader", "uploaderId", uploaderRoles)
        assertThrows<ConflictApiException> {
            manager.processNonSourceabilityRequest(request(bypassQa = false, currentlyActive = false))
        }
    }

    @Test
    fun `bypassQa false currentlyActive false rejected when pending entry exists throws ConflictApiException`() {
        manager.processNonSourceabilityRequest(request(bypassQa = false, currentlyActive = false))
        assertThrows<ConflictApiException> {
            manager.processNonSourceabilityRequest(request(bypassQa = false, currentlyActive = false))
        }
    }

    // --- T009-T010 US2: admin bypass constraint checks ---

    @Test
    fun `bypassQa true currentlyActive true rejected when active entry exists throws ConflictApiException`() {
        AuthenticationMock.mockSecurityContext("admin", "adminId", adminRoles)
        manager.processNonSourceabilityRequest(request(bypassQa = true, currentlyActive = true))
        assertThrows<ConflictApiException> {
            manager.processNonSourceabilityRequest(request(bypassQa = true, currentlyActive = true))
        }
    }

    @Test
    fun `bypassQa true currentlyActive true rejected when pending entry exists throws ConflictApiException`() {
        AuthenticationMock.mockSecurityContext("uploader", "uploaderId", uploaderRoles)
        manager.processNonSourceabilityRequest(request(bypassQa = false, currentlyActive = false))
        AuthenticationMock.mockSecurityContext("admin", "adminId", adminRoles)
        assertThrows<ConflictApiException> {
            manager.processNonSourceabilityRequest(request(bypassQa = true, currentlyActive = true))
        }
    }

    // --- T012-T015 US1: admin reversal ---

    @Test
    fun `bypassQa true currentlyActive false deactivates active entry and returns new entry`() {
        AuthenticationMock.mockSecurityContext("admin", "adminId", adminRoles)
        manager.processNonSourceabilityRequest(request(bypassQa = true, currentlyActive = true))
        val result = manager.processNonSourceabilityRequest(request(bypassQa = true, currentlyActive = false))
        require(result is NonSourceabilityInformationManager.ProcessNonSourceabilityResult.Success)
        val response = result.response
        assertFalse(response.currentlyActive)
        assertEquals(QaStatus.Accepted, response.qaStatus)
        assertFalse(manager.isCurrentlyActive(companyId, dataType, reportingPeriod))
        val allEntries = nonSourceabilityDataRepository.findByFilters(companyId, dataType, reportingPeriod, null)
        assertEquals(2, allEntries.size)
        assertTrue(allEntries.all { !it.currentlyActive })
        assertEquals(2, allEntries.count { it.bypassQa && !it.currentlyActive && it.qaStatus == QaStatus.Accepted })
    }

    @Test
    fun `bypassQa true currentlyActive false returns ConflictApiException when no active entry exists`() {
        AuthenticationMock.mockSecurityContext("admin", "adminId", adminRoles)
        assertThrows<ConflictApiException> {
            manager.processNonSourceabilityRequest(request(bypassQa = true, currentlyActive = false))
        }
    }

    @Test
    fun `bypassQa true currentlyActive false returns ConflictApiException when pending entry exists`() {
        AuthenticationMock.mockSecurityContext("uploader", "uploaderId", uploaderRoles)
        manager.processNonSourceabilityRequest(request(bypassQa = false, currentlyActive = false))
        AuthenticationMock.mockSecurityContext("admin", "adminId", adminRoles)
        assertThrows<ConflictApiException> {
            manager.processNonSourceabilityRequest(request(bypassQa = true, currentlyActive = false))
        }
    }

    @Test
    fun `bypassQa true currentlyActive false does not emit lifecycle event`() {
        AuthenticationMock.mockSecurityContext("admin", "adminId", adminRoles)
        manager.processNonSourceabilityRequest(request(bypassQa = true, currentlyActive = true))
        clearInvocations(cloudEventMessageHandler)
        manager.processNonSourceabilityRequest(request(bypassQa = true, currentlyActive = false))
        verifyNoInteractions(cloudEventMessageHandler)
    }
}

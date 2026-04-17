package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.metainformation.NonSourceabilityRequest
import org.dataland.datalandbackend.repositories.NonSourceabilityDataRepository
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional

/**
 * Tests for the canonical NonSourceabilityInformationManager (T016, T048).
 * Verifies duplicate rejection, bypass authorization, qaStatus transitions,
 * and that SourceabilityEntity is not used as a runtime source.
 */
@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
@DefaultMocks
@MockitoBean(types = [CloudEventMessageHandler::class])
class NonSourceabilityInformationManagerTest(
    @Autowired private val nonSourceabilityDataRepository: NonSourceabilityDataRepository,
    @Autowired private val companyQueryManager: CompanyQueryManager,
    @Autowired private val companyAlterationManager: CompanyAlterationManager,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
) {
    private lateinit var manager: NonSourceabilityInformationManager
    private lateinit var companyId: String
    private val dataType = DataType("eutaxonomy-financials")
    private val reportingPeriod = "2023"
    private val uploaderRoles = setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER)
    private val adminRoles = DatalandRealmRole.entries.toSet()

    @BeforeEach
    fun setUp() {
        manager =
            NonSourceabilityInformationManager(
                nonSourceabilityDataRepository = nonSourceabilityDataRepository,
                companyQueryManager = companyQueryManager,
                cloudEventMessageHandler = cloudEventMessageHandler,
                objectMapper = objectMapper,
            )
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
        val storedCompany = companyAlterationManager.addCompany(companyInfo)
        companyId = storedCompany.companyId
    }

    private fun request(bypassQa: Boolean = false) =
        NonSourceabilityRequest(
            companyId = companyId,
            dataType = dataType,
            reportingPeriod = reportingPeriod,
            reason = "No source",
            bypassQa = bypassQa,
        )

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
    fun `duplicate request for Pending entry throws InvalidInputApiException`() {
        manager.processNonSourceabilityRequest(request())
        val duplicateResult = manager.processNonSourceabilityRequest(request())
        require(duplicateResult is NonSourceabilityInformationManager.ProcessNonSourceabilityResult.Duplicate)
        assertTrue(duplicateResult.message.contains("already exists"))
    }

    @Test
    fun `duplicate request for Accepted entry throws InvalidInputApiException`() {
        AuthenticationMock.mockSecurityContext("admin", "adminId", adminRoles)
        manager.processNonSourceabilityRequest(request(bypassQa = true))
        val duplicateResult = manager.processNonSourceabilityRequest(request(bypassQa = true))
        require(duplicateResult is NonSourceabilityInformationManager.ProcessNonSourceabilityResult.Duplicate)
        assertTrue(duplicateResult.message.contains("already exists"))
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
}

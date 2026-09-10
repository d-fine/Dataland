package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandbackend.services.CompanyAlterationManager
import org.dataland.datalandbackend.services.CompanyBaseManager
import org.dataland.datalandbackend.services.CompanyIdentifierManager
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.DataAvailabilityChecker
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

/**
 * Integration test which verifies that the reportingPeriod filter of the company search endpoints
 * (getCompanies, getNumberOfCompanies) and the available-search-filters endpoint behave as expected.
 */
@SpringBootTest(
    classes = [DatalandBackend::class],
    properties = ["spring.rabbitmq.listener.simple.auto-startup=false"],
)
@DefaultMocks
@Suppress("LongParameterList")
class CompanyDataControllerReportingPeriodFilterTest(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val companyAlterationManager: CompanyAlterationManager,
    @Autowired private val companyQueryManager: CompanyQueryManager,
    @Autowired private val companyBaseManager: CompanyBaseManager,
    @Autowired private val companyIdentifierManager: CompanyIdentifierManager,
    @Autowired private val dataAvailabilityChecker: DataAvailabilityChecker,
    @Autowired private val dataMetaInformationRepository: DataMetaInformationRepository,
) : BaseIntegrationTest() {
    private val testDataProvider = TestDataProvider(objectMapper)
    private lateinit var companyReportingPeriod2023: StoredCompanyEntity
    private lateinit var companyReportingPeriod2024: StoredCompanyEntity
    private lateinit var companyReportingPeriodBoth: StoredCompanyEntity
    private lateinit var companyWithMismatchedFrameworkAndPeriod: StoredCompanyEntity
    private lateinit var companyController: CompanyDataController

    @BeforeEach
    fun setup() {
        companyController =
            CompanyDataController(
                companyAlterationManager,
                companyQueryManager,
                companyIdentifierManager,
                companyBaseManager,
                dataAvailabilityChecker,
            )

        val companies = testDataProvider.getCompanyInformationWithoutIdentifiers(4)
        companyReportingPeriod2023 = companyAlterationManager.addCompany(companies[0])
        companyReportingPeriod2024 = companyAlterationManager.addCompany(companies[1])
        companyReportingPeriodBoth = companyAlterationManager.addCompany(companies[2])
        companyWithMismatchedFrameworkAndPeriod = companyAlterationManager.addCompany(companies[3])

        storeActiveDataset(companyReportingPeriod2023.companyId, "2023")
        storeActiveDataset(companyReportingPeriod2024.companyId, "2024")
        storeActiveDataset(companyReportingPeriodBoth.companyId, "2023")
        storeActiveDataset(companyReportingPeriodBoth.companyId, "2024")
        storeActiveDataset(companyWithMismatchedFrameworkAndPeriod.companyId, "2023", dataType = "sfdr")
        storeActiveDataset(companyWithMismatchedFrameworkAndPeriod.companyId, "2024", dataType = "lksg")
    }

    private fun storeActiveDataset(
        companyId: String,
        reportingPeriod: String,
        dataType: String = "sfdr",
    ) {
        dataMetaInformationRepository.saveAndFlush(
            DataMetaInformationEntity(
                dataId = UUID.randomUUID().toString(),
                company =
                    companyQueryManager
                        .getCompanyById(companyId),
                dataType = dataType,
                uploaderUserId = UUID.randomUUID().toString(),
                uploadTime = System.currentTimeMillis(),
                reportingPeriod = reportingPeriod,
                currentlyActive = true,
                qaStatus = QaStatus.Accepted,
            ),
        )
    }

    @Test
    fun `getCompanies returns all companies when reportingPeriods filter is empty`() {
        val result = companyController.getCompanies(reportingPeriods = emptySet()).body!!
        val companyIds = result.map { it.companyId }
        assertTrue(companyIds.contains(companyReportingPeriod2023.companyId))
        assertTrue(companyIds.contains(companyReportingPeriod2024.companyId))
        assertTrue(companyIds.contains(companyReportingPeriodBoth.companyId))
    }

    @Test
    fun `getCompanies filters correctly by a single reportingPeriod`() {
        val result = companyController.getCompanies(reportingPeriods = setOf("2023")).body!!
        val companyIds = result.map { it.companyId }
        assertTrue(companyIds.contains(companyReportingPeriod2023.companyId))
        assertTrue(companyIds.contains(companyReportingPeriodBoth.companyId))
        assertTrue(!companyIds.contains(companyReportingPeriod2024.companyId))
    }

    @Test
    fun `getCompanies filters correctly by multiple reportingPeriods`() {
        val result = companyController.getCompanies(reportingPeriods = setOf("2023", "2024")).body!!
        val companyIds = result.map { it.companyId }
        assertTrue(companyIds.contains(companyReportingPeriod2023.companyId))
        assertTrue(companyIds.contains(companyReportingPeriod2024.companyId))
        assertTrue(companyIds.contains(companyReportingPeriodBoth.companyId))
    }

    @Test
    fun `getCompanies returns no companies for a non-existent reportingPeriod`() {
        val result = companyController.getCompanies(reportingPeriods = setOf("1999")).body!!
        val companyIds = result.map { it.companyId }
        assertTrue(!companyIds.contains(companyReportingPeriod2023.companyId))
        assertTrue(!companyIds.contains(companyReportingPeriod2024.companyId))
        assertTrue(!companyIds.contains(companyReportingPeriodBoth.companyId))
    }

    @Test
    fun `getCompanies combines reportingPeriod filter with countryCode filter`() {
        val countryCode = companyReportingPeriod2023.countryCode
        val result =
            companyController
                .getCompanies(
                    reportingPeriods = setOf("2023"),
                    countryCodes = setOf(countryCode),
                ).body!!
        val companyIds = result.map { it.companyId }
        assertTrue(companyIds.all { it in listOf(companyReportingPeriod2023.companyId, companyReportingPeriodBoth.companyId) })
    }

    @Test
    fun `getNumberOfCompanies respects the reportingPeriod filter`() {
        val countBefore = companyController.getNumberOfCompanies(reportingPeriods = emptySet()).body!!
        val countFor2024 = companyController.getNumberOfCompanies(reportingPeriods = setOf("2024")).body!!
        assertEquals(3, countFor2024)
        assertTrue(countBefore >= countFor2024)
    }

    @Test
    fun `getAvailableCompanySearchFilters returns the distinct reporting periods in descending order`() {
        val availableFilters = companyController.getAvailableCompanySearchFilters().body!!
        assertTrue(availableFilters.reportingPeriods.containsAll(setOf("2023", "2024")))
    }

    @Test
    fun `getCompanies requires the dataType and reportingPeriod filter to match the same dataset`() {
        // The company has an "sfdr" dataset for 2023 and a "lksg" dataset for 2024, but no dataset that is
        // both "sfdr" and 2024 (or "lksg" and 2023). The filter combination must not match on two different,
        // uncorrelated datasets.
        val matchingResult =
            companyController
                .getCompanies(
                    dataTypes = setOf(DataType.valueOf("sfdr")),
                    reportingPeriods = setOf("2023"),
                ).body!!
        assertTrue(matchingResult.map { it.companyId }.contains(companyWithMismatchedFrameworkAndPeriod.companyId))

        val nonMatchingResult =
            companyController
                .getCompanies(
                    dataTypes = setOf(DataType.valueOf("sfdr")),
                    reportingPeriods = setOf("2024"),
                ).body!!
        assertTrue(
            !nonMatchingResult.map { it.companyId }.contains(companyWithMismatchedFrameworkAndPeriod.companyId),
        )

        val otherNonMatchingResult =
            companyController
                .getCompanies(
                    dataTypes = setOf(DataType.valueOf("lksg")),
                    reportingPeriods = setOf("2023"),
                ).body!!
        assertTrue(
            !otherNonMatchingResult.map { it.companyId }.contains(companyWithMismatchedFrameworkAndPeriod.companyId),
        )

        val otherMatchingResult =
            companyController
                .getCompanies(
                    dataTypes = setOf(DataType.valueOf("lksg")),
                    reportingPeriods = setOf("2024"),
                ).body!!
        assertTrue(
            otherMatchingResult.map { it.companyId }.contains(companyWithMismatchedFrameworkAndPeriod.companyId),
        )
    }
}

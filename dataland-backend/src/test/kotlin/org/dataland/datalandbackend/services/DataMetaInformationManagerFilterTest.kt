package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.frameworks.sfdr.model.SfdrData
import org.dataland.datalandbackend.model.DataDimensionQuery
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
import kotlin.random.Random

@SpringBootTest(
    classes = [DatalandBackend::class],
    properties = ["spring.rabbitmq.listener.simple.auto-startup=false"],
)
@DefaultMocks
class DataMetaInformationManagerFilterTest(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val companyManager: CompanyAlterationManager,
    @Autowired private val dataMetaInformationManager: DataMetaInformationManager,
) : BaseIntegrationTest() {
    private val testDataProvider = TestDataProvider(objectMapper)
    private val defaultDataType = DataType.of(SfdrData::class.java)
    private val defaultReportingPeriod = "2023"
    private val uploaderUserId = "uploader-user-id"
    private lateinit var storedCompany: StoredCompanyEntity

    @BeforeEach
    fun setup() {
        storedCompany = companyManager.addCompany(testDataProvider.getCompanyInformationWithoutIdentifiers(1).first())
    }

    @Test
    fun `check that the resulting data dimensions are as expected when retrieving active datasets`() {
        val singleReportingPeriod = "2020"
        val singleDataType = "lksg"
        val storedCompanies = addCompanyToDatabase(3)
        addMetainformation(company = storedCompanies[0], reportingPeriod = singleReportingPeriod)
        addMetainformation(company = storedCompanies[1])
        addMetainformation(company = storedCompanies[2])
        addMetainformation(company = storedCompanies[0], currentlyActive = null, qaStatus = QaStatus.Rejected)
        addMetainformation(company = storedCompanies[0], dataType = singleDataType)
        val expectedDimensions =
            listOf(
                BasicDataDimensions(
                    companyId = storedCompanies[0].companyId,
                    dataType = defaultDataType.toString(),
                    reportingPeriod = singleReportingPeriod,
                ),
                BasicDataDimensions(
                    companyId = storedCompanies[1].companyId,
                    dataType = defaultDataType.toString(),
                    reportingPeriod = defaultReportingPeriod,
                ),
                BasicDataDimensions(
                    companyId = storedCompanies[0].companyId,
                    dataType = singleDataType,
                    reportingPeriod = defaultReportingPeriod,
                ),
            )
        val combinedSingleFilters =
            dataMetaInformationManager
                .getActiveDataMetaInformationList(
                    DataDimensionQuery(
                        companyIds = listOf(storedCompanies[0].companyId),
                        dataTypes = listOf(defaultDataType.toString()),
                        reportingPeriods = listOf(singleReportingPeriod),
                    ),
                ).map { it.toBasicDataDimensions() }
        assertTrue(combinedSingleFilters.first() == expectedDimensions.first())

        val combinedMultipleFilters =
            dataMetaInformationManager
                .getActiveDataMetaInformationList(
                    DataDimensionQuery(
                        companyIds = listOf(storedCompanies[0].companyId, storedCompanies[1].companyId),
                        dataTypes = listOf(defaultDataType.toString(), singleDataType),
                        reportingPeriods = listOf(singleReportingPeriod, defaultReportingPeriod),
                    ),
                ).map { it.toBasicDataDimensions() }
        assertTrue(combinedMultipleFilters == expectedDimensions)
    }

    @Test
    fun `empty filter returns no active datasets`() {
        addMetainformation()
        val result = dataMetaInformationManager.getActiveDataMetaInformationList(DataDimensionQuery())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filter with all empty lists returns no active datasets`() {
        addMetainformation()
        val result =
            dataMetaInformationManager.getActiveDataMetaInformationList(
                DataDimensionQuery(
                    companyIds = emptyList(),
                    dataTypes = emptyList(),
                    reportingPeriods = emptyList(),
                ),
            )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filter with only companyIds returns all active datasets for that company`() {
        val companies = addCompanyToDatabase(2)
        addMetainformation(company = companies[0], reportingPeriod = "2022")
        addMetainformation(company = companies[0], dataType = "lksg", reportingPeriod = "2023")
        addMetainformation(company = companies[1])
        val result =
            dataMetaInformationManager
                .getActiveDataMetaInformationList(
                    DataDimensionQuery(companyIds = listOf(companies[0].companyId)),
                ).map { it.toBasicDataDimensions() }
        assertEquals(2, result.size)
        assertTrue(result.all { it.companyId == companies[0].companyId })
    }

    @Test
    fun `empty list is wildcard for unset filter dimensions`() {
        val companies = addCompanyToDatabase(2)
        addMetainformation(company = companies[0], reportingPeriod = "2022")
        addMetainformation(company = companies[0], dataType = "lksg", reportingPeriod = "2023")
        addMetainformation(company = companies[1])
        val companyIds = listOf(companies[0].companyId)
        val resultWithDefaults =
            dataMetaInformationManager
                .getActiveDataMetaInformationList(
                    DataDimensionQuery(companyIds = companyIds),
                ).map { it.toBasicDataDimensions() }
        val resultWithEmptyList =
            dataMetaInformationManager
                .getActiveDataMetaInformationList(
                    DataDimensionQuery(
                        companyIds = companyIds,
                        dataTypes = emptyList(),
                        reportingPeriods = emptyList(),
                    ),
                ).map { it.toBasicDataDimensions() }
        assertEquals(resultWithDefaults, resultWithEmptyList)
        assertEquals(2, resultWithDefaults.size)
        assertTrue(resultWithDefaults.all { it.companyId == companies[0].companyId })
    }

    @Test
    fun `filter with non-matching values returns empty result`() {
        val company = addCompanyToDatabase(1).first()
        addMetainformation(company = company, reportingPeriod = "2023")
        val result =
            dataMetaInformationManager.getActiveDataMetaInformationList(
                DataDimensionQuery(
                    companyIds = listOf(company.companyId),
                    reportingPeriods = listOf("2024"),
                ),
            )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `dataset with currentlyActive false is excluded`() {
        val company = addCompanyToDatabase(1).first()
        addMetainformation(company = company, currentlyActive = true)
        addMetainformation(company = company, currentlyActive = false)
        addMetainformation(company = company, currentlyActive = null)
        val result =
            dataMetaInformationManager.getActiveDataMetaInformationList(
                DataDimensionQuery(companyIds = listOf(company.companyId)),
            )
        assertEquals(1, result.size)
        assertEquals(true, result.first().currentlyActive)
    }

    @Test
    fun `filter uses independent IN matching not exact triple matching`() {
        val companies = addCompanyToDatabase(2)
        val companyA = companies[0]
        val companyB = companies[1]
        addMetainformation(company = companyA, dataType = defaultDataType.toString(), reportingPeriod = "2022")
        addMetainformation(company = companyA, dataType = "lksg", reportingPeriod = "2023")
        addMetainformation(company = companyB, dataType = defaultDataType.toString(), reportingPeriod = "2023")
        addMetainformation(company = companyB, dataType = "lksg", reportingPeriod = "2022")
        val result =
            dataMetaInformationManager
                .getActiveDataMetaInformationList(
                    DataDimensionQuery(
                        companyIds = listOf(companyA.companyId, companyB.companyId),
                        dataTypes = listOf(defaultDataType.toString(), "lksg"),
                        reportingPeriods = listOf("2022", "2023"),
                    ),
                ).map { it.toBasicDataDimensions() }
        assertEquals(4, result.size)
        assertTrue(result.any { it == BasicDataDimensions(companyA.companyId, defaultDataType.toString(), "2022") })
        assertTrue(result.any { it == BasicDataDimensions(companyA.companyId, "lksg", "2023") })
        assertTrue(result.any { it == BasicDataDimensions(companyB.companyId, defaultDataType.toString(), "2023") })
        assertTrue(result.any { it == BasicDataDimensions(companyB.companyId, "lksg", "2022") })
    }

    private fun addCompanyToDatabase(numberOfCompanies: Int): List<StoredCompanyEntity> =
        testDataProvider
            .getCompanyInformationWithoutIdentifiers(numberOfCompanies)
            .map { companyManager.addCompany(it) }

    @Suppress("LongParameterList")
    @SuppressWarnings("kotlin:S107")
    private fun addMetainformation(
        dataId: String? = null,
        company: StoredCompanyEntity? = null,
        userId: String? = null,
        uploadTime: Long? = null,
        dataType: String? = null,
        reportingPeriod: String? = null,
        currentlyActive: Boolean? = true,
        qaStatus: QaStatus? = null,
    ): DataMetaInformationEntity =
        dataMetaInformationManager.storeDataMetaInformation(
            DataMetaInformationEntity(
                dataId = dataId ?: UUID.randomUUID().toString(),
                company = company ?: storedCompany,
                dataType = dataType ?: defaultDataType.toString(),
                uploaderUserId = userId ?: uploaderUserId,
                uploadTime = uploadTime ?: Random.nextLong(),
                reportingPeriod = reportingPeriod ?: defaultReportingPeriod,
                currentlyActive = currentlyActive,
                qaStatus = qaStatus ?: QaStatus.Accepted,
            ),
        )
}

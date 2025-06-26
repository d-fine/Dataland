package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import javax.sql.DataSource

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class DataAvailabilityCheckerTest {
    companion object {
        private const val REPORTING_PERIOD = "2023"
        private const val DATA_TYPE = "sfdr"
        private const val COMPANY_ID = "12345-67890-12345-67890"
    }

    @Autowired
    private lateinit var dataSource: DataSource

    @Autowired
    private lateinit var dataMetaInformationRepository: DataMetaInformationRepository

    @Autowired
    private lateinit var storedCompanyRepository: StoredCompanyRepository
    private lateinit var dataAvailabilityChecker: DataAvailabilityChecker

    private val dummyCompany =
        StoredCompanyEntity(
            companyId = COMPANY_ID,
            companyName = "Test Company",
            countryCode = "DE",
            headquarters = "Berlin",
            identifiers = mutableListOf(),
            dataRegisteredByDataland = mutableListOf(),
            isTeaserCompany = false,
            website = null,
            parentCompanyLei = null,
            companyAlternativeNames = null,
            companyContactDetails = null,
            sector = null,
            sectorCodeWz = null,
            companyLegalForm = null,
            headquartersPostalCode = null,
        )

    @BeforeEach
    fun setUp() {
        dataAvailabilityChecker = DataAvailabilityChecker(dataSource)
        storedCompanyRepository.saveAndFlush(dummyCompany)
    }

    @ParameterizedTest
    @CsvSource(
        "1234, $DATA_TYPE, $REPORTING_PERIOD",
        "$COMPANY_ID, dummy, $REPORTING_PERIOD",
        "$COMPANY_ID, $DATA_TYPE, 1234",
    )
    fun `check that data dimensions are filtered out correctly`(
        companyId: String,
        dataType: String,
        reportingPeriod: String,
    ) {
        storeMetaData(dataType = dataType, reportingPeriod = reportingPeriod)
        val results =
            dataAvailabilityChecker.getMetaDataOfActiveDatasets(
                listOf(
                    BasicDataDimensions(
                        companyId = companyId,
                        dataType = dataType,
                        reportingPeriod = reportingPeriod,
                    ),
                ),
            )
        assert(results.isEmpty())
    }

    @Test
    fun `check that the availability check returns active datasets as expected`() {
        storeMetaData()
        storeMetaData(currentlyActive = false)
        val results =
            dataAvailabilityChecker.getMetaDataOfActiveDatasets(
                listOf(
                    BasicDataDimensions(
                        companyId = COMPANY_ID,
                        dataType = DATA_TYPE,
                        reportingPeriod = REPORTING_PERIOD,
                    ),
                ),
            )
        assert(results.isNotEmpty()) { "Results should not be empty" }
        assert(results.size == 1) { "Results should not be empty" }
    }

    @Test
    fun `check that multiple dimensions are retrieved correctly`() {
        val year = "2024"
        val framework = "lksg"
        storeMetaData()
        storeMetaData(currentlyActive = false)
        storeMetaData(reportingPeriod = year)
        storeMetaData(dataType = framework)
        storeMetaData(dataType = framework, reportingPeriod = year, currentlyActive = false)

        val expectedDimensions =
            listOf(
                BasicDataDimensions(companyId = COMPANY_ID, dataType = DATA_TYPE, reportingPeriod = REPORTING_PERIOD),
                BasicDataDimensions(companyId = COMPANY_ID, dataType = DATA_TYPE, reportingPeriod = year),
                BasicDataDimensions(companyId = COMPANY_ID, dataType = framework, reportingPeriod = REPORTING_PERIOD),
            )

        val unexpectedDimensions =
            listOf(
                BasicDataDimensions(companyId = COMPANY_ID, dataType = framework, reportingPeriod = year),
                BasicDataDimensions(companyId = UUID.randomUUID().toString(), dataType = DATA_TYPE, reportingPeriod = REPORTING_PERIOD),
                BasicDataDimensions(companyId = COMPANY_ID, dataType = DATA_TYPE, reportingPeriod = "2020"),
            )

        val results = dataAvailabilityChecker.getMetaDataOfActiveDatasets(expectedDimensions + unexpectedDimensions)

        assert(results.size == 3) { "Incorrect number of datasets found." }
        val resultingDimensions = results.map { BasicDataDimensions(it.companyId, it.dataType.toString(), it.reportingPeriod) }
        assert(resultingDimensions == expectedDimensions)
    }

    private fun storeMetaData(
        dataType: String? = DATA_TYPE,
        reportingPeriod: String? = REPORTING_PERIOD,
        currentlyActive: Boolean? = true,
    ) {
        dataMetaInformationRepository.saveAndFlush(
            DataMetaInformationEntity(
                dataId = UUID.randomUUID().toString(),
                company = dummyCompany,
                dataType = dataType ?: DATA_TYPE,
                reportingPeriod = reportingPeriod ?: REPORTING_PERIOD,
                currentlyActive = currentlyActive ?: true,
                uploadTime = System.currentTimeMillis(),
                qaStatus = QaStatus.Accepted,
                uploaderUserId = UUID.randomUUID().toString(),
            ),
        )
    }
}

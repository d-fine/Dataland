package org.dataland.datalandbackend.services.datapoints

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.model.DataDimensionQuery
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest(
    classes = [DatalandBackend::class],
    properties = ["spring.rabbitmq.listener.simple.auto-startup=false"],
)
@DefaultMocks
class DataPointMetaInformationManagerFilterTest(
    @Autowired private val dataPointMetaInformationManager: DataPointMetaInformationManager,
) : BaseIntegrationTest() {
    private val defaultCompanyId = UUID.randomUUID().toString()
    private val defaultDataPointType = "extendedDateFiscalYearEnd"
    private val defaultReportingPeriod = "2023"
    private val uploaderUserId = "uploader-user-id"
    private var uploadTimeCounter = 0L

    @Test
    fun `empty filter returns no active data points`() {
        addDataPointMetaInformation()
        val result = dataPointMetaInformationManager.getActiveDataPointMetaInformationList(DataDimensionQuery())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filter with all empty lists returns no active data points`() {
        addDataPointMetaInformation()
        val result =
            dataPointMetaInformationManager.getActiveDataPointMetaInformationList(
                DataDimensionQuery(
                    companyIds = emptyList(),
                    dataTypes = emptyList(),
                    reportingPeriods = emptyList(),
                ),
            )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filter with only companyIds returns all active data points for that company`() {
        val companyA = UUID.randomUUID().toString()
        val companyB = UUID.randomUUID().toString()
        addDataPointMetaInformation(companyId = companyA, reportingPeriod = "2022")
        addDataPointMetaInformation(companyId = companyA, dataPointType = "annualRevenue", reportingPeriod = "2023")
        addDataPointMetaInformation(companyId = companyB)
        val result =
            dataPointMetaInformationManager
                .getActiveDataPointMetaInformationList(
                    DataDimensionQuery(companyIds = listOf(companyA)),
                ).map { it.toBasicDataPointDimensions() }
        assertEquals(2, result.size)
        assertTrue(result.all { it.companyId == companyA })
    }

    @Test
    fun `empty list is wildcard for unset filter dimensions`() {
        val companyA = UUID.randomUUID().toString()
        val companyB = UUID.randomUUID().toString()
        addDataPointMetaInformation(companyId = companyA, reportingPeriod = "2022")
        addDataPointMetaInformation(companyId = companyA, dataPointType = "annualRevenue", reportingPeriod = "2023")
        addDataPointMetaInformation(companyId = companyB)
        val companyIds = listOf(companyA)
        val resultWithDefaults =
            dataPointMetaInformationManager
                .getActiveDataPointMetaInformationList(
                    DataDimensionQuery(companyIds = companyIds),
                ).map { it.toBasicDataPointDimensions() }
        val resultWithEmptyList =
            dataPointMetaInformationManager
                .getActiveDataPointMetaInformationList(
                    DataDimensionQuery(
                        companyIds = companyIds,
                        dataTypes = emptyList(),
                        reportingPeriods = emptyList(),
                    ),
                ).map { it.toBasicDataPointDimensions() }
        assertEquals(resultWithDefaults, resultWithEmptyList)
        assertEquals(2, resultWithDefaults.size)
        assertTrue(resultWithDefaults.all { it.companyId == companyA })
    }

    @Test
    fun `filter with non-matching reporting period returns empty result`() {
        val company = UUID.randomUUID().toString()
        addDataPointMetaInformation(companyId = company, reportingPeriod = "2023")
        val result =
            dataPointMetaInformationManager.getActiveDataPointMetaInformationList(
                DataDimensionQuery(
                    companyIds = listOf(company),
                    reportingPeriods = listOf("2024"),
                ),
            )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `data point with currentlyActive false is excluded`() {
        val company = UUID.randomUUID().toString()
        addDataPointMetaInformation(companyId = company, currentlyActive = true)
        addDataPointMetaInformation(companyId = company, currentlyActive = false)
        addDataPointMetaInformation(companyId = company, currentlyActive = null)
        val result =
            dataPointMetaInformationManager.getActiveDataPointMetaInformationList(
                DataDimensionQuery(companyIds = listOf(company)),
            )
        assertEquals(1, result.size)
        assertEquals(true, result.first().currentlyActive)
    }

    @Test
    fun `filter uses independent IN matching not exact triple matching`() {
        val companyA = UUID.randomUUID().toString()
        val companyB = UUID.randomUUID().toString()
        addDataPointMetaInformation(companyId = companyA, dataPointType = defaultDataPointType, reportingPeriod = "2022")
        addDataPointMetaInformation(companyId = companyA, dataPointType = "annualRevenue", reportingPeriod = "2023")
        addDataPointMetaInformation(companyId = companyB, dataPointType = defaultDataPointType, reportingPeriod = "2023")
        addDataPointMetaInformation(companyId = companyB, dataPointType = "annualRevenue", reportingPeriod = "2022")
        val result =
            dataPointMetaInformationManager
                .getActiveDataPointMetaInformationList(
                    DataDimensionQuery(
                        companyIds = listOf(companyA, companyB),
                        dataTypes = listOf(defaultDataPointType, "annualRevenue"),
                        reportingPeriods = listOf("2022", "2023"),
                    ),
                ).map { it.toBasicDataPointDimensions() }
        assertEquals(4, result.size)
        assertTrue(result.any { it == BasicDataPointDimensions(companyA, defaultDataPointType, "2022") })
        assertTrue(result.any { it == BasicDataPointDimensions(companyA, "annualRevenue", "2023") })
        assertTrue(result.any { it == BasicDataPointDimensions(companyB, defaultDataPointType, "2023") })
        assertTrue(result.any { it == BasicDataPointDimensions(companyB, "annualRevenue", "2022") })
    }

    @Suppress("LongParameterList")
    private fun addDataPointMetaInformation(
        dataPointId: String = UUID.randomUUID().toString(),
        companyId: String = defaultCompanyId,
        dataPointType: String = defaultDataPointType,
        reportingPeriod: String = defaultReportingPeriod,
        currentlyActive: Boolean? = true,
        qaStatus: QaStatus = QaStatus.Accepted,
    ): DataPointMetaInformationEntity =
        dataPointMetaInformationManager.storeDataPointMetaInformation(
            DataPointMetaInformationEntity(
                dataPointId = dataPointId,
                companyId = companyId,
                dataPointType = dataPointType,
                reportingPeriod = reportingPeriod,
                uploaderUserId = uploaderUserId,
                uploadTime = ++uploadTimeCounter,
                currentlyActive = currentlyActive,
                qaStatus = qaStatus,
            ),
        )
}

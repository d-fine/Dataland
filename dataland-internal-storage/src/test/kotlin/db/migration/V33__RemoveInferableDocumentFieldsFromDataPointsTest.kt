package db.migration

import db.migration.utils.JsonUtils
import org.dataland.datalandbackendutils.services.utils.BaseFlywayMigrationTest
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandinternalstorage.entities.DataPointItem
import org.dataland.datalandinternalstorage.repositories.DataPointItemRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest(classes = [org.dataland.datalandinternalstorage.DatalandInternalStorage::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Suppress("ClassName")
@Transactional
class V33__RemoveInferableDocumentFieldsFromDataPointsTest : BaseFlywayMigrationTest() {
    companion object {
        const val ORIGINAL_JSON = "V33/original.json"
    }

    @Autowired
    lateinit var dataPointItemRepository: DataPointItemRepository

    private lateinit var dataPointId: String

    override fun getFlywayBaselineVersion(): String = "32"

    override fun getFlywayTargetVersion(): String = "33"

    override fun setupBeforeMigration() {
        dataPointId = UUID.randomUUID().toString()
        dataPointItemRepository.save(
            DataPointItem(
                dataPointId = dataPointId,
                companyId = UUID.randomUUID().toString(),
                dataPointType = "plainSfdrHighImpactClimateSectors",
                reportingPeriod = "2023",
                dataPoint =
                    defaultObjectMapper.writeValueAsString(
                        JsonUtils.readJsonFromResourcesFile(ORIGINAL_JSON).toString(),
                    ),
            ),
        )
    }

    @Test
    fun `check that fileName and publicationDate are removed from all data source objects`() {
        val storedDataPoint = dataPointItemRepository.findById(dataPointId).get().dataPoint
        val unwrappedJson = defaultObjectMapper.readValue(storedDataPoint, String::class.java)
        val migratedDataPoint = defaultObjectMapper.readTree(unwrappedJson)

        val energyConsumption = migratedDataPoint["NaceCodeA"]["highImpactClimateSectorEnergyConsumptionInGWh"]["dataSource"]
        assertFalse(energyConsumption.has("fileName"))
        assertFalse(energyConsumption.has("publicationDate"))
        assertEquals("14", energyConsumption["page"].asText())
        assertEquals("someTag", energyConsumption["tagName"].asText())
        assertEquals(
            "378b5971a4583bc5028fc7d67b03d82d07272e5c3db650a5316442d0cdeaf883",
            energyConsumption["fileReference"].asText(),
        )

        val energyConsumptionPerRevenue =
            migratedDataPoint["NaceCodeA"]["highImpactClimateSectorEnergyConsumptionInGWhPerMillionEURRevenue"]["dataSource"]
        assertFalse(energyConsumptionPerRevenue.has("fileName"))
        assertFalse(energyConsumptionPerRevenue.has("publicationDate"))
        assertEquals("revenueRef1", energyConsumptionPerRevenue["fileReference"].asText())
        assertEquals("3", energyConsumptionPerRevenue["page"].asText())

        val secondSectorDataSource =
            migratedDataPoint["NaceCodeB"]["highImpactClimateSectorEnergyConsumptionInGWh"]["dataSource"]
        assertFalse(secondSectorDataSource.has("fileName"))
        assertFalse(secondSectorDataSource.has("publicationDate"))
        assertEquals("abcd1234", secondSectorDataSource["fileReference"].asText())

        val malformedDataSource =
            migratedDataPoint["NaceCodeC"]["highImpactClimateSectorEnergyConsumptionInGWh"]["dataSource"]
        assertFalse(malformedDataSource.has("fileName"))
        assertFalse(malformedDataSource.has("publicationDate"))

        val fieldsWithValueNull = migratedDataPoint["NaceCodeD"]["highImpactClimateSectorEnergyConsumptionInGWh"]["dataSource"]
        assertFalse(fieldsWithValueNull.has("fileName"))
        assertFalse(fieldsWithValueNull.has("publicationDate"))
    }
}

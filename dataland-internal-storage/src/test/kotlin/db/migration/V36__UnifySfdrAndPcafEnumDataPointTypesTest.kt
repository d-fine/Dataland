package db.migration

import org.dataland.datalandbackendutils.services.utils.BaseFlywayMigrationTest
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandinternalstorage.entities.DataPointItem
import org.dataland.datalandinternalstorage.repositories.DataPointItemRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
class V36__UnifySfdrAndPcafEnumDataPointTypesTest : BaseFlywayMigrationTest() {
    companion object {
        private const val SFDR_SECTOR = "extendedEnumSfdrMainPcafSector"
        private const val PCAF_SECTOR = "extendedEnumPcafMainSector"
        private const val SFDR_EXCHANGE_STATUS = "extendedEnumSfdrCompanyExchangeStatus"
        private const val PCAF_EXCHANGE_STATUS = "extendedEnumCompanyExchangeStatus"

        private val companyWithSfdrOnly = UUID.randomUUID().toString()
        private val companyWithCollision = UUID.randomUUID().toString()
        private val companyWithPcafOnly = UUID.randomUUID().toString()

        private val savedItems = mutableMapOf<String, DataPointItem>()
    }

    @Autowired
    lateinit var dataPointItemRepository: DataPointItemRepository

    override fun getFlywayBaselineVersion(): String = "35"

    override fun getFlywayTargetVersion(): String = "36"

    private fun save(
        key: String,
        companyId: String,
        reportingPeriod: String,
        dataPointType: String,
        value: String,
    ) {
        val item =
            DataPointItem(
                dataPointId = UUID.randomUUID().toString(),
                companyId = companyId,
                reportingPeriod = reportingPeriod,
                dataPointType = dataPointType,
                dataPoint = defaultObjectMapper.writeValueAsString("{\"value\":\"$value\",\"quality\":\"Reported\"}"),
            )
        dataPointItemRepository.save(item)
        savedItems[key] = item
    }

    override fun setupBeforeMigration() {
        save("sfdrOnlySector", companyWithSfdrOnly, "2023", SFDR_SECTOR, "Public")
        save("sfdrOnlyExchangeStatus", companyWithSfdrOnly, "2023", SFDR_EXCHANGE_STATUS, "Listed")

        save("collisionSfdrSector", companyWithCollision, "2023", SFDR_SECTOR, "Energy")
        save("collisionPcafSector", companyWithCollision, "2023", PCAF_SECTOR, "Utilities")
        save("collisionSfdrExchangeStatus", companyWithCollision, "2023", SFDR_EXCHANGE_STATUS, "Listed")
        save("collisionPcafExchangeStatus", companyWithCollision, "2023", PCAF_EXCHANGE_STATUS, "Unlisted")
        save("pcafInOtherPeriod", companyWithCollision, "2024", PCAF_SECTOR, "Materials")

        save("pcafOnlySector", companyWithPcafOnly, "2023", PCAF_SECTOR, "HealthCare")
    }

    private fun assertMigrated(
        key: String,
        expectedType: String,
    ) {
        val original = savedItems.getValue(key)
        val migrated = dataPointItemRepository.findById(original.dataPointId).get()
        assertEquals(expectedType, migrated.dataPointType)
        assertEquals(original.dataPoint, migrated.dataPoint)
        assertEquals(original.companyId, migrated.companyId)
        assertEquals(original.reportingPeriod, migrated.reportingPeriod)
    }

    @Test
    fun `check that SFDR enum data point types are renamed to the PCAF ones with unchanged values`() {
        assertMigrated("sfdrOnlySector", PCAF_SECTOR)
        assertMigrated("sfdrOnlyExchangeStatus", PCAF_EXCHANGE_STATUS)
    }

    @Test
    fun `check that the SFDR data point is kept and the PCAF data point is deleted in case of a collision`() {
        assertMigrated("collisionSfdrSector", PCAF_SECTOR)
        assertMigrated("collisionSfdrExchangeStatus", PCAF_EXCHANGE_STATUS)
        assertTrue(dataPointItemRepository.findById(savedItems.getValue("collisionPcafSector").dataPointId).isEmpty)
        assertTrue(dataPointItemRepository.findById(savedItems.getValue("collisionPcafExchangeStatus").dataPointId).isEmpty)
    }

    @Test
    fun `check that PCAF data points without an SFDR counterpart are not touched`() {
        assertMigrated("pcafInOtherPeriod", PCAF_SECTOR)
        assertMigrated("pcafOnlySector", PCAF_SECTOR)
    }

    @Test
    fun `check that no data point of the SFDR enum types is left`() {
        val testCompanies = setOf(companyWithSfdrOnly, companyWithCollision, companyWithPcafOnly)
        val remainingTypes =
            dataPointItemRepository
                .findAll()
                .filter { it.companyId in testCompanies }
                .map { it.dataPointType }
                .toSet()
        assertTrue(SFDR_SECTOR !in remainingTypes)
        assertTrue(SFDR_EXCHANGE_STATUS !in remainingTypes)
    }
}

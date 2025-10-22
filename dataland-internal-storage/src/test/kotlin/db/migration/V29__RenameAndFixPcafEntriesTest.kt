package db.migration

import db.migration.utils.JsonUtils
import org.dataland.datalandbackendutils.services.utils.BaseFlywayMigrationTest
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandinternalstorage.entities.DataPointItem
import org.dataland.datalandinternalstorage.repositories.DataPointItemRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(classes = [org.dataland.datalandinternalstorage.DatalandInternalStorage::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Suppress("ClassName")
@Transactional
class V29__RenameAndFixPcafEntriesTest : BaseFlywayMigrationTest() {
    companion object {
        const val ORIGINAL_JSON = "V29/original.json"

        private val expectedRenaming =
            mapOf(
                "customEnumPcafMainSector" to "extendedEnumPcafMainSector",
                "customEnumCompanyExchangeStatus" to "extendedEnumCompanyExchangeStatus",
                "other" to "other",
            )
    }

    @Autowired
    lateinit var dataPointItemRepository: DataPointItemRepository

    override fun getFlywayBaselineVersion(): String = "28"

    override fun setupBeforeMigration() {
        expectedRenaming.keys
            .map {
                DataPointItem(
                    dataPointId = it,
                    companyId = "dummy-company-id",
                    dataPointType = it,
                    reportingPeriod = "2023",
                    dataPoint =
                        defaultObjectMapper.writeValueAsString(
                            JsonUtils.readJsonFromResourcesFile(ORIGINAL_JSON).toString(),
                        ),
                )
            }.let {
                dataPointItemRepository.saveAll(it)
            }
    }

    @Test
    fun `check correct renaming`() {
        expectedRenaming.forEach { (oldType, newType) ->
            val migratedDataPointType = dataPointItemRepository.findById(oldType).get().dataPointType
            Assertions.assertEquals(newType, migratedDataPointType)
        }
    }

    @Test fun `check removal of provider-field`() {
        expectedRenaming.keys.forEach {
            val migratedDataPoint = dataPointItemRepository.findById(it).get().dataPoint
            defaultObjectMapper.readTree(migratedDataPoint).let { jsonNode ->
                Assertions.assertFalse(jsonNode.has("provider"))
            }
        }
    }
}

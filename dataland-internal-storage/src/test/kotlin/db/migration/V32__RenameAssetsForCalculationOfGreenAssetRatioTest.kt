package db.migration

import db.migration.utils.JsonUtils
import org.dataland.datalandbackendutils.services.utils.BaseFlywayMigrationTest
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandinternalstorage.DatalandInternalStorage
import org.dataland.datalandinternalstorage.entities.DataPointItem
import org.dataland.datalandinternalstorage.repositories.DataPointItemRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest(classes = [DatalandInternalStorage::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Suppress("ClassName")
@Transactional
class V32__RenameAssetsForCalculationOfGreenAssetRatioTest : BaseFlywayMigrationTest() {
    companion object {
        const val ORIGINAL_JSON = "V32/original.json"

        private val expectedRenaming = V32__RenameAssetsForCalculationOfGreenAssetRatio.renameMap

        private lateinit var expectedDataPointTypes: Map<String, String>

        private lateinit var expectedDataPoints: Map<String, String>
    }

    @Autowired
    lateinit var dataPointItemRepository: DataPointItemRepository

    override fun getFlywayBaselineVersion(): String = "31"

    override fun getFlywayTargetVersion(): String = "32"

    override fun setupBeforeMigration() {
        expectedRenaming.keys
            .map {
                DataPointItem(
                    dataPointId = UUID.randomUUID().toString(),
                    companyId = UUID.randomUUID().toString(),
                    dataPointType = it,
                    reportingPeriod = "2023",
                    dataPoint =
                        defaultObjectMapper.writeValueAsString(
                            JsonUtils.readJsonFromResourcesFile(ORIGINAL_JSON).toString(),
                        ),
                )
            }.let {
                dataPointItemRepository.saveAll(it)
                expectedDataPointTypes =
                    it.associate { item ->
                        item.dataPointId to expectedRenaming.getValue(item.dataPointType)
                    }
                expectedDataPoints = it.associate { item -> item.dataPointId to item.dataPoint }
            }
    }

    @Test
    fun `check correct renaming`() {
        expectedDataPointTypes.forEach { (id, expectedType) ->
            val migratedDataPointType = dataPointItemRepository.findById(id).get().dataPointType
            Assertions.assertEquals(expectedType, migratedDataPointType)
        }
    }
}

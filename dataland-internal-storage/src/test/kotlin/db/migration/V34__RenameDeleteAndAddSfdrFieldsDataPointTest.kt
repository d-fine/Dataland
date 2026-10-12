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
import java.util.UUID

@SpringBootTest(classes = [org.dataland.datalandinternalstorage.DatalandInternalStorage::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Suppress("ClassName")
@Transactional
class V34__RenameDeleteAndAddSfdrFieldsDataPointTest : BaseFlywayMigrationTest() {
    companion object {
        const val ORIGINAL_JSON = "V34/originalDataPoint.json"
        private lateinit var renamedDataPointIds: Map<String, String>
        private lateinit var deletedDataPointIds: List<String>
    }

    @Autowired
    lateinit var dataPointItemRepository: DataPointItemRepository

    override fun getFlywayBaselineVersion(): String = "33"

    override fun getFlywayTargetVersion(): String = "34"

    override fun setupBeforeMigration() {
        val dataPointJson =
            defaultObjectMapper.writeValueAsString(
                JsonUtils.readJsonFromResourcesFile(ORIGINAL_JSON).toString(),
            )

        renamedDataPointIds =
            V34__RenameDeleteAndAddSfdrFields.dataPointTypeRenameMap.keys.associate { sourceType ->
                val dataPointId = UUID.randomUUID().toString()
                dataPointItemRepository.save(
                    DataPointItem(
                        dataPointId = dataPointId,
                        companyId = UUID.randomUUID().toString(),
                        reportingPeriod = "2023",
                        dataPointType = sourceType,
                        dataPoint = dataPointJson,
                    ),
                )
                dataPointId to sourceType
            }

        deletedDataPointIds =
            V34__RenameDeleteAndAddSfdrFields.deletedDataPointTypes.map { sourceType ->
                val dataPointId = UUID.randomUUID().toString()
                dataPointItemRepository.save(
                    DataPointItem(
                        dataPointId = dataPointId,
                        companyId = UUID.randomUUID().toString(),
                        reportingPeriod = "2023",
                        dataPointType = sourceType,
                        dataPoint = dataPointJson,
                    ),
                )
                dataPointId
            }
    }

    @Test
    fun `check that renamed data point types are updated`() {
        renamedDataPointIds.forEach { (dataPointId, sourceType) ->
            val migratedDataPointType = dataPointItemRepository.findById(dataPointId).get().dataPointType
            val expectedType = V34__RenameDeleteAndAddSfdrFields.dataPointTypeRenameMap.getValue(sourceType)
            Assertions.assertEquals(expectedType, migratedDataPointType)
        }
    }

    @Test
    fun `check that deleted data point types are removed`() {
        deletedDataPointIds.forEach { dataPointId ->
            Assertions.assertTrue(dataPointItemRepository.findById(dataPointId).isEmpty)
        }
    }
}

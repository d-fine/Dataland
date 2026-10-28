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
class V35__UpdatePcafSectorsAndRemoveEstimationsTest : BaseFlywayMigrationTest() {
    companion object {
        const val ORIGINAL_JSON = "V35/originalDataPoint.json"
        private const val UNCHANGED_SECTOR = "Energy"

        private lateinit var sectorDataPointIds: Map<String, Pair<String, String>>
        private lateinit var unchangedSectorDataPointId: String
        private lateinit var deletedDataPointIds: List<String>
    }

    @Autowired
    lateinit var dataPointItemRepository: DataPointItemRepository

    override fun getFlywayBaselineVersion(): String = "34"

    override fun getFlywayTargetVersion(): String = "35"

    private fun saveDataPoint(
        dataPointType: String,
        sector: String?,
    ): String {
        val dataPointId = UUID.randomUUID().toString()
        val json = JsonUtils.readJsonFromResourcesFile(ORIGINAL_JSON)
        sector?.let { json.put("value", it) }
        dataPointItemRepository.save(
            DataPointItem(
                dataPointId = dataPointId,
                companyId = UUID.randomUUID().toString(),
                reportingPeriod = "2023",
                dataPointType = dataPointType,
                dataPoint = defaultObjectMapper.writeValueAsString(json.toString()),
            ),
        )
        return dataPointId
    }

    override fun setupBeforeMigration() {
        sectorDataPointIds =
            V35__UpdatePcafSectorsAndRemoveEstimations.sectorDataPointTypes
                .flatMap { type ->
                    V35__UpdatePcafSectorsAndRemoveEstimations.sectorValueMap.map { (old, new) ->
                        saveDataPoint(type, old) to (old to new)
                    }
                }.toMap()
        unchangedSectorDataPointId = saveDataPoint("extendedEnumPcafMainSector", UNCHANGED_SECTOR)
        deletedDataPointIds =
            V35__UpdatePcafSectorsAndRemoveEstimations.deletedDataPointTypes.map { saveDataPoint(it, null) }
    }

    private fun readValue(dataPointId: String): String =
        defaultObjectMapper
            .readTree(
                defaultObjectMapper.readValue(
                    dataPointItemRepository.findById(dataPointId).get().dataPoint,
                    String::class.java,
                ),
            ).get("value")
            .asText()

    @Test
    fun `check that obsolete sector values are replaced`() {
        sectorDataPointIds.forEach { (id, values) -> Assertions.assertEquals(values.second, readValue(id)) }
    }

    @Test
    fun `check that other sector values stay untouched`() {
        Assertions.assertEquals(UNCHANGED_SECTOR, readValue(unchangedSectorDataPointId))
    }

    @Test
    fun `check that estimated data point types are removed`() {
        deletedDataPointIds.forEach { Assertions.assertTrue(dataPointItemRepository.findById(it).isEmpty) }
    }
}

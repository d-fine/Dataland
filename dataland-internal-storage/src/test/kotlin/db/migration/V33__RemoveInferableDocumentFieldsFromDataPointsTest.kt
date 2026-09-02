package db.migration

import db.migration.utils.JsonUtils
import org.dataland.datalandbackendutils.services.utils.BaseFlywayMigrationTest
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandinternalstorage.entities.DataPointItem
import org.dataland.datalandinternalstorage.repositories.DataPointItemRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
                dataPointType = "someDataPointType",
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

        val topLevelDataSource = migratedDataPoint["dataSource"]
        assertFalse(topLevelDataSource.has("fileName"))
        assertFalse(topLevelDataSource.has("publicationDate"))
        assertEquals("14", topLevelDataSource["page"].asText())
        assertEquals("someTag", topLevelDataSource["tagName"].asText())
        assertEquals(
            "378b5971a4583bc5028fc7d67b03d82d07272e5c3db650a5316442d0cdeaf883",
            topLevelDataSource["fileReference"].asText(),
        )

        val nestedDataSource = migratedDataPoint["nested"]["dataSource"]
        assertFalse(nestedDataSource.has("fileName"))
        assertFalse(nestedDataSource.has("publicationDate"))
        assertEquals("abcd1234", nestedDataSource["fileReference"].asText())

        val listDataSource = migratedDataPoint["listOfSources"][0]["dataSource"]
        assertFalse(listDataSource.has("fileName"))
        assertFalse(listDataSource.has("publicationDate"))
        assertTrue(listDataSource.has("fileReference"))
        assertEquals("3", listDataSource["page"].asText())
    }
}

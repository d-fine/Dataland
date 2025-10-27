package db.migration

import com.fasterxml.jackson.module.kotlin.readValue
import db.migration.utils.JsonUtils
import org.dataland.datalandbackendutils.services.utils.BaseFlywayMigrationTest
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandinternalstorage.entities.DataPointItem
import org.dataland.datalandinternalstorage.repositories.DataPointItemRepository
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@SpringBootTest(classes = [org.dataland.datalandinternalstorage.DatalandInternalStorage::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Suppress("ClassName")
@Transactional
class V30__MigratePlainDatesToExtendedDatesTest : BaseFlywayMigrationTest() {
    companion object {
        const val TRANSFORMED_JSON_FOLDER = "V30"

        private val expectedRenamingReversed =
            mapOf(
                "extendedDateFiscalYearEnd" to "plainDateFiscalYearEnd",
                "extendedEnumFiscalYearDeviation" to "plainEnumFiscalYearDeviation",
            )
    }

    @Autowired
    private lateinit var dataPointItemRepository: DataPointItemRepository

    override fun setupBeforeMigration() {
        val expectedTransformedDataPoints =
            expectedRenamingReversed.keys
                .map {
                    DataPointItem(
                        dataPointId = UUID.randomUUID().toString(),
                        companyId = UUID.randomUUID().toString(),
                        dataPointType = it,
                        reportingPeriod = "2023",
                        dataPoint =
                            defaultObjectMapper.writeValueAsString(
                                JsonUtils.readJsonFromResourcesFile("$TRANSFORMED_JSON_FOLDER/$it.json").toString(),
                            ),
                    )
                }
        expectedTransformedDataPoints.forEach {
            dataPointItemRepository.save(
                DataPointItem(
                    dataPointId = it.dataPointId,
                    companyId = it.companyId,
                    dataPointType = expectedRenamingReversed[it.dataPointType]!!,
                    reportingPeriod = "2023",
                    dataPoint = defaultObjectMapper.readValue<ExtendedDataPoint>(it.dataPoint).value,
                ),
            )
        }
    }

    override fun getFlywayBaselineVersion(): String = "29"

    override fun getFlywayTargetVersion(): String = "30"

    inner class DataSource(
        val page: String,
        val tagName: String?,
        val fileName: String,
        val fileReference: String,
        val publicationDate: LocalDate,
    )

    inner class ExtendedDataPoint(
        val value: String,
        val quality: String,
        val comment: String,
        val dataSource: DataSource,
    )

    //
}

package db.migration

import com.fasterxml.jackson.module.kotlin.readValue
import db.migration.utils.JsonUtils
import org.dataland.datalandbackendutils.services.utils.BaseFlywayMigrationTest
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandinternalstorage.entities.DataPointItem
import org.dataland.datalandinternalstorage.repositories.DataPointItemRepository
import org.junit.jupiter.api.Assertions.assertEquals
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
class V30__MigratePlainDatesToExtendedDatesTest : BaseFlywayMigrationTest() {
    companion object {
        const val TRANSFORMED_JSON_FOLDER = "V30"

        private const val EXTENDED_DATE_TYPE = "extendedDateFiscalYearEnd"
        private const val EXTENDED_ENUM_TYPE = "extendedEnumFiscalYearDeviation"

        private val expectedRenamingReversed =
            mapOf(
                EXTENDED_DATE_TYPE to "plainDateFiscalYearEnd",
                EXTENDED_ENUM_TYPE to "plainEnumFiscalYearDeviation",
            )

        private val dataPointIdMap = expectedRenamingReversed.mapValues { UUID.randomUUID().toString() }
    }

    @Autowired
    private lateinit var dataPointItemRepository: DataPointItemRepository

    data class DataSource(
        val page: String,
        val tagName: String?,
        val fileName: String,
        val fileReference: String,
        val publicationDate: String,
    )

    data class ExtendedDataPoint(
        val value: String,
        val quality: String?,
        val comment: String?,
        val dataSource: DataSource?,
    )

    private fun exceptionMessage(dataPointType: String) = "The data point type $dataPointType is not a map key."

    private fun toPlainDataPoint(extendedDataPoint: DataPointItem): DataPointItem =
        DataPointItem(
            dataPointId = extendedDataPoint.dataPointId,
            companyId = extendedDataPoint.companyId,
            dataPointType =
                expectedRenamingReversed.getOrElse(extendedDataPoint.dataPointType) {
                    throw IllegalArgumentException(exceptionMessage(extendedDataPoint.dataPointType))
                },
            reportingPeriod = extendedDataPoint.reportingPeriod,
            dataPoint =
                "\"\\\"" +
                    defaultObjectMapper
                        .readValue<ExtendedDataPoint>(
                            defaultObjectMapper
                                .readValue<String>(
                                    extendedDataPoint.dataPoint,
                                ),
                        ).value +
                    "\\\"\"",
        )

    private fun extractExtendedDataPointFromJson(
        extendedDataPointType: String,
        companyId: String = UUID.randomUUID().toString(),
    ): DataPointItem =
        DataPointItem(
            dataPointId =
                dataPointIdMap.getOrElse(extendedDataPointType) {
                    throw IllegalArgumentException(exceptionMessage(extendedDataPointType))
                },
            companyId = companyId,
            dataPointType = extendedDataPointType,
            reportingPeriod = "2023",
            dataPoint =
                defaultObjectMapper.writeValueAsString(
                    JsonUtils
                        .readJsonFromResourcesFile("$TRANSFORMED_JSON_FOLDER/$extendedDataPointType.json")
                        .toString(),
                ),
        )

    override fun getFlywayBaselineVersion(): String = "29"

    override fun getFlywayTargetVersion(): String = "30"

    override fun setupBeforeMigration() {
        val expectedTransformedDataPoints =
            expectedRenamingReversed.keys.map { extractExtendedDataPointFromJson(it) }
        expectedTransformedDataPoints.forEach {
            dataPointItemRepository.save(toPlainDataPoint(it))
        }
    }

    @Test
    fun `check correct migration of plain data points`() {
        val migratedDateDataPoint =
            dataPointItemRepository
                .findById(
                    dataPointIdMap.getOrElse(EXTENDED_DATE_TYPE) {
                        throw IllegalArgumentException(exceptionMessage(EXTENDED_DATE_TYPE))
                    },
                ).get()
        assertEquals(
            extractExtendedDataPointFromJson(EXTENDED_DATE_TYPE, migratedDateDataPoint.companyId),
            migratedDateDataPoint,
        )
        val migratedEnumDataPoint =
            dataPointItemRepository
                .findById(
                    dataPointIdMap.getOrElse(EXTENDED_ENUM_TYPE) {
                        throw IllegalArgumentException(exceptionMessage(EXTENDED_ENUM_TYPE))
                    },
                ).get()
        assertEquals(
            extractExtendedDataPointFromJson(EXTENDED_ENUM_TYPE, migratedEnumDataPoint.companyId),
            migratedEnumDataPoint,
        )
    }
}

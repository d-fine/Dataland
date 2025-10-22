package db.migration

import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.DataPointMetaInformationRepository
import org.dataland.datalandbackend.services.CompanyAlterationManager
import org.dataland.datalandbackend.services.MessageQueuePublications
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.services.utils.BaseFlywayMigrationTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.UUID

@SpringBootTest(classes = [org.dataland.datalandbackend.DatalandBackend::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Suppress("ClassName")
class V10__RenamePcafDatapointsTest : BaseFlywayMigrationTest() {
    @Autowired
    lateinit var companyAlterationManager: CompanyAlterationManager

    @Autowired
    lateinit var dataPointMetaInformationRepository: DataPointMetaInformationRepository

    @MockitoBean
    lateinit var ignored: MessageQueuePublications

    private val expectedRenaming =
        mapOf(
            "customEnumPcafMainSector" to "extendedEnumPcafMainSector",
            "customEnumCompanyExchangeStatus" to "extendedEnumCompanyExchangeStatus",
            "other" to "other",
        )
    private lateinit var dummyCompanyId: String
    private lateinit var metaDataBeforeMigration: Map<String, DataPointMetaInformationEntity>

    override fun getFlywayBaselineVersion(): String = "9"

    override fun setupBeforeMigration() {
        dummyCompanyId =
            companyAlterationManager
                .addCompany(
                    CompanyInformation(
                        companyName = "Dummy Company",
                        headquarters = "Nowhere",
                        countryCode = "US",
                        identifiers = mapOf(IdentifierType.Lei to listOf("1234567890ABCDEFGH12)")),
                    ),
                ).companyId

        metaDataBeforeMigration =
            expectedRenaming.keys
                .map {
                    dataPointMetaInformationRepository.save(createDummyMetaData(it))
                }.associateBy { it.dataPointId }
    }

    private fun createDummyMetaData(dataPointType: String) =
        DataPointMetaInformationEntity(
            dataPointId = UUID.randomUUID().toString(),
            companyId = dummyCompanyId,
            dataPointType = dataPointType,
            uploaderUserId = "dummy-uploader",
            uploadTime = System.currentTimeMillis(),
            reportingPeriod = "2023",
            currentlyActive = true,
            qaStatus = QaStatus.Pending,
        )

    @Test
    fun `verify migration script renames PCAF data points correctly`() {
        val migratedMetaInfo =
            metaDataBeforeMigration.keys.map {
                dataPointMetaInformationRepository.findById(it).get()
            }

        migratedMetaInfo.forEach { newMetaInfo ->
            val expectation =
                metaDataBeforeMigration[newMetaInfo.dataPointId].let { oldMetaInfo ->
                    oldMetaInfo?.copy(dataPointType = expectedRenaming.getValue(oldMetaInfo.dataPointType))
                }
            Assertions.assertEquals(expectation, newMetaInfo)
        }
    }
}

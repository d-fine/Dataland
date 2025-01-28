package org.dataland.datalandqaservice.services

import jakarta.transaction.Transactional
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataAndQaReportMetadata
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.QaReportRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReportMetadataService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.IdUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest(
    classes = [DatalandQaService::class],
    properties = ["spring.profiles.active=nodb"],
)
class QaReportMetadataServiceTest(
    @Autowired private val qaReportMetadataService: QaReportMetadataService,
    @Autowired private val qaReportRepository: QaReportRepository,
) {
    private val companyController: CompanyDataControllerApi = Mockito.mock(CompanyDataControllerApi::class.java)
    private val metadataController: MetaDataControllerApi = Mockito.mock(MetaDataControllerApi::class.java)

    val reporterId1: UUID = UUID.randomUUID()
    val reporterId2: UUID = UUID.randomUUID()
    val companyIdentifier = "companyIdentifier"
    val companyId = "companyId"
    val dataId1 = "dataId1"
    val dataId2 = "dataId2"

    @BeforeAll
    fun insertTestData() {
        val now = Instant.now()
        qaReportRepository.save(
            QaReportEntity(
                qaReportId = IdUtils.generateUUID(),
                qaReport = UUID.randomUUID().toString(),
                dataId = dataId1,
                dataType = "sfdr",
                reporterUserId = reporterId1.toString(),
                uploadTime = now.toEpochMilli(),
                active = true,
            ),
        )
        qaReportRepository.save(
            QaReportEntity(
                qaReportId = IdUtils.generateUUID(),
                qaReport = UUID.randomUUID().toString(),
                dataId = dataId2,
                dataType = "sfdr",
                reporterUserId = reporterId2.toString(),
                uploadTime = now.minus(7, ChronoUnit.DAYS).toEpochMilli(),
                active = true,
            ),
        )
    }

    @Test
    fun `check that non unique company ids throw an exception`() {
        val matchingCompanyIdsAndNamesOnDataland: List<CompanyIdAndName> =
            listOf(
                CompanyIdAndName("1", companyId),
                CompanyIdAndName("2", "2"),
            )
        Mockito
            .`when`(companyController.getCompaniesBySearchString(companyIdentifier))
            .thenReturn(matchingCompanyIdsAndNamesOnDataland)
        val thrown =
            assertThrows<InvalidInputApiException> {
                qaReportMetadataService.searchDataAndQaReportMetadata(null, true, null, null, null, companyIdentifier)
            }
        assertEquals(
            "Multiple companies have been found for the identifier you specified. " +
                "Please specify a unique company identifier.",
            thrown.message,
        )
    }

    @Test
    fun `check that an empty list is returned when searching for non existing company`() {
        val result: List<DataAndQaReportMetadata> =
            qaReportMetadataService
                .searchDataAndQaReportMetadata(null, true, null, null, null, companyIdentifier)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `search active reports without additional filter`() {
        val dataMetaInformation: List<DataMetaInformation> =
            listOf(
                DataMetaInformation(dataId1, companyId, DataTypeEnum.sfdr, 1, "test", true, QaStatus.Accepted, null),
                DataMetaInformation(dataId2, companyId, DataTypeEnum.sfdr, 1, "test", true, QaStatus.Accepted, null),
            )
        Mockito
            .`when`(metadataController.getListOfDataMetaInfo(null, null, false, null, null, null))
            .thenReturn(dataMetaInformation)
        val result: List<DataAndQaReportMetadata> =
            qaReportMetadataService
                .searchDataAndQaReportMetadata(null, true, null, null, null, null)
        assertEquals(2, result.size)
    }

    @Test
    fun `search by userId`() {
        val matchingCompanyIdsAndNamesOnDataland: List<CompanyIdAndName> = listOf(CompanyIdAndName("1", companyId))
        Mockito
            .`when`(companyController.getCompaniesBySearchString(companyIdentifier))
            .thenReturn(matchingCompanyIdsAndNamesOnDataland)
        val dataMetaInformation: List<DataMetaInformation> =
            listOf(
                DataMetaInformation(
                    dataId1,
                    companyId,
                    DataTypeEnum.sfdr,
                    1,
                    "test",
                    true,
                    QaStatus.Accepted,
                    reporterId1.toString(),
                ),
            )
        Mockito
            .`when`(metadataController.getListOfDataMetaInfo(null, null, false, null, setOf<UUID>(reporterId1), null))
            .thenReturn(dataMetaInformation)
        val result: List<DataAndQaReportMetadata> =
            qaReportMetadataService
                .searchDataAndQaReportMetadata(setOf<UUID>(reporterId1), true, null, null, null, null)
        assertEquals(1, result.size)
        assertEquals(reporterId1.toString(), result[0].dataMetadata.uploaderUserId)
    }

    @Test
    fun `search only inactive cases`() {
        val matchingCompanyIdsAndNamesOnDataland: List<CompanyIdAndName> = listOf(CompanyIdAndName("1", companyId))
        Mockito
            .`when`(companyController.getCompaniesBySearchString(companyIdentifier))
            .thenReturn(matchingCompanyIdsAndNamesOnDataland)
        val dataMetaInformation: List<DataMetaInformation> = emptyList()
        Mockito
            .`when`(metadataController.getListOfDataMetaInfo(null, null, false, null, setOf<UUID>(reporterId1), null))
            .thenReturn(dataMetaInformation)
        val result: List<DataAndQaReportMetadata> =
            qaReportMetadataService
                .searchDataAndQaReportMetadata(null, false, null, null, null, null)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `search closed date range`() {
        val dataMetaInformation: List<DataMetaInformation> =
            listOf(
                DataMetaInformation(dataId1, companyId, DataTypeEnum.sfdr, 1, "test", true, QaStatus.Accepted, null),
                DataMetaInformation(dataId2, companyId, DataTypeEnum.sfdr, 1, "test", true, QaStatus.Accepted, null),
            )
        Mockito
            .`when`(metadataController.getListOfDataMetaInfo(null, null, false, null, null, null))
            .thenReturn(dataMetaInformation)
        val result: List<DataAndQaReportMetadata> =
            qaReportMetadataService
                .searchDataAndQaReportMetadata(
                    null,
                    true,
                    null,
                    LocalDate.now().minusDays(8),
                    LocalDate.now().plusDays(2),
                    null,
                )
        assertEquals(2, result.size)
    }

    @Test
    fun `search semi open date ranges`() {
        val dataMetaInformation: List<DataMetaInformation> =
            listOf(
                DataMetaInformation(dataId1, companyId, DataTypeEnum.sfdr, 1, "test", true, QaStatus.Accepted, null),
                DataMetaInformation(dataId2, companyId, DataTypeEnum.sfdr, 1, "test", true, QaStatus.Accepted, null),
            )
        Mockito
            .`when`(metadataController.getListOfDataMetaInfo(null, null, false, null, null, null))
            .thenReturn(dataMetaInformation)

        val resultOnlyStart: List<DataAndQaReportMetadata> =
            qaReportMetadataService
                .searchDataAndQaReportMetadata(
                    null, true, null, LocalDate.now().minusDays(3), null, null,
                )
        assertEquals(1, resultOnlyStart.size)
        assertEquals(dataId1, resultOnlyStart[0].qaReportMetadata.dataId)

        val resultOnlyEnd: List<DataAndQaReportMetadata> =
            qaReportMetadataService
                .searchDataAndQaReportMetadata(
                    null, true, null, null, LocalDate.now().minusDays(3), null,
                )
        assertEquals(1, resultOnlyEnd.size)
        assertEquals(dataId2, resultOnlyEnd[0].qaReportMetadata.dataId)
    }
}

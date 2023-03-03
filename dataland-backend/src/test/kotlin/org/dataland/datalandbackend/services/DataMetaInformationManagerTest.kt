package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.enums.data.QAStatus
import org.dataland.datalandbackend.model.lksg.LksgData
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException

@SpringBootTest(classes = [DatalandBackend::class])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class DataMetaInformationManagerTest(
    @Autowired private val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired private val companyManager: CompanyManager,
    @Autowired private val objectMapper: ObjectMapper,
) {
    private val testDataProvider = TestDataProvider(objectMapper)

    @Test
    fun `check that an exception is thrown when non existing company id is provided in meta data search`() {
        val nonExistingCompanyId = "nonExistingCompanyId"
        val thrown = assertThrows<ResourceNotFoundApiException> {
            dataMetaInformationManager.searchDataMetaInfo(
                companyId = nonExistingCompanyId,
                dataType = null,
                showOnlyActive = true,
                reportingPeriod = "",
            )
        }
        assertEquals(
            "Dataland does not know the company ID nonExistingCompanyId",
            thrown.message,
        )
    }

    @Test
    fun `check that an exception is thrown when non existing data id is provided to get meta data`() {
        val nonExistingDataId = "nonExistingDataId"
        val thrown = assertThrows<ResourceNotFoundApiException> {
            dataMetaInformationManager.getDataMetaInformationByDataId(dataId = nonExistingDataId)
        }
        assertEquals(
            "No dataset with the id: nonExistingDataId could be found in the data store.",
            thrown.message,
        )
    }

    @Test
    fun `check that an exception is thrown when two meta-data entries are uploaded simultaneously`() {
        val companyInformation = testDataProvider.getCompanyInformation(1).first()
        val company = companyManager.addCompany(companyInformation)

        dataMetaInformationManager.storeDataMetaInformation(
            DataMetaInformationEntity(
                "data-id-1",
                company,
                DataType.of(LksgData::class.java).toString(),
                "uploader-user-id",
                0,
                "reporting-period",
                null,
                QAStatus.Accepted,
            ),
        )

        assertThrows<DataIntegrityViolationException> {
            dataMetaInformationManager.storeDataMetaInformation(
                DataMetaInformationEntity(
                    "data-id-2",
                    company,
                    DataType.of(LksgData::class.java).toString(),
                    "uploader-user-id",
                    0,
                    "reporting-period",
                    null,
                    QAStatus.Accepted,
                ),
            )
        }
    }
}

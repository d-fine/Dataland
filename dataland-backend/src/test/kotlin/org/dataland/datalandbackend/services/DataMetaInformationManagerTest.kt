package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.interfaces.DataMetaInformationManagerInterface
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import javax.transaction.Transactional

@SpringBootTest(classes = [DatalandBackend::class])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class DataMetaInformationManagerTest(
    @Autowired val dataMetaInformationManager: DataMetaInformationManagerInterface,
) {

    private fun getNonExistingCompanyIdMessage(requestedCompanyId: String): String {
        return "Dataland does not know the company ID $requestedCompanyId"
    }

    private fun getNonExistingDataIdMessage(requestedDataId: String): String {
        return "Dataland does not know the data ID: $requestedDataId"
    }

    @Test
    fun `check that an exception is thrown when non existing company id is provided in meta data search`() {
        val nonExistingCompanyId = "nonExistingCompanyId"
        val thrown = assertThrows<IllegalArgumentException> {
            dataMetaInformationManager.searchDataMetaInfo(companyId = nonExistingCompanyId)
        }
        assertEquals(
            getNonExistingCompanyIdMessage(nonExistingCompanyId),
            thrown.message
        )
    }

    @Test
    fun `check that an exception is thrown when non existing data id is provided to get meta data`() {
        val nonExistingDataId = "nonExistingCompanyId"
        val thrown = assertThrows<IllegalArgumentException> {
            dataMetaInformationManager.getDataMetaInformationByDataId(dataId = nonExistingDataId)
        }
        assertEquals(
            getNonExistingDataIdMessage(nonExistingDataId),
            thrown.message
        )
    }
}

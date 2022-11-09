package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackend.interfaces.DataMetaInformationManagerInterface
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
class DataManagerTest(
    @Autowired val dataMetaInformationManager: DataMetaInformationManagerInterface
) {
    @Test
    fun `check that an exception is thrown when non existing company id is provided in meta data search`() {
        assertThrows<ResourceNotFoundApiException> {
            dataMetaInformationManager.searchDataMetaInfo(companyId = "error")
        }
    }

    @Test
    fun `check that an exception is thrown when non existing data id is provided to get meta data`() {
        assertThrows<ResourceNotFoundApiException> {
            dataMetaInformationManager.getDataMetaInformationByDataId(dataId = "error")
        }
    }
}

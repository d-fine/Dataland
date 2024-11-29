package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.NonSourceableInfo
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class NonSourceableDataManagerTest(
    @Autowired private val nonSourceableDataManager: NonSourceableDataManager,
) {
    @Test
    fun `check that an exception is thrown when non existing company id is provided processing sourcebility storage`() {
        val nonExistingCompanyId = "nonExistingCompanyId"
        val dataType = DataType("eutaxonomy-financials")
        val nonSourceableInfo = NonSourceableInfo(nonExistingCompanyId, dataType, "2023", true, "test reason")
        val thrown =
            assertThrows<ResourceNotFoundApiException> {
                nonSourceableDataManager.processSourceabilityDataStorageRequest(
                    nonSourceableInfo,
                )
            }
        assertEquals(
            "Dataland does not know the company ID nonExistingCompanyId",
            thrown.message,
        )
    }
}

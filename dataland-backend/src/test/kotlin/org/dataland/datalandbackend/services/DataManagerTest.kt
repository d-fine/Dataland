package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.interfaces.DataMetaInformationManagerInterface
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DataManagerTest(
    @Autowired val dataMetaInformationManager: DataMetaInformationManagerInterface
) {
    @Test
    fun `check that an exception is thrown when non existing company id is provided in meta data search`() {
        assertThrows<IllegalArgumentException> {
            dataMetaInformationManager.searchDataMetaInfo(companyId = "error")
        }
    }

    @Test
    fun `check that an exception is thrown when non existing data id is provided to get meta data`() {
        assertThrows<IllegalArgumentException> {
            dataMetaInformationManager.getDataMetaInformationByDataId(dataId = "error")
        }
    }
}

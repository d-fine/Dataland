package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DataManagerTest(
    @Autowired val edcClient: DefaultApi,
    @Autowired val objectMapper: ObjectMapper
) {
    val testCompanyManager = CompanyManager()
    val testDataManager = DataManager(edcClient, objectMapper, testCompanyManager)

    @Test
    fun `check that an exception is thrown when non existing company id is provided in meta data search`() {
        assertThrows<IllegalArgumentException> {
            testDataManager.searchDataMetaInfo(companyId = "error")
        }
    }

    @Test
    fun `check that an exception is thrown when non existing data id is provided to get meta data`() {
        assertThrows<IllegalArgumentException> {
            testDataManager.getDataMetaInfo(dataId = "error")
        }
    }
}

package org.dataland.datalandbackend.service

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
    fun `check that an exception is thrown when company id is provided that does not exist`() {
        assertThrows<IllegalArgumentException> {
            testDataManager.searchDataMetaInfo(companyId = "error")
        }
    }

    @Test
    fun `check that an exception is thrown when a data id is provided that does not exist`() {
        assertThrows<IllegalArgumentException> {
            testDataManager.getDataMetaInfo(dataId = "error")
        }
    }
}

package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DataProcessorTest {

    val testDataProcessor = DataProcessor(edcClient = DefaultApi(basePath = "dummy"))

    @Test
    fun `get the data sets for a company id that does not exist`() {
        assertThrows<IllegalArgumentException> {
            testDataProcessor.searchDataMetaInfo(companyId = "error")
        }
    }
}

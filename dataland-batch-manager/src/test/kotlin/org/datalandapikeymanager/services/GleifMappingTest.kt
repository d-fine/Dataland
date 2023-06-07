package org.datalandapikeymanager.services

import org.dataland.datalandbatchmanager.DatalandBatchManager
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackages = ["org.dataland"])
@SpringBootTest(classes = [DatalandBatchManager::class])
class GleifMappingTest() {
    @Test
    fun `dummy test`() {
        Assertions.assertEquals(
            "Test.",
            "Test.",
            "The test failed.",
        )
    }
}

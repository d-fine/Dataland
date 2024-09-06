package org.datalandapikeymanager.services

import org.dataland.datalandapikeymanager.DatalandDataExporter
import org.dataland.datalanddataexporter.services.CsvExporter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [DatalandDataExporter::class])
class CsvExporterTest(
    @Autowired val testCsvExporter: CsvExporter,
) {
    @Test
    fun `just a dummy`() {
        Assertions.assertEquals(
            "Hello World!",
            testCsvExporter.dummyFunction(),
            "The message is not as expected",
        )
    }
}

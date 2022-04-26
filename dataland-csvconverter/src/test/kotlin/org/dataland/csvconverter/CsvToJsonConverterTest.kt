package org.dataland.csvconverter

import org.junit.jupiter.api.Test
import java.io.File

class CsvToJsonConverterTest {

    @Test
    fun `Read csv and check that the generated objects are as expected`() {
        val csvParser = CsvToJsonConverter(File("./src/test/resources/DatalandTestDaten.csv").path)
        csvParser.writeJson()
    }
}

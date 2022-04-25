package org.dataland.prepoulator

import org.junit.jupiter.api.Test

class CSVParserTest {

    private val dataProvider = DataProvider()

    @Test
    fun `Read csv and check that the generated objects are as expected`() {
        val csvParser = CSVParser(dataProvider.getFile())
        csvParser.writeJson()
    }
}

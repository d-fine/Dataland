package org.dataland.prepoulator

import org.junit.jupiter.api.Test

class CSVParserTest {

    private val dataProvider = DataProvider()

    @Test
    fun `aaaa`() {
        val csvParser = CSVParser(dataProvider.getFile())

        val map = csvParser.readCsv()
        println(map)
    }
}

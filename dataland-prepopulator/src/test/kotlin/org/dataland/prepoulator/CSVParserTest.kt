package org.dataland.prepoulator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource

class CSVParserTest {

    private val dataProvider = DataProvider()

    @Test
    fun `aaaa`() {
        val csvParser = CSVParser(dataProvider.getFile())

        val map = csvParser.readCsv()
        println(map)
    }

}
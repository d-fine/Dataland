package org.dataland.prepoulator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CSVParserTest {


    private val csvParser = CSVParser("C:\\Users\\d92432\\Documents\\Projekte\\02-Dataland\\Fachlich\\dataland_data.csv")

    @Test
    fun `aaa`() {
        val map = csvParser.readCsv()
        println(map)
    }

}
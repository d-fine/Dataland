package org.dataland.prepoulator

// import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
// import org.springframework.beans.factory.annotation.Autowired

class CSVParserTest {

    private val dataProvider = DataProvider()

    @Test
    fun `aaaa`() {
        val csvParser = CSVParser(dataProvider.getFile())
        csvParser.writeJson()
        // csvParser.readJson()

        // val map = csvParser.buildListOfCompanyInformation()
        // println(map)
    }
}

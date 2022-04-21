package org.dataland.prepoulator

import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import java.io.FileReader


class CSVParser(val filePath: String) {

    inline fun <reified T> readCsvFile(fileName: String): List<T> {
        val csvMapper = CsvMapper()
        FileReader(fileName).use { reader ->
            return csvMapper
                .readerFor(T::class.java)
                .with(CsvSchema.emptySchema().withHeader())
                .readValues<T>(reader)
                .readAll()
                .toList()
        }
    }

    fun readCsv(): List<Map<String, String>> {
        return readCsvFile(filePath)
    }

    fun getValue(key: String, mapObject: Map<String, String>): String {
        return if (mapObject[key]!!.isEmpty()) {
            "n/a"
        } else mapObject[key]!!
    }

    fun buildListOfCompanyInformation() {
        val inputList = readCsv()
        for(map in inputList) {
            val companyName = getValue("Unternehmensname", map)
            val headquarters = getValue("Hauptsitz", map)
            /*sector
            marketCap
            reportingDateOfMarketCap
            identifiers
            indices
            val companyInformation = CompanyInformation(companyName, headquarters)*/
        }
    }

    /*
    counter=0
    listOfMaps
    currentDataMap = listOfMaps[counter]

    companyInformation = CompanyInformation(currentDataMap[Unternehmensname] = companyName ...)
    euTaxonomyData = EutaxonomyData( ...)

    to List<companyInformation>        List<exuTaxonomyData>

    List => jsons bauen => in reosurces verschieben
*/
}
package org.dataland.datalandbatchmanager.service

import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.dataland.datalandbatchmanager.model.GleifCompanyInformation
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

@Component
class GleifCsvParser {
    fun getCsvStreamFromZip(zipFile: File): BufferedReader {
        val zipInputStream = ZipInputStream(zipFile.inputStream())
        val zipEntry = zipInputStream.nextEntry
        require(zipEntry?.name?.endsWith(".csv") ?: false)
        { "The downloaded ZIP file does not contain the CSV file in the first position" }

        val inputStreamReader = InputStreamReader(zipInputStream)
        return BufferedReader(inputStreamReader)
    }

    fun readGleifDataFromBufferedReader(bufferedReader: BufferedReader): MappingIterator<GleifCompanyInformation> {
        val schema = CsvSchema.emptySchema().withHeader()

        return CsvMapper()
            .registerModule(kotlinModule())
            .readerFor(GleifCompanyInformation::class.java)
            .with(schema)
            .readValues(bufferedReader)
    }
}
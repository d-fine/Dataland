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

/**
 * Class to read in the zipped CSV file and return buffered GleifCompanyInformation objects
 */
@Component
class GleifCsvParser {
    /**
     * Reads the zipped CSV file and returns the content as buffered reader
     * @param zipFile The file containing the CSV file to be parsed
     * @return the content of the CSV file as buffered reader
     */
    fun getCsvStreamFromZip(zipFile: File): BufferedReader {
        val zipInputStream = ZipInputStream(zipFile.inputStream())
        val zipEntry = zipInputStream.nextEntry
        require(zipEntry?.name?.endsWith(".csv") ?: false) {
            "The downloaded ZIP file does not contain the CSV file in the first position"
        }

        val inputStreamReader = InputStreamReader(zipInputStream)
        return BufferedReader(inputStreamReader)
    }

    /**
     * Transforms the streamed CSV content into an iterable of GleifCompanyInformation objects
     * @param bufferedReader the input stream read from the GLEIF csv file
     * @return An iterable of the corresponding GleifCompanyInformation objects
     */
    fun readGleifDataFromBufferedReader(bufferedReader: BufferedReader): MappingIterator<GleifCompanyInformation> {
        return CsvMapper()
            .registerModule(kotlinModule())
            .readerFor(GleifCompanyInformation::class.java)
            .with(CsvSchema.emptySchema().withHeader())
            .readValues(bufferedReader)
    }
}

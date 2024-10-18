package org.dataland.datalandbatchmanager.service

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.dataland.datalandbatchmanager.model.GleifCompanyInformation
import org.dataland.datalandbatchmanager.model.GleifRelationshipInformation
import org.dataland.datalandbatchmanager.model.NorthDataCompanyInformation
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

/**
 * Class to read in the zipped CSV file and return buffered GleifCompanyInformation objects
 */
@Component
class CsvParser {
    /**
     * Reads the zipped CSV file and returns the content as buffered reader
     * @param zipFile The file containing the CSV file to be parsed
     * @return the content of the CSV file as buffered reader
     */
    fun getCsvStreamFromZip(zipFile: File): BufferedReader {
        val zipInputStream = ZipInputStream(zipFile.inputStream())
        return getCsvFromInputStream(zipInputStream)
    }

    private fun getCsvFromInputStream(zipInputStream: ZipInputStream): BufferedReader {
        var zipEntry = zipInputStream.nextEntry
        var foundCsv = false
        while (zipEntry != null) {
            if (zipEntry.name.endsWith(".csv")) {
                foundCsv = true
                break
            }
            zipEntry = zipInputStream.nextEntry
        }
        require(foundCsv) {
            "The downloaded ZIP file does not contain a CSV file"
        }

        val inputStreamReader = InputStreamReader(zipInputStream, "UTF-8")
        return BufferedReader(inputStreamReader)
    }

    /**
     * Reads the zip file and checks for the contained zipped CSV file and returns the content as buffered reader
     * @param zipFile The zip file containing the zipped CSV file to be parsed
     * @return the content of the CSV file as buffered reader
     */
    fun getCsvStreamFromNorthDataZipFile(zipFile: File): BufferedReader {
        val zipInputStream = ZipInputStream(zipFile.inputStream())
        var zipEntry = zipInputStream.nextEntry
        var foundContainedZip = false
        while (zipEntry != null) {
            if (zipEntry.name.endsWith("M-de.csv.zip")) {
                foundContainedZip = true
                break
            }
            zipEntry = zipInputStream.nextEntry
        }
        require(foundContainedZip) {
            "Could not find zipped CSV file in zip file"
        }
        val zipStreamTwo = ZipInputStream(zipInputStream)
        return getCsvFromInputStream(zipStreamTwo)
    }

    /**
     * Transforms the streamed CSV content into an iterable of objects of class T
     * @param bufferedReader the input stream read from the csv file
     * @return An iterable of the corresponding T objects
     */
    private final inline fun <reified T> readDataFromBufferedReader(bufferedReader: BufferedReader): Iterable<T> =
        Iterable<T> {
            CsvMapper()
                .registerModule(kotlinModule())
                .readerFor(T::class.java)
                .with(CsvSchema.emptySchema().withHeader())
                .readValues(bufferedReader)
        }

    /**
     * Transforms the streamed CSV content into an iterable of objects of GleifRelationshipInformation
     * @param bufferedReader the input stream read from the csv file
     * @return An iterable of the corresponding objects
     */
    fun readGleifRelationshipDataFromBufferedReader(bufferedReader: BufferedReader): Iterable<GleifRelationshipInformation> =
        readDataFromBufferedReader(bufferedReader)

    /**
     * Transforms the streamed CSV content into an iterable of objects of GleifCompanyInformation
     * @param bufferedReader the input stream read from the csv file
     * @return An iterable of the corresponding objects
     */
    fun readGleifCompanyDataFromBufferedReader(bufferedReader: BufferedReader): Iterable<GleifCompanyInformation> =
        readDataFromBufferedReader(bufferedReader)

    /**
     * Transforms the streamed CSV content into an iterable of objects of NorthDataCompanyInformation
     * @param bufferedReader the input stream read from the csv file
     * @return An iterable of the corresponding objects
     */
    fun readNorthDataFromBufferedReader(bufferedReader: BufferedReader): Iterable<NorthDataCompanyInformation> =
        readDataFromBufferedReader(bufferedReader)
}

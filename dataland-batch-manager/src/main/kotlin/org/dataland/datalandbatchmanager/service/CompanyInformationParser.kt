package org.dataland.datalandbatchmanager.service

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.dataland.datalandbatchmanager.model.GleifLEIData
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
class CompanyInformationParser {
    /**
     * Reads the zipped CSV file and returns the content as buffered reader
     * @param zipFile The file containing the CSV file to be parsed
     * @return the content of the CSV file as buffered reader
     */
    fun getCsvStreamFromZip(zipFile: File): BufferedReader {
        val zipInputStream = ZipInputStream(zipFile.inputStream())
        return getDataFromInputStream(zipInputStream)
    }

    /**
     * Reads the zipped XML file and returns the content as buffered reader
     * @param zipFile The file containing the XML file to be parsed
     * @return the content of the XML file as buffered reader
     */
    fun getXmlStreamFromZip(zipFile: File): BufferedReader {
        val zipInputStream = ZipInputStream(zipFile.inputStream())
        return getDataFromInputStream(zipInputStream, ".xml")
    }

    private fun getDataFromInputStream(
        zipInputStream: ZipInputStream,
        fileEnding: String = ".csv",
    ): BufferedReader {
        var zipEntry = zipInputStream.nextEntry
        var foundFileType = false
        while (zipEntry != null) {
            if (zipEntry.name.endsWith(fileEnding)) {
                foundFileType = true
                break
            }
            zipEntry = zipInputStream.nextEntry
        }
        require(foundFileType) {
            "The downloaded ZIP file does not contain a $fileEnding file"
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
        return getDataFromInputStream(zipStreamTwo)
    }

    /**
     * Transforms the streamed CSV content into an iterable of objects of class T
     * @param bufferedReader the input stream read from the csv file
     * @return An iterable of the corresponding T objects
     */
    private final inline fun <reified T> readCsVDataFromBufferedReader(bufferedReader: BufferedReader): Iterable<T> =
        Iterable<T> {
            CsvMapper()
                .registerModule(kotlinModule())
                .readerFor(T::class.java)
                .with(CsvSchema.emptySchema().withHeader())
                .readValues(bufferedReader)
        }

    /**
     * Transforms the streamed xml content into an object of class T
     * @param bufferedReader the input stream read from the xml file
     * @return An iterable of the corresponding T objects
     */
    private final inline fun <reified T> readXmlDataFromBufferedReader(bufferedReader: BufferedReader): T {
        val xmlMapper = XmlMapper().registerModule(kotlinModule())
        return xmlMapper.readValue(bufferedReader, T::class.java)
    }

    /**
     * Transforms the streamed CSV content into an iterable of objects of GleifRelationshipInformation
     * @param bufferedReader the input stream read from the csv file
     * @return An iterable of the corresponding objects
     */
    fun readGleifRelationshipDataFromBufferedReader(bufferedReader: BufferedReader): Iterable<GleifRelationshipInformation> =
        readCsVDataFromBufferedReader(bufferedReader)

    /*fun readGleifCompanyDataFromBufferedReader(bufferedReader: BufferedReader): Iterable<GleifCompanyInformation> =
        readCsVDataFromBufferedReader(bufferedReader)*/

    /**
     * Transforms the streamed xml content into an GleifLEIData object
     * @param bufferedReader the input stream read from the xml file
     * @return The GleifLEIData object
     */
    fun readGleifCompanyDataFromBufferedReader(bufferedReader: BufferedReader): GleifLEIData = readXmlDataFromBufferedReader(bufferedReader)

    /**
     * Transforms the streamed CSV content into an iterable of objects of NorthDataCompanyInformation
     * @param bufferedReader the input stream read from the csv file
     * @return An iterable of the corresponding objects
     */
    fun readNorthDataFromBufferedReader(bufferedReader: BufferedReader): Iterable<NorthDataCompanyInformation> =
        readCsVDataFromBufferedReader(bufferedReader)
}

package org.dataland.datalandbatchmanager.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.net.URI
import java.util.zip.ZipInputStream

/**
 * The class to download the zipped GLEIF golden copy CSV files
 */
@Component
class GleifApiAccessor(
    @Value("\${gleif.download.baseurl}") private val gleifBaseUrl: String,
    @Value("\${gleif.isin.mapping.download.url}") private val isinMappingReferenceUrl: String,
    @Autowired private val externalFileDownload: ExternalFileDownload,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Downloads the golden copy delta file of last month
     * @param targetFile the local target file to be written
     */
    fun getLastMonthGoldenCopyDelta(targetFile: File) {
        downloadFileFromGleif("lei2/latest.csv?delta=LastMonth", targetFile, "Golden Copy Delta File")
    }

    /**
     * Downloads the complete golden copy file
     * @param targetFile the local target file to be written
     */
    fun getFullGoldenCopy(targetFile: File) {
        downloadFileFromGleif("lei2/latest.csv", targetFile, "full Golden Copy File")
    }

    /**
     * Downloads the complete relationship golden copy file
     * @param targetFile the local target file to be written
     */
    fun getFullGoldenCopyOfRelationships(targetFile: File) {
        downloadFileFromGleif("rr/latest.csv", targetFile, "full Golden Copy RR File")
    }

    /**
     * Downloads the latest complete Lei-ISIN mapping file
     * @param targetFile the local target file to be written
     */
    fun getFullIsinMappingFile(targetFile: File) {
        logger.info("Successfully acquired download link for mapping")
        val tempZipFile = File.createTempFile("gleif_mapping_update", ".zip")
        externalFileDownload.downloadIndirectFile(URI(isinMappingReferenceUrl).toURL(), tempZipFile)
        getCsvFileFromZip(tempZipFile, "gleif_mapping_update").copyTo(targetFile, true)
        if (!tempZipFile.delete()) {
            logger.error("Unable to delete file $tempZipFile")
        }
    }

    private fun downloadFileFromGleif(urlSuffx: String, targetFile: File, fileDescription: String) {
        logger.info("Starting download of $fileDescription.")
        val downloadUrl = URI("$gleifBaseUrl/$urlSuffx").toURL()
        externalFileDownload.downloadFile(downloadUrl, targetFile)
        logger.info("Download of $fileDescription completed.")
    }

    /**
     * Extracts CSV file from Zip file
     * @param zipFile the zip file
     * @return CSV file inside Zip file
     */
    private fun getCsvFileFromZip(zipFile: File, prefixForCsvFile: String): File {
        val csvFile = File.createTempFile(prefixForCsvFile, ".csv")
        csvFile.deleteOnExit()

        ZipInputStream(zipFile.inputStream()).use { zipInputStream ->
            val zipEntry = zipInputStream.nextEntry
            require(zipEntry?.name?.endsWith(".csv") ?: false) {
                "The downloaded ZIP file does not contain the CSV file in the first position"
            }

            csvFile.outputStream().use { csvOutputStream ->
                val buffer = ByteArray(ZIP_BUFFER_SIZE)
                var bytesRead: Int

                while (zipInputStream.read(buffer).also { bytesRead = it } != -1) {
                    csvOutputStream.write(buffer, 0, bytesRead)
                }
            }
        }

        return csvFile
    }
}

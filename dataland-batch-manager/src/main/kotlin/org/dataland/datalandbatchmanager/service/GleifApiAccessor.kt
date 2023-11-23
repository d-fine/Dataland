package org.dataland.datalandbatchmanager.service

import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.io.FileUtils
import org.dataland.datalandbatchmanager.service.CompanyUploader.Companion.MAX_RETRIES
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.SocketException
import java.net.URL
import java.util.zip.ZipInputStream

const val ZIP_BUFFER_SIZE = 8192

/**
 * The class to download the zipped GLEIF golden copy CSV files
 */
@Component
class GleifApiAccessor(
    @Qualifier("UnauthenticatedOkHttpClient") private val httpClient: OkHttpClient,
    @Value("\${gleif.download.baseurl}") private val gleifBaseUrl: String,
    @Value("\${gleif.isin.mapping.download.url}") private val isinMappingReferenceUrl: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Downloads the golden copy delta file of last month
     * @param targetFile the local target file to be written
     */
    fun getLastMonthGoldenCopyDelta(targetFile: File) {
        downloadFileFromGleif("latest.csv?delta=LastMonth", targetFile, "Golden Copy Delta File")
    }

    /**
     * Downloads the complete golden copy file
     * @param targetFile the local target file to be written
     */
    fun getFullGoldenCopy(targetFile: File) {
        downloadFileFromGleif("latest.csv", targetFile, "full Golden Copy File")
    }

    /**
     * Downloads the latest complete Lei-ISIN mapping file
     * @param targetFile the local target file to be written
     */
    fun getFullIsinMappingFile(targetFile: File) {
        logger.info("Successfully acquired download link for mapping")
        val tempZipFile = File.createTempFile("gleif_mapping_update", ".zip")
        downloadIndirectFile(URL(isinMappingReferenceUrl), tempZipFile)
        getCsvFileFromZip(tempZipFile).copyTo(targetFile, true)
        if (!tempZipFile.delete()) {
            logger.error("Unable to delete file $tempZipFile")
        }
    }

    /**
     * Extracts CSV file from Zip file
     * @param zipFile the zip file
     * @return CSV file inside Zip file
     */
    fun getCsvFileFromZip(zipFile: File): File {
        val csvFile = File.createTempFile("gleif_mapping_update", ".csv")

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

    private fun downloadFileFromGleif(urlSuffx: String, targetFile: File, fileDescription: String) {
        logger.info("Starting download of $fileDescription.")
        val downloadUrl = URL("$gleifBaseUrl/$urlSuffx")
        downloadFile(downloadUrl, targetFile)
        logger.info("Download of $fileDescription completed.")
    }

    /**
     * Downloads a file and saves it to disk
     * @param url: the target URL of the file
     * @param targetFile: the destination file name where the target file is copied to
     */
    private fun downloadFile(url: URL, targetFile: File) {
        var counter = 0
        while (counter < MAX_RETRIES) {
            try {
                FileUtils.copyURLToFile(url, targetFile)
                logger.info("Successfully saved local copy of the required file.")
                break
            } catch (exception: SocketException) {
                logger.warn("Download attempt failed. Exception was: ${exception.message}.")
                counter++
            }
        }
        if (counter >= MAX_RETRIES) {
            throw FileNotFoundException("Unable to download file behind $url after $MAX_RETRIES attempts.")
        }
    }

    /**
     * Downloads a file from an API endpoint and saves it to disk
     * @param url: the URL triggering the file download
     * @param targetFile: the destination file name where the target file is copied to
     */
    fun downloadIndirectFile(url: URL, targetFile: File) {
        var counter = 0
        fun handleDownloadError(exception: Exception) {
            logger.warn("Download attempt failed. Exception was: ${exception.message}.")
            counter++
        }
        while (counter < MAX_RETRIES) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .build()
                val response = httpClient.newCall(request).execute()
                targetFile.writeBytes(response.body!!.bytes())
                logger.info("Successfully saved local copy of the required file.")
                break
            } catch (exception: SocketException) {
                handleDownloadError(exception)
            } catch (exception: IOException) {
                handleDownloadError(exception)
            }
        }
        if (counter >= MAX_RETRIES) {
            throw FileNotFoundException("Unable to download file behind $url after $MAX_RETRIES attempts.")
        }
    }
}

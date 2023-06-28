package org.dataland.datalandbatchmanager.service

import org.apache.commons.io.FileUtils
import org.dataland.datalandbatchmanager.service.CompanyUploader.Companion.MAX_RETRIES
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileNotFoundException
import java.net.SocketException
import java.net.URL

/**
 * The class to download the zipped GLEIF golden copy CSV files
 */
@Component
class GleifApiAccessor(
    @Value("\${gleif.download.baseurl}") private val gleifBaseUrl: String,
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

    private fun downloadFileFromGleif(urlSuffx: String, targetFile: File, fileDescription: String) {
        logger.info("Starting download of $fileDescription.")
        val downloadUrl = URL("$gleifBaseUrl/$urlSuffx")
        downloadFile(downloadUrl, targetFile)
        logger.info("Download of $fileDescription completed.")
    }

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
}

package org.dataland.datalandbatchmanager.service

import org.apache.commons.io.FileUtils
import org.dataland.datalandbatchmanager.service.CompanyUpload.Companion.MAX_RETRIES
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileNotFoundException
import java.net.SocketException
import java.net.URL

/**
 * The class to download the zipped GLEIF golden copy CSV files
 */
@Component
class GleifApiAccessor {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val gleifBaseUrl = "https://goldencopy.gleif.org/api/v2/golden-copies/publishes/lei2"

    /**
     * Downloads the golden copy delta file of last month
     * @param targetFile the local target file to be written
     */
    fun getLastMonthGoldenCopyDelta(targetFile: File) {
        logger.info("Starting download of Golden Copy Delta File.")
        val deltaUrl = URL("$gleifBaseUrl/latest.csv?delta=LastMonth")
        downloadFile(deltaUrl, targetFile)
        logger.info("Download of Golden Copy Delta File completed.")
    }

    /**
     * Downloads the complete golden copy file
     * @param targetFile the local target file to be written
     */
    fun getFullGoldenCopy(targetFile: File) {
        logger.info("Starting download of full Golden Copy File.")
        val downloadUrl = URL("$gleifBaseUrl/latest.csv")
        downloadFile(downloadUrl, targetFile)
        logger.info("Download of full Golden Copy File completed.")
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

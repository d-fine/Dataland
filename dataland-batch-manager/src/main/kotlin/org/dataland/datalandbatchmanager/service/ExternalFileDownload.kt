package org.dataland.datalandbatchmanager.service

import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.io.FileUtils
import org.dataland.datalandbatchmanager.service.CompanyUploader.Companion.MAX_RETRIES
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.SocketException
import java.net.URL
const val ZIP_BUFFER_SIZE = 8192

/**
 * The utility class to download external files
 */
@Component
class ExternalFileDownload(
    @Qualifier("UnauthenticatedOkHttpClient") private val httpClient: OkHttpClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Downloads a file and saves it to disk
     * @param url: the target URL of the file
     * @param targetFile: the destination file name where the target file is copied to
     */
    fun downloadFile(url: URL, targetFile: File) {
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

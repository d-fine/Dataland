package org.dataland.datalandbatchmanager.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.net.URI

/**
 * The class to download the zipped GLEIF golden copy CSV files
 */
@Component
class NorthDataAccessor(
    @Value("\${northdata.download.baseurl}") private val gleifBaseUrl: String,
    @Autowired private val externalFileDownload: ExternalFileDownload,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Downloads the complete golden copy file
     * @param targetFile the local target file to be written
     */
    fun getFullGoldenCopy(targetFile: File) {
        downloadFileFromNorthdata(
            "testdata20240402-osnabrueck-DE-M-de.csv.zip",
            targetFile,
            "quarterly dataset of german sme from Northdata",
        )
    }

    private fun downloadFileFromNorthdata(urlSuffx: String, targetFile: File, fileDescription: String) {
        logger.info("Starting download of $fileDescription.")
        val downloadUrl = URI("$gleifBaseUrl/$urlSuffx").toURL()
        externalFileDownload.downloadFile(downloadUrl, targetFile)
        logger.info("Download of $fileDescription completed.")
    }
}

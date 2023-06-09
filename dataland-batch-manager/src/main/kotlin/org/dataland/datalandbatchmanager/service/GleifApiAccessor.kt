package org.dataland.datalandbatchmanager.service

import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import java.net.URL

@Component
class GleifApiAccessor {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getLastMonthGoldenCopyDelta(targetFile: File) {
        logger.info("Starting download of Golden Copy Delta File.")
        val deltaUrl = URL("https://goldencopy.gleif.org/api/v2/golden-copies/publishes/lei2/latest.csv?delta=LastMonth")
        FileUtils.copyURLToFile(deltaUrl, targetFile)
        logger.info("Download of Golden Copy Delta File completed.")
    }

    fun getFullGoldenCopy(targetFile: File) {
        logger.info("Starting download of full Golden Copy File.")
        val downloadUrl = URL("https://goldencopy.gleif.org/api/v2/golden-copies/publishes/lei2/latest.csv")
        FileUtils.copyURLToFile(downloadUrl, targetFile)
        logger.info("Download of full Golden Copy File completed.")
    }
}

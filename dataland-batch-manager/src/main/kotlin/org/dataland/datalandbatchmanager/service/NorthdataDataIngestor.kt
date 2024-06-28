package org.dataland.datalandbatchmanager.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File
import java.util.*
import kotlin.time.Duration
import kotlin.time.measureTime

/**
 * Class to execute scheduled tasks, like the import of the GLEIF golden copy files
 * @param northDataAccessor downloads the golden copy files from GLEIF
 * @param gleifParser reads in the csv file from GLEIF and creates GleifCompanyInformation objects
 */
@Suppress("LongParameterList")
@Component
class NorthdataDataIngestor(
    @Autowired private val northDataAccessor: NorthDataAccessor,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Synchronized
    private fun processNorthdataFile(zipFile: File, downloadFile: (file: File) -> Unit) {
        val duration = measureTime {
            try {
                downloadFile(zipFile)
                // TODO function that maps Northdata data to the GLEIF data
            } finally {
                if (!zipFile.delete()) {
                    logger.error("Unable to delete temporary file $zipFile")
                }
            }
        }
        logger.info("Finished processing of Northdata file $zipFile in ${formatExecutionTime(duration)}.")
    }

    private fun formatExecutionTime(duration: Duration): String {
        return duration
            .toComponents { hours, minutes, seconds, _ ->
                String.format(
                    Locale.getDefault(), "%02dh %02dm %02ds", hours, minutes, seconds,
                )
            }
    }
}

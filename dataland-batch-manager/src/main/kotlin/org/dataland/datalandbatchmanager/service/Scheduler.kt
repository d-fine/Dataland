package org.dataland.datalandbatchmanager.service

import org.dataland.datalandbatchmanager.gleif.CompanyUpload
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

const val BOOT_WAIT: Long = 180000

@Component
class Scheduler(
    @Autowired private val apiAccessor: GleifApiAccessor,
    @Autowired private val gleifParser: GleifCsvParser,
    @Autowired private val keycloakTokenManager: KeycloakTokenManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        if (System.getenv("GET_ALL_GLEIF_COMPANIES") == "true") {
            logger.info("Retrieving all company data available via GLEIF.")
            logger.info("Waiting ${BOOT_WAIT}ms to let the backend finish booting before continuing.")
            Thread.sleep(BOOT_WAIT)
            val tempFile = File.createTempFile("gleif_golden_copy", ".csv")
            processFile(tempFile, apiAccessor::getFullGoldenCopy)
        }
    }

    @Suppress("UnusedPrivateMember") // Detect does not recognise the scheduled execution of this function
    @Scheduled(fixedDelay = 1000000000000, initialDelay = BOOT_WAIT)
    // @Scheduled(cron = "0 3 * * 0")
    private fun processDeltaFile() {
        logger.info("Starting update cycle for latest delta file.")
        val tempFile = File.createTempFile("gleif_update_delta", ".csv")
        processFile(tempFile, apiAccessor::getLastMonthGoldenCopyDelta)
    }

    private fun processFile(csvFile: File, downloadFile: (file: File) -> Unit) {
        val start = System.nanoTime()
        try {
            downloadFile(csvFile)
            uploadCompanies(csvFile)
        } finally {
            if (!csvFile.delete()) {
                logger.error("Unable to delete temporary file $csvFile")
            }
        }
        logger.info("Finished processing of file $csvFile in ${getExecutionTime(start)}.")
    }

    private fun uploadCompanies(csvFile: File) {
        val gleifDataStream = gleifParser.getCsvStreamFromZip(csvFile)
        val gleifIterator = gleifParser.readGleifDataFromBufferedReader(gleifDataStream)
        CompanyUpload(keycloakTokenManager).uploadCompanies(gleifIterator.readAll().map { it.toCompanyInformation() })
    }

    private fun getExecutionTime(startTime: Long): String {
        return (System.nanoTime() - startTime)
            .toDuration(DurationUnit.NANOSECONDS)
            .toComponents { hours, minutes, seconds, _ ->
                String.format(
                    Locale.getDefault(), "%02dh %02dm %02ds", hours, minutes, seconds,
                )
            }
    }
}

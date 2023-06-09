package org.dataland.datalandbatchmanager.service

import org.dataland.datalandbatchmanager.gleif.Upload
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Component
class Scheduler(
    @Autowired private val apiAccessor: GleifApiAccessor,
    @Autowired private val gleifParser: GleifCsvParser,
    @Autowired private val keycloakTokenManager: KeycloakTokenManager
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    init{
        if (System.getenv("GET_ALL_GLEIF_COMPANIES") == "true") {
            getAllGleifCompanies()
        }
    }

    private fun getAllGleifCompanies() {
        logger.info("Retrieving all company data available via GLEIF.")
        val start = System.nanoTime()
        val tempFile = File.createTempFile("gleif_golden_copy", ".csv")
        try {
            apiAccessor.getFullGoldenCopy(tempFile)
            uploadCompanies(tempFile)
        } finally {
            tempFile.delete()
        }
        logger.info("Finished processing of all companies in ${getExecutionTime(start)}.")
    }


    @Scheduled(fixedDelay = 1000000000000, initialDelay = 120000)
    private fun processDeltaFile() {
        logger.info("Starting update cycle for latest delta file.")
        val start = System.nanoTime()
        val tempFile = File.createTempFile("gleif_update_delta", ".csv")
        try {
            apiAccessor.getLastMonthGoldenCopyDelta(tempFile)
            uploadCompanies(tempFile)
        } finally {
            tempFile.delete()
        }
        logger.info("Finished update cycle for latest delta file in ${getExecutionTime(start)}.")
    }

    private fun uploadCompanies(csvFile: File) {
        val gleifDataStream = gleifParser.getCsvStreamFromZip(csvFile)
        val gleifIterator = gleifParser.readGleifDataFromBufferedReader(gleifDataStream)
        Upload(keycloakTokenManager).uploadCompanies(gleifIterator.readAll().map { it.toCompanyInformation() })
    }

    private fun getExecutionTime(startTime: Long): String {
        return (System.nanoTime() - startTime)
            .toDuration(DurationUnit.NANOSECONDS)
            .toComponents { hours, minutes, seconds, _ ->  String.format("%02dh %02dm %02ds", hours, minutes, seconds ) }
    }
}

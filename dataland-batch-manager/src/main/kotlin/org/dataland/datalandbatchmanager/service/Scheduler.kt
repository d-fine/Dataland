package org.dataland.datalandbatchmanager.service

import org.dataland.datalandbackend.openApiClient.api.ActuatorApi
import org.dataland.datalandbatchmanager.gleif.CompanyUpload
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import java.net.ConnectException
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

const val WAIT_TIME: Long = 5000

/**
 * Class to execute scheduled tasks, like the import of the GLEIF golden copy files
 * @param gleifApiAccessor downloads the golden copy files from GLEIF
 * @param gleifParser reads in the csv file from GLEIF and creates GleifCompanyInformation objects
 * @param keycloakTokenManager manages the access tokens for authenticating against the backend API
 */
@Component
class Scheduler(
    @Autowired private val gleifApiAccessor: GleifApiAccessor,
    @Autowired private val gleifParser: GleifCsvParser,
    @Autowired private val keycloakTokenManager: KeycloakTokenManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        waitForBackend()
        if (System.getenv("GET_ALL_GLEIF_COMPANIES") == "true") {
            logger.info("Retrieving all company data available via GLEIF.")
            val tempFile = File.createTempFile("gleif_golden_copy", ".csv")
            processFile(tempFile, gleifApiAccessor::getFullGoldenCopy)
        }
    }

    private fun waitForBackend() {
        val actuatorApi = ActuatorApi(System.getenv("INTERNAL_BACKEND_URL"))
        while (true) {
            try {
                actuatorApi.health()
                break
            } catch (exception: ConnectException) {
                logger.info("Waiting for 5s backend to be available. Exception was: ${exception.message}.")
                Thread.sleep(WAIT_TIME)
            }
        }
    }

    @Suppress("UnusedPrivateMember") // Detect does not recognise the scheduled execution of this function
    @Scheduled(cron = "0 0 3 * * SUN")
    private fun processDeltaFile() {
        logger.info("Starting update cycle for latest delta file.")
        val tempFile = File.createTempFile("gleif_update_delta", ".csv")
        processFile(tempFile, gleifApiAccessor::getLastMonthGoldenCopyDelta)
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

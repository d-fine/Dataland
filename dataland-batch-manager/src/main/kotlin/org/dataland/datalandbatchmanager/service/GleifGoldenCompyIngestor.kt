package org.dataland.datalandbatchmanager.service

import org.dataland.datalandbackend.openApiClient.api.ActuatorApi
import org.dataland.datalandbatchmanager.model.GleifCompanyInformation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import java.net.ConnectException
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.stream.StreamSupport
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Class to execute scheduled tasks, like the import of the GLEIF golden copy files
 * @param gleifApiAccessor downloads the golden copy files from GLEIF
 * @param gleifParser reads in the csv file from GLEIF and creates GleifCompanyInformation objects
 * @param keycloakTokenManager manages the access tokens for authenticating against the backend API
 */
@Component
class GleifGoldenCompyIngestor(
    @Autowired private val gleifApiAccessor: GleifApiAccessor,
    @Autowired private val gleifParser: GleifCsvParser,
    @Autowired private val companyUploader: CompanyUpload,
    @Autowired private val actuatorApi: ActuatorApi,
    @Value("\${dataland.dataland-batch-managet.get-all-gleif-companies.force:false}")
    private val allCompaniesForceIngest: Boolean,

    @Value("\${dataland.dataland-batch-managet.get-all-gleif-companies.flag-file:#{null}}")
    private val allCompaniesIngestFlagFilePath: String?,
) {
    companion object {
        const val WAIT_TIME: Long = 5000
        const val UPLOAD_THREAT_POOL_SIZE = 32
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Downloads the entire GLEIF golden copy file and uploads all included companies to the Dataland Backend.
     * Does so only if the property "dataland.dataland-batch-managet.get-all-gleif-companies" is set.
     */
    @EventListener(ApplicationReadyEvent::class)
    fun processFullGoldenCopyFileIfEnabled() {
        val flagFile = allCompaniesIngestFlagFilePath?.let { File(it) }

        if (allCompaniesForceIngest || flagFile?.exists() == true) {
            if (flagFile?.exists() == true) {
                logger.info("Found collect all companies flag. Deleting it.")
                if (!flagFile.delete()) {
                    logger.error(
                        "Unable to delete flag file $flagFile. Manually remove it or import will " +
                            "be triggered after service restart again.",
                    )
                }
            }

            logger.info("Retrieving all company data available via GLEIF.")
            val tempFile = File.createTempFile("gleif_golden_copy", ".csv")
            processFile(tempFile, gleifApiAccessor::getFullGoldenCopy)
        } else {
            logger.info("Flag file not present & no force update variable set => Not performing any download")
        }
    }

    @Suppress("UnusedPrivateMember") // Detect does not recognise the scheduled execution of this function
    @Scheduled(cron = "0 0 3 * * SUN")
    private fun processDeltaFile() {
        logger.info("Starting update cycle for latest delta file.")
        val tempFile = File.createTempFile("gleif_update_delta", ".csv")
        processFile(tempFile, gleifApiAccessor::getLastMonthGoldenCopyDelta)
    }

    @Synchronized
    private fun processFile(csvFile: File, downloadFile: (file: File) -> Unit) {
        waitForBackend()
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

    private fun waitForBackend() {
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

    private fun uploadCompanies(csvFile: File) {
        val gleifDataStream = gleifParser.getCsvStreamFromZip(csvFile)
        val gleifIterator = gleifParser.readGleifDataFromBufferedReader(gleifDataStream)
        val gleifIterable = Iterable<GleifCompanyInformation> { gleifIterator }

        val uploadThreadPool = ForkJoinPool(UPLOAD_THREAT_POOL_SIZE)
        try {
            uploadThreadPool.submit {
                StreamSupport.stream(gleifIterable.spliterator(), true)
                    .forEach { companyUploader.uploadSingleCompany(it.toCompanyInformation()) }
            }.get()
        } finally {
            uploadThreadPool.shutdown()
        }
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

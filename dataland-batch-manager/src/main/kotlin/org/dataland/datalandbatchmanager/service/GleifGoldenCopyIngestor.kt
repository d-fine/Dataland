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
import java.time.Instant
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
class GleifGoldenCopyIngestor(
    @Autowired private val gleifApiAccessor: GleifApiAccessor,
    @Autowired private val gleifParser: GleifCsvParser,
    @Autowired private val companyUploader: CompanyUploader,
    @Autowired private val actuatorApi: ActuatorApi,
//    @Autowired private val isinDeltaBuilder: IsinDeltaBuilder,
    @Value("\${dataland.dataland-batch-managet.get-all-gleif-companies.force:false}")
    private val allCompaniesForceIngest: Boolean,

    @Value("\${dataland.dataland-batch-managet.get-all-gleif-companies.flag-file:#{null}}")
    private val allCompaniesIngestFlagFilePath: String?,
) {
    companion object {
        const val MS_PER_S = 1000L
        const val MAX_WAITING_TIME_IN_MS = 10L * 60L * MS_PER_S
        const val WAIT_TIME_IN_MS: Long = 5000
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
            processLeiFile(tempFile, gleifApiAccessor::getFullGoldenCopy)
        } else {
            logger.info("Flag file not present & no force update variable set => Not performing any download")
        }
    }

    @Suppress("UnusedPrivateMember") // Detect does not recognise the scheduled execution of this function
    @Scheduled(cron = "0 0 3 * * SUN")
    private fun processLeiAndIsinFiles() {
        prepareLeiDeltaFile()
        prepareIsinFile()
    }

    private fun prepareLeiDeltaFile() {
        logger.info("Starting LEI update cycle for latest delta file.")
        val tempFileLei = File.createTempFile("gleif_LEI_update_delta", ".csv")
        processLeiFile(tempFileLei, gleifApiAccessor::getLastMonthGoldenCopyDelta)
    }

    private fun prepareIsinFile() {
        logger.info("Starting ISIN update cycle for latest file.")
        val tempFileIsin = File.createTempFile("gleif_ISIN_update_delta", ".csv")
        processIsinFile(tempFileIsin, gleifApiAccessor::getIsinFileZip)
        // upload file via API, see also hint from Andreas
    }

    @Synchronized
    private fun processIsinFile(csvFile: File, downloadFile: (file: File) -> Unit) {
        waitForBackend()
        //  download ZIP file
        downloadFile(csvFile)
        // do: unpack ZIP file into CSV
//        val newFileIsinCsv = File.createTempFile("gleif_ISIN_update", ".csv")
        // do: do the unpacking
//        val oldFileIsinCsv
        // do: get the old file as oldFileIsinCsv
//      produce delta of that file
//     val isinDeltas: Map<String, String> = isinDeltaBuilder.createDeltaOfMappingFile(newFileIsinCsv, oldFileIsinCsv)
    }

    @Synchronized
    private fun processLeiFile(csvFile: File, downloadFile: (file: File) -> Unit) {
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
        val timeoutTime = Instant.now().toEpochMilli() + MAX_WAITING_TIME_IN_MS
        while (Instant.now().toEpochMilli() <= timeoutTime) {
            try {
                actuatorApi.health()
                break
            } catch (exception: ConnectException) {
                logger.info(
                    "Waiting for ${WAIT_TIME_IN_MS / MS_PER_S}s backend to be available." +
                        " Exception was: ${exception.message}.",
                )
                Thread.sleep(WAIT_TIME_IN_MS)
            }
        }
    }

    private fun uploadCompanies(csvFile: File) {
        val gleifDataStream = gleifParser.getCsvStreamFromZip(csvFile) // do: zip or csv coming in?
        val gleifIterator = gleifParser.readGleifDataFromBufferedReader(gleifDataStream)
        val gleifIterable = Iterable<GleifCompanyInformation> { gleifIterator }

        val uploadThreadPool = ForkJoinPool(UPLOAD_THREAT_POOL_SIZE)
        try {
            uploadThreadPool.submit {
                StreamSupport.stream(gleifIterable.spliterator(), true)
                    .forEach { companyUploader.uploadOrPatchSingleCompany(it) }
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

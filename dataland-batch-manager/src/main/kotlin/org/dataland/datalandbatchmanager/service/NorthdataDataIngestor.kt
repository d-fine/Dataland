package org.dataland.datalandbatchmanager.service

import org.dataland.datalandbatchmanager.model.NorthDataCompanyInformation
import org.dataland.datalandbatchmanager.service.GleifGoldenCopyIngestor.Companion.UPLOAD_THREAT_POOL_SIZE
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.stream.StreamSupport
import kotlin.time.Duration
import kotlin.time.measureTime

/**
 * Class to handle the scheduled update of the NorthData data
 * @param northDataAccessor downloads the NorthData bulk data
 * @param companyUploader uploads the company information to the backend
 */
@Suppress("LongParameterList")
@Component
class NorthdataDataIngestor(
    @Autowired private val northDataAccessor: NorthDataAccessor,
    @Autowired private val companyUploader: CompanyUploader,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun filterAndTriggerUpload(northDataCompanyInformation: NorthDataCompanyInformation) {
        when (northDataCompanyInformation.status) {
            "terminated" -> return
            // TODO discuss how to handle these cases
            "active", "liquidation", "" -> {
                companyUploader.uploadOrPatchSingleCompany(northDataCompanyInformation)
            }
            else -> {
                logger.info(
                    "Found unexpected status code for NorthDataCompanyInformation " +
                        "${northDataCompanyInformation.status} for company with name " +
                        northDataCompanyInformation.companyName,
                )
            }
        }
    }

    // TODO is almost a copy of code in GleifGoldenCopyIngestor, somehow avoid duplicate code?
    private fun updateNorthData(zipFile: File) {
        val csvParser = GleifCsvParser()
        val northStream = csvParser.getCsvStreamFromNorthDataZipFile(zipFile)
        val northDataIterable = csvParser.readNorthDataFromBufferedReader(northStream)

        val uploadThreadPool = ForkJoinPool(UPLOAD_THREAT_POOL_SIZE)
        try {
            uploadThreadPool.submit {
                StreamSupport.stream(northDataIterable.spliterator(), true)
                    .forEach {
                        filterAndTriggerUpload(it)
                    }
            }.get()
        } finally {
            uploadThreadPool.shutdown()
        }
    }

    /**
     * Method to download and execute the NorthData bulk data update
     * @param downloadFile function to execute to download the NorthData bulk data
     */
    @Synchronized
    fun processNorthdataFile(downloadFile: (file: File) -> Unit) {
        val zipFile = File.createTempFile("northdata_golden_copy", ".zip")
        val duration = measureTime {
            try {
                downloadFile(zipFile)
                updateNorthData(zipFile)
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

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
 * Class to execute scheduled tasks, like the import of the NorthData golden copy files
 * @param northDataAccessor downloads the golden copy files from NorthData
 * @param companyUploader uploads the updated company entries to dataland
 */
@Suppress("LongParameterList")
@Component
class NorthdataDataIngestor(
    @Autowired private val northDataAccessor: NorthDataAccessor,
    @Autowired private val companyUploader: CompanyUploader,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // TODO is almost a copy of code in GleifGoldenCopyIngestor, somehow avoid duplicate code?
    private fun updateNorthData(zipFile: File) {
        val csvParser = GleifCsvParser()
        val northStream = csvParser.getCsvStreamFromNorthDataZipFile(zipFile)
        val northDataIterable: Iterable<NorthDataCompanyInformation> = csvParser.readDataFromBufferedReader(northStream)

        val uploadThreadPool = ForkJoinPool(UPLOAD_THREAT_POOL_SIZE)
        try {
            uploadThreadPool.submit {
                StreamSupport.stream(northDataIterable.spliterator(), true)
                    .forEach {
                        companyUploader.uploadOrPatchFromNorthData(it)
                    }
            }.get()
        } finally {
            uploadThreadPool.shutdown()
        }
    }

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

package org.dataland.datalandbatchmanager.service

import org.dataland.datalandbatchmanager.model.NorthDataCompanyInformation
import org.dataland.datalandbatchmanager.service.GleifGoldenCopyIngestor.Companion.UPLOAD_THREAD_POOL_SIZE
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
 * @param companyUploader uploads the company information to the backend
 */
@Suppress("LongParameterList")
@Component
class NorthdataDataIngestor(
    @Autowired private val companyUploader: CompanyUploader,
    @Autowired private val csvParser: CsvParser,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun filterAndTriggerUpload(northDataCompanyInformation: NorthDataCompanyInformation) {
        var update = false
        when (northDataCompanyInformation.status) {
            "terminated" -> update = false
            "active", "liquidation", "" -> update = true
            else -> {
                logger.info(
                    "Found unexpected status code for NorthDataCompanyInformation " +
                        "${northDataCompanyInformation.status} for company with name " +
                        northDataCompanyInformation.companyName,
                )
            }
        }
        if ((northDataCompanyInformation.registerId == "") &&
            (northDataCompanyInformation.vatId == "") &&
            (northDataCompanyInformation.lei == "")
        ) {
            update = false
            logger.info(
                "Neither registerId nor vatId nor LEI provided for company with name " +
                    northDataCompanyInformation.companyName,
            )
        }
        if (update) companyUploader.uploadOrPatchSingleCompany(northDataCompanyInformation)
    }

    private fun updateCompaniesFromNorthDataFile(zipFile: File) {
        val northStream = csvParser.getCsvStreamFromNorthDataZipFile(zipFile)
        val northDataIterable = csvParser.readNorthDataFromBufferedReader(northStream)

        val uploadThreadPool = ForkJoinPool(UPLOAD_THREAD_POOL_SIZE)
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
        val zipFile = File("/NorthdataTestData.zip")
        val duration = measureTime {
            try {
                downloadFile(zipFile)
                updateCompaniesFromNorthDataFile(zipFile)
            } finally {
                logger.error("Not deleting $zipFile now, remember to change this.")
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

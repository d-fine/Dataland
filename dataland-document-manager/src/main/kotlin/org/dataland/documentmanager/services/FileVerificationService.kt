package org.dataland.documentmanager.services

import org.apache.pdfbox.Loader
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

/**
 * A service for performing basic sanity checks on files uploaded by user
 */
@Component
class FileVerificationService {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * A function that performs surface-level checks to ensure that an uploaded file is indeed a PDF.
     * This function does not enforce anything else.
     * In particular a file passing this function is in no way guaranteed to be safe to open.
     */
    fun assertThatFileLooksLikeAPdf(file: MultipartFile, correlationId: String) {
        try {
            checkIfPotentialPdfFileIsEmpty(file.bytes, correlationId)
        } catch (ex: IOException) {
            logger.info("Document uploaded with correlation ID: $correlationId cannot be parsed as a PDF, aborting.")
            throw InvalidInputApiException(
                "Could not parse file as PDF document",
                "The file you uploaded was not able to be parsed as PDF file." +
                    " Please ensure that the file you uploaded has not been corrupted",
                ex,
            )
        }
    }

    private fun checkIfPotentialPdfFileIsEmpty(blob: ByteArray, correlationId: String) {
        Loader.loadPDF(blob).use {
            if (it.numberOfPages <= 0) {
                logger.info(
                    "PDF document uploaded with correlation ID: $correlationId seems to have 0 pages, aborting.",
                )
                throw InvalidInputApiException(
                    "You seem to have uploaded an empty PDF",
                    "We have detected that the PDF you uploaded has 0 pages.",
                )
            }
        }
    }
}

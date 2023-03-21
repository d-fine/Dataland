package org.dataland.documentmanager.services

import org.apache.pdfbox.pdmodel.PDDocument
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException

/**
 * A service for performing basic PDF sanity checks
 */
@Component
class PdfVerificationService {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * A function that performs surface-level checks to ensure that an uploaded document is indeed a PDF.
     * This function does not enforce anything else.
     * In particular a file passing this function is in no way guaranteed to be safe to open.
     */
    fun assertThatBlobLooksLikeAPdf(blob: ByteArray, correlationId: String) {
        try {
            PDDocument.load(blob).use {
                if (it.numberOfPages <= 0) {
                    logger.info("PDF document uploaded with correlation id: $correlationId seems to have 0 pages, " +
                            "aborting.")
                    throw InvalidInputApiException(
                        "You seem to have uploaded an empty PDF",
                        "We have detected that the pdf you uploaded has 0 pages.",
                    )
                }
            }
        } catch (ex: IOException) {
            logger.info("Document uploaded with correlation id: $correlationId cannot be parsed as a PDF, aborting.")
            throw InvalidInputApiException(
                "Could not parse PDF document",
                "We were unable to load the PDF document you provided." +
                    " Please ensure that the file you uploaded has not been corrupted",
                ex,
            )
        }
    }
}

package org.dataland.documentmanager.services

import org.apache.pdfbox.pdmodel.PDDocument
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
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
    fun assertThatDocumentLooksLikeAPdf(document: MultipartFile, correlationId: String) {
        try {
            checkIfPdfDocumentIsEmpty(document.bytes, correlationId)
            checkThatDocumentNameEndsOnPdf(document.originalFilename!!, correlationId)
            checkThatDocumentNameIsValid(document.originalFilename!!, correlationId)
        } catch (ex: IOException) {
            logger.info("Document uploaded with correlation ID: $correlationId cannot be parsed as a PDF, aborting.")
            throw InvalidInputApiException(
                "Could not parse PDF document",
                "We were unable to load the PDF document you provided." +
                    " Please ensure that the file you uploaded has not been corrupted",
                ex,
            )
        }
    }

    private fun checkIfPdfDocumentIsEmpty(blob: ByteArray, correlationId: String) {
        PDDocument.load(blob).use {
            if (it.numberOfPages <= 0) {
                logger.info(
                    "PDF document uploaded with correlation ID: $correlationId seems to have 0 pages, aborting.",
                )
                throw InvalidInputApiException(
                    "You seem to have uploaded an empty PDF",
                    "We have detected that the pdf you uploaded has 0 pages.",
                )
            }
        }
    }

    private fun checkThatDocumentNameEndsOnPdf(name: String, correlationId: String) {
        if (name.takeLast(expectedFileNameIdentifierLength) != ".pdf") {
            logger.info(
                "PDF document uploaded with correlation ID: $correlationId " +
                    "does not have a name ending on '.pdf', aborting.",
            )
            throw InvalidInputApiException(
                "You seem to have uploaded an file that is not a pdf file",
                "We have detected that the file does not have a name ending on '.pdf'",
            )
        }
    }

    /**
     * We allow alphanumeric characters, hyphens, spaces, and periods up to a maximum length of 254 characters
     * in our filenames
     */
    private val allowedFilenameRegex = Regex("^[\\w\\-. ]{1,254}\$")

    private fun checkThatDocumentNameIsValid(name: String, correlationId: String) {
        if (!allowedFilenameRegex.matches(name)) {
            logger.info(
                "PDF document uploaded with correlation ID: $correlationId has invalid name, aborting.",
            )
            throw InvalidInputApiException(
                "You seem to have uploaded an file that has an invalid name",
                "Please ensure that your filename only contains alphanumeric characters, hyphens, spaces," +
                    " and periods up to maximum length of 254 characters.",
            )
        }
    }

    companion object {
        const val expectedFileNameIdentifierLength: Int = 4
    }
}

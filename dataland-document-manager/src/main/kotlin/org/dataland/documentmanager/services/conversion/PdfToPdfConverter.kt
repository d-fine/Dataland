package org.dataland.documentmanager.services.conversion

import org.apache.pdfbox.Loader
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import xyz.capybara.clamav.ClamavClient
import java.io.IOException

/**
 * A validator for pdfs
 */
@Component
class PdfToPdfConverter(
    @Autowired
    clamAvClient: ClamavClient,
) : FileConverter(
    allowedMimeTypesPerFileExtension = mapOf(
        "pdf" to setOf("application/pdf"),
    ),
    clamAvClient = clamAvClient,
) {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)

    val pdfParsingErrorMessage = "The file you uploaded was not able to be parsed as PDF file."
    val pdfHasZeroPagesErrorMessage = "The PDF you uploaded seems to have 0 pages."

    override fun validateFileContent(file: MultipartFile, correlationId: String) {
        logger.info("Validating that the pdf is not empty. (correlation ID: $correlationId)")
        try {
            checkIfPotentialPdfFileIsEmpty(file.bytes, correlationId)
        } catch (ex: IOException) {
            logger.info("Document uploaded with correlation ID: $correlationId cannot be parsed as a PDF, aborting.")
            throw InvalidInputApiException(
                "Could not parse file as PDF document",
                pdfParsingErrorMessage,
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
                    pdfHasZeroPagesErrorMessage,
                )
            }
        }
    }

    override fun convert(file: MultipartFile, correlationId: String): ByteArray = file.bytes
}

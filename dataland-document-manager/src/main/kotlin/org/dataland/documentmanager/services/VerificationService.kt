package org.dataland.documentmanager.services

import org.apache.pdfbox.Loader
import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException

/**
 * A service for performing basic sanity checks on files uploaded by users
 */
@Component
class VerificationService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val extensionToMimetype = mapOf(
        "ppt" to setOf("application/vnd.ms-powerpoint"),
        "pptx" to setOf("application/vnd.openxmlformats-officedocument.presentationml.presentation"),
        "doc" to setOf("application/msword"),
        "docx" to setOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        "txt" to setOf("text/plain"),
        "png" to setOf("image/png"),
        "jpg" to setOf("image/jpeg"),
        "jpeg" to setOf("image/jpeg"),
        "jpe" to setOf("image/jpeg"),
        "jxr" to setOf("image/vnd.ms-photo"),
        "tif" to setOf("image/tiff"),
        "tiff" to setOf("image/tiff"),
        "heif" to setOf("image/heif"),
        "heic" to setOf("image/heic"),
    )

    val pdfParsingErrorMessage = "The file you uploaded was not able to be parsed as PDF file."
    val pdfHasZeroPagesErrorMessage = "The PDF you uploaded seems to have 0 pages."
    val fileNameHasForbiddenCharactersMessage =
        "Please ensure that your selected file name follows the naming convention for Windows: Avoid using " +
            "special characters like < > : \" / \\ | ? * and ensure the name does not end or begin with a space, " +
            "or end with a full stop character."
    val typeNotSupportedMessage = "Only supported file types can be uploaded."
    val fileExtensionAndMimeTypeMismatchMessage =
        "Only upload of documents with matching file extensions and MIME types is supported."

    /**
     * A function that performs surface-level checks to ensure that an uploaded file is indeed a PDF with at least one
     * page and a file name that follows a specific naming convention.
     * This function does not enforce anything else.
     * In particular a file passing this function is in no way guaranteed to be safe to open.
     */
    fun assertThatFileLooksLikeAValidPdfWithAValidName(file: MultipartFile, correlationId: String) {
        try {
            checkIfPotentialPdfFileIsEmpty(file.bytes, correlationId)
            checkThatFileNameIsWithinNamingConvention(file.originalFilename!!, correlationId)
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

    /** todo remove
     * to be removed, got shifted
     */
    fun validateFileType(file: MultipartFile, correlationId: String) {
        val fileExtension = validateFileExtensionSupport(file)
        validateMimeType(file, fileExtension)
    }

    private fun validateMimeType(file: MultipartFile, fileExtension: String) {
        val detectedMimeType = Tika().detect(file.bytes)
        val expectedMimeTypes = extensionToMimetype.getValue(fileExtension)
        if (detectedMimeType !in expectedMimeTypes) {
            throw InvalidInputApiException(
                "The provided file extension $fileExtension does not match the detected MIME type $detectedMimeType.",
                fileExtensionAndMimeTypeMismatchMessage,
            )
        }
    }

    private fun validateFileExtensionSupport(file: MultipartFile): String {
        val fileExtension = file.originalFilename!!.let { File(it).extension }
        if (!extensionToMimetype.containsKey(fileExtension)) {
            throw InvalidInputApiException(
                "The provided file extension $fileExtension of ${file.originalFilename} is not recognized.",
                typeNotSupportedMessage,
            )
        }
        return fileExtension
    }

    /**
     * We allow file names that follow the naming convention of Windows systems.
     */
    private val allowedFilenameRegex =
        Regex("^[^<>:\"|?/*\\\\\\s][^<>:\"|?/*\\\\]{0,252}[^<>:\"|?/*\\\\.\\s]\$")

    private fun checkThatFileNameIsWithinNamingConvention(name: String, correlationId: String) {
        if (!allowedFilenameRegex.matches(name)) {
            logger.info(
                "Document uploaded with correlation ID: $correlationId violates the naming convention" +
                    "of Dataland, aborting.",
            )
            throw InvalidInputApiException(
                "You seem to have uploaded a file that has an invalid name",
                fileNameHasForbiddenCharactersMessage,
            )
        }
    }
}

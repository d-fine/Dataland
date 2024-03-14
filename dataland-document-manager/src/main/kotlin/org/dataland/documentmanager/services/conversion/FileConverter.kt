package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.slf4j.Logger
import org.springframework.web.multipart.MultipartFile
import java.io.File

abstract class FileConverter {
    protected abstract val logger: Logger
    protected abstract val allowedMimeTypesPerFileExtension: Map<String, Set<String>>
    val fileExtensionAndMimeTypeMismatchMessage = "Only upload of documents with matching file extensions and MIME types is supported."
    val fileNameHasForbiddenCharactersMessage =
        "Please ensure that your selected file name follows the naming convention for Windows: Avoid using " +
            "special characters like < > : \" / \\ | ? * and ensure the name does not end or begin with a space, " +
            "or end with a full stop character."

    val responsibleFileExtensions: Set<String>
        get() = allowedMimeTypesPerFileExtension.keys

    fun validateFile(file: MultipartFile) {
        validateFileNameWithinNamingConvention(file.originalFilename!!, "placeholder") // todo
        validateMimeType(file)
        validateFileContent(file)
    }

    protected open fun validateFileContent(file: MultipartFile) {

    }

    abstract fun convertToPdf(file: MultipartFile): ByteArray


    private fun validateMimeType(file: MultipartFile) {
        val fileExtension = file.originalFilename!!.let { File(it).extension }
        require(fileExtension in allowedMimeTypesPerFileExtension) // TODO probably duplicate later
        val detectedMimeType = Tika().detect(file.bytes)
        val expectedMimeTypes = allowedMimeTypesPerFileExtension.getValue(fileExtension)
        if (detectedMimeType !in expectedMimeTypes) {
            throw InvalidInputApiException(
                "The provided file extension $fileExtension does not match the detected MIME type $detectedMimeType.",
                fileExtensionAndMimeTypeMismatchMessage,
            )
        }
    }

    /**
     * We allow file names that follow the naming convention of Windows systems.
     */
    private val allowedFilenameRegex =
        Regex("^[^<>:\"|?/*\\\\\\s][^<>:\"|?/*\\\\]{0,252}[^<>:\"|?/*\\\\.\\s]\$")

    private fun validateFileNameWithinNamingConvention(name: String, correlationId: String) {
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
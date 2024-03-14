package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.springframework.core.io.InputStreamResource
import org.springframework.web.multipart.MultipartFile
import java.io.File

abstract class FileConverter {
    protected abstract val allowedMimeTypesPerFileExtension: Map<String, Set<String>>
    val fileExtensionAndMimeTypeMismatchMessage = "Only upload of documents with matching file extensions and MIME types is supported."

    val responsibleFileExtensions: Set<String>
        get() = allowedMimeTypesPerFileExtension.keys

    fun validateFile(file: MultipartFile) {
        validateMimeType(file)
        validateFileContent(file)
    }

    protected open fun validateFileContent(file: MultipartFile) {

    }

    abstract fun convertToPdf(file: MultipartFile): InputStreamResource


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
}
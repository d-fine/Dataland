package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Meta information associated to an upload process of am Excel file
 * @param uploadSuccessful tells the uploader/user if the upload process was successful
 * @param uploadMessage gives the uploader/user some information about the result of the upload process
 */
data class ExcelFilesUploadResponse(
    @field:JsonProperty(required = true)
    val uploadSuccessful: Boolean,

    @field:JsonProperty(required = true)
    val uploadMessage: String

    // TODO extendable by things like filesize or upload-timestamp or keycloakUserIdOfUploader etc.
)

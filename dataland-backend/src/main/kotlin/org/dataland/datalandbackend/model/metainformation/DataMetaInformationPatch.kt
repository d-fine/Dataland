package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.utils.CompanyControllerDescriptionsAndExamples

/**
 * --- API model ---
 * Model to update data meta information associated to data in the data store selectively
 * @param uploaderUserId the user ID of the user who uploaded the dataset
 */
data class DataMetaInformationPatch(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.UPLOADER_USER_ID_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.UPLOADER_USER_ID_EXAMPLE,
    )
    val uploaderUserId: String,
)

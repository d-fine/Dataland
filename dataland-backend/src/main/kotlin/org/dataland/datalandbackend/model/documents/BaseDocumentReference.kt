package org.dataland.datalandbackend.model.documents

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import org.dataland.datalandbackend.interfaces.documents.BaseDocumentReference
import org.dataland.datalandbackend.validator.DocumentExists
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of a generic document reference
 */
data class BaseDocumentReference(
    override val fileName: String? = null,
    @field:JsonProperty(required = true)
    @field:NotBlank
    @field:DocumentExists
    override val fileReference: String,
    override val publicationDate: LocalDate? = null,
) : BaseDocumentReference

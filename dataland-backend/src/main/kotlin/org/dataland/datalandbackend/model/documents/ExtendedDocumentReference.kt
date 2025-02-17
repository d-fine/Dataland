package org.dataland.datalandbackend.model.documents

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import org.dataland.datalandbackend.interfaces.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.validator.DocumentExists
import org.dataland.datalandbackend.validator.PageRange
import java.time.LocalDate

/**
 * --- API model ---
 * A reference to a page in a company report
 */
data class ExtendedDocumentReference(
    @field:PageRange
    override val page: String? = null,
    override val tagName: String? = null,
    override val fileName: String? = null,
    @field:JsonProperty(required = true)
    @field:NotBlank
    @field:DocumentExists
    override val fileReference: String,
    override var publicationDate: LocalDate? = null,
) : ExtendedDocumentReference

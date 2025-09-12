package org.dataland.datalandbackend.model.documents

import com.fasterxml.jackson.annotation.JsonCreator
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
data class ExtendedDocumentReference
    @JsonCreator
    constructor(
        @field:PageRange
        @JsonProperty("page")
        override val page: String? = null,
        @JsonProperty("tagName")
        override val tagName: String? = null,
        @JsonProperty("fileName")
        override val fileName: String? = null,
        @JsonProperty("fileReference", required = true)
        @field:NotBlank
        @field:DocumentExists
        override val fileReference: String,
        @JsonProperty("publicationDate")
        override val publicationDate: LocalDate? = null,
    ) : ExtendedDocumentReference {
        /**
         * Converts this reference to a company report
         */
        fun toCompanyReport(): CompanyReport =
            CompanyReport(
                fileName = fileName,
                fileReference = fileReference,
                publicationDate = publicationDate,
            )
    }

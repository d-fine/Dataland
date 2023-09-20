package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * A reference to a page in a company report
 */
interface DocumentDetailedReferenceInterface {
    val quality: QualityOptions

    val comment: String?

}
/**
 * --- API model ---
 * A reference to a page in a company report
 */
data class DocumentDetailedReference(
    override val fileName: String,
    @field:JsonProperty(required = true)
    override val page: Long? = null,

    override val tagName: String? = null,
    @field:JsonProperty(required = true)
    override val fileReference: String,
    override val quality: QualityOptions,

    override val comment: String? = null,

    ):DocumentReferenceInterface, CompanyReportReferenceInterface, DocumentDetailedReferenceInterface

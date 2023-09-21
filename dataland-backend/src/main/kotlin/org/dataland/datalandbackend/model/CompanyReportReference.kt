package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * A reference to a page in a company report
 */
interface CompanyReportReferenceInterface: BaseDocumentReferenceInterface {
    val page: Long?
    val tagName: String?

}
/**
 * --- API model ---
 * A reference to a page in a company report
 */
data class CompanyReportReference(
    override val fileName: String,
    @field:JsonProperty(required = true)
    override val fileReference: String,
    override val page: Long? = null,

    override val tagName: String? = null,

):CompanyReportReferenceInterface

ExtendedDocumentReference
BaseDocumentReference
Page
TagName

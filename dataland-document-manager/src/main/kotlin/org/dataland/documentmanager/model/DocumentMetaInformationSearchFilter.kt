package org.dataland.documentmanager.model

import org.dataland.datalandbackendutils.model.DocumentCategory

/**
 * A data class for storing the filters of a document metainformation search.
 */
data class DocumentMetaInformationSearchFilter(
    val companyId: String? = null,
    val documentCategories: Set<DocumentCategory>? = null,
    val reportingPeriod: String? = null,
) {
    /**
     * Checks whether the search filter is syntactically trivial (i.e., everything matches).
     */
    fun isInvalid() =
        companyId == null &&
            (documentCategories == null || documentCategories == DocumentCategory.entries.toSet()) &&
            reportingPeriod == null
}

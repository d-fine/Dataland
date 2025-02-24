package org.dataland.documentmanager.services

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
     * Checks whether all three fields are null.
     */
    fun isEmpty() =
        companyId == null &&
            (documentCategories == null || documentCategories == DocumentCategory.entries.toSet()) &&
            reportingPeriod == null
}

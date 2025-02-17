package org.dataland.datalandbackend.model.datapoints

import org.dataland.datalandbackend.interfaces.documents.BaseDocumentReference

/**
 * Marks a datapoint as having one or multiple document references.
 * The document references are used to compute the referencedReports fields.
 * The implementor MUST provide a direct reference to the document reference as they may be modified by the calling
 * code.
 */
interface DataPointWithDocumentReference {
    /**
     * Returns all document references of the data point.
     */
    fun getAllDocumentReferences(): List<BaseDocumentReference>
}

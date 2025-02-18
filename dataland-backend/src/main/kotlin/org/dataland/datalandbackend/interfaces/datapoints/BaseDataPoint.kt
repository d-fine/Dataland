package org.dataland.datalandbackend.interfaces.datapoints

import org.dataland.datalandbackend.interfaces.documents.BaseDocumentReference
import org.dataland.datalandbackend.model.datapoints.DataPointWithDocumentReference

/**
 * --- API model ---
 * Interface of the generic base data point
 */
interface BaseDataPoint<T> : DataPointWithDocumentReference {
    val value: T?
    val dataSource: BaseDocumentReference?

    override fun getAllDocumentReferences(): List<BaseDocumentReference> {
        val source = dataSource
        return if (source != null) {
            listOf(source)
        } else {
            emptyList()
        }
    }
}

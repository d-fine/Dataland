package org.dataland.datalandbackend.interfaces.datapoints

import org.dataland.datalandbackend.interfaces.documents.BaseDocumentReference

/**
 * --- API model ---
 * Interface of the generic base data point
 */
interface BaseDataPoint<T> {
    val value: T?
    val dataSource: BaseDocumentReference?
}

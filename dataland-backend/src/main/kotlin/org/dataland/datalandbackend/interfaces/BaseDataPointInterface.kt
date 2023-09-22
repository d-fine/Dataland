package org.dataland.datalandbackend.interfaces

/**
 * --- API model ---
 * Interface of the generic base data point
 */
interface BaseDataPointInterface<T> {
    val value: T?
    val dataSource: BaseDocumentReferenceInterface?
}

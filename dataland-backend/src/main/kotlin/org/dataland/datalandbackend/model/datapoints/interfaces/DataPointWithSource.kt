package org.dataland.datalandbackend.model.datapoints.interfaces

import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.validator.QualityAndValue

@QualityAndValue
interface DataPointWithSource<T> {
    val value: T?
    val quality: QualityOptions?
    val comment: String?
    val dataSource: ExtendedDocumentReference?
    val applicable: Boolean?
}

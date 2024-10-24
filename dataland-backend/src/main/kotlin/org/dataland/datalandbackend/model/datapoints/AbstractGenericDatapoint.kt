package org.dataland.datalandbackend.model.datapoints

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions

open class AbstractGenericDatapoint<T> {
    open val value: T? = null

    open val quality: QualityOptions? = null

    @field:NotBlank
    open val comment: String? = null

    @field:Valid
    open val dataSource: ExtendedDocumentReference? = null

    open val applicable: Boolean? = true
}

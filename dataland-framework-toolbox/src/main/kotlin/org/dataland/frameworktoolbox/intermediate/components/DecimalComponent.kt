package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import java.math.BigDecimal

/**
 * A DecimalComponent represents a numeric decimal value
 */
open class DecimalComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {
    open var minimumValue: BigDecimal? = null
    open var maximumValue: BigDecimal? = null
    open var constantUnitSuffix: String? = null

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addProperty(
            this.identifier,
            TypeReference("java.math.BigDecimal", true),
        )
    }
}

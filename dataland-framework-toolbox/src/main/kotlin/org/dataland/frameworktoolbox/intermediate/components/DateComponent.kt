package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder

/**
 * A DataComponent represents a date (with Year, Month, and Day)
 */
class DateComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addProperty(
            this.identifier,
            TypeReference("java.time.LocalDate", isNullable),
        )
    }
}

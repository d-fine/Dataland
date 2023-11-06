package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder

/**
 * A YesNoComponent is either Yes or No or N/A.
 */
class YesNoNaComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addProperty(
            this.identifier,
            TypeReference("org.dataland.datalandbackend.model.enums.commons.YesNoNa", isNullable),
        )
    }
}

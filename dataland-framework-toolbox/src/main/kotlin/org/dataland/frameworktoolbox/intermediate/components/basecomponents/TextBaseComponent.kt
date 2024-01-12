package org.dataland.frameworktoolbox.intermediate.components.basecomponents

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder

/**
 * The NumberBaseComponent serves as base-class for any text component.
 */
open class TextBaseComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addProperty(
            identifier,
            documentSupport.getJvmTypeReference(
                TypeReference("String", isNullable),
                isNullable,
            ),
        )
    }
}

package org.dataland.frameworktoolbox.frameworks.lksg.custom

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.MultiSelectComponent
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder

/**
 * Represents the LkSG-spcific "Risk Position MultiSelect" component
 */
open class LksgRiskPositionMultiSelectComponent(
    identifier: String,
    parent: FieldNodeParent,
) : MultiSelectComponent(identifier, parent) {

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addProperty(
            identifier,
            TypeReference(
                "java.util.EnumSet", isNullable,
                listOf(
                    TypeReference(
                        "org.dataland.datalandbackend.frameworks.lksg.custom.RiskPosition",
                        false,
                    ),
                ),
            ),
        )
    }
}

package org.dataland.frameworktoolbox.frameworks.vsme.custom

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.requireDocumentSupportIn
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder

/**
 * Describes a custom component for classification of waste including several fields with additional information
 */
class VsmeWasteClassificationObjectCustomComponent(
    identifier: String,
    parent: FieldNodeParent,
) : VsmeSimpleCustomComponentBase(
    identifier = identifier,
    parent = parent,
    viewFormattingFunctionName = "formatVsmeWasteClassificationObjectForDisplay",
    uploadComponentName = "WasteClassificationFormField",
    guaranteedFixtureExpression = "dataGenerator.guaranteedArray(() => " +
        "dataGenerator.generateRandomVsmeWasteClassificationObject(), 0, 3)",
    randomFixtureExpression = "dataGenerator.randomArray(() => dataGenerator." +
        "generateRandomVsmeWasteClassificationObject(), 0, 3)",
) {

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        dataClassBuilder.addProperty(
            identifier,
            TypeReference(
                "List", isNullable,
                listOf(
                    TypeReference(
                        "org.dataland.datalandbackend.frameworks.vsme.custom." +
                            "VsmeWasteClassificationObject",
                        true,
                    ),
                ),
            ),
        )
    }
}

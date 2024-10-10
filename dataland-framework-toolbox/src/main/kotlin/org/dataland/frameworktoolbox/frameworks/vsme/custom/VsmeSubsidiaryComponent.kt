package org.dataland.frameworktoolbox.frameworks.vsme.custom

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.requireDocumentSupportIn
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder

/**
 * Represents the Subsidiary component for vsme
 */
class VsmeSubsidiaryComponent(
    identifier: String,
    parent: FieldNodeParent,
) : VsmeSimpleCustomComponentBase(
        identifier = identifier,
        parent = parent,
        viewFormattingFunctionName = "formatVsmeSubsidiaryForDisplay",
        uploadComponentName = "SubsidiaryFormField",
        guaranteedFixtureExpression = "dataGenerator.randomArray(() => dataGenerator.generateVsmeSubsidiary(), 0, 5)",
        randomFixtureExpression = null,
    ) {
    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        dataClassBuilder.addProperty(
            identifier,
            TypeReference(
                "List", isNullable,
                listOf(
                    TypeReference(
                        "org.dataland.datalandbackend.frameworks.vsme.custom.VsmeSubsidiary",
                        true,
                    ),
                ),
            ),
        )
    }
}

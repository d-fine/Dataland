package org.dataland.frameworktoolbox.frameworks.vsme.custom

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.requireDocumentSupportIn
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder

/**
 * Represents the subsidiaries component for vsme
 */
class VsmeEmployeesPerCountryComponent(
    identifier: String,
    parent: FieldNodeParent,
) : VsmeSimpleCustomComponentBase(
    identifier = identifier,
    parent = parent,
    viewFormattingFunctionName = "formatVsmeEmployeesPerCountryForDisplay",
    uploadComponentName = "EmployeesPerCountryFormField",
    guaranteedFixtureExpression = "dataGenerator.randomArray(() => dataGenerator.generateVsmeEmployeesPerCountry()," +
        " 0, 5)",
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
                        "org.dataland.datalandbackend.frameworks.vsme.custom.SmeEmployeesPerCountry",
                        true,
                    ),

                ),
            ),

        )
    }
}

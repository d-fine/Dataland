package org.dataland.frameworktoolbox.frameworks.lksg.custom

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.requireDocumentSupportIn
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder

/**
 * Represents the SubcontractingCompanies component
 */
class LksgSubcontractingCompaniesComponent(
    identifier: String,
    parent: FieldNodeParent,
) : LksgSimpleCustomComponentBase(
        identifier = identifier,
        parent = parent,
        viewFormattingFunctionName = "formatLksgSubcontractingCompaniesForDisplay",
        uploadComponentName = "LksgSubcontractingCompaniesFormField",
        guaranteedFixtureExpression = "dataGenerator.generateSubcontractingCompanies()",
        randomFixtureExpression = "dataGenerator.valueOrNull(dataGenerator.generateSubcontractingCompanies())",
    ) {
    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        dataClassBuilder.addProperty(
            identifier,
            TypeReference(
                "Map", isNullable,
                listOf(
                    TypeReference(
                        "String",
                        false,
                    ),
                    TypeReference(
                        "List",
                        false,
                        listOf(TypeReference("String", false)),
                    ),
                ),
            ),
            listOf(
                Annotation(
                    fullyQualifiedName = "io.swagger.v3.oas.annotations.media.Schema",
                    rawParameterSpec =
                        "example = JsonExampleFormattingConstants.SUBCONTRACTING_COMPANIES_DEFAULT_VALUE",
                    applicationTargetPrefix = "field",
                    additionalImports = setOf("org.dataland.datalandbackend.utils.JsonExampleFormattingConstants"),
                ),
            ),
        )
    }
}

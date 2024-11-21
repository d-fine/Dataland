package org.dataland.frameworktoolbox.frameworks.lksg.custom

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.requireDocumentSupportIn
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder

/**
 * Represents the ProcurementCategories component
 */
class LksgProcurementCategoriesComponent(
    identifier: String,
    parent: FieldNodeParent,
) : LksgSimpleCustomComponentBase(
        identifier = identifier,
        parent = parent,
        viewFormattingFunctionName = "formatLksgProcurementCategoriesForDisplay",
        uploadComponentName = "ProcurementCategoriesFormField",
        guaranteedFixtureExpression = "dataGenerator.generateProcurementCategories()",
        randomFixtureExpression = "dataGenerator.valueOrNull(dataGenerator.generateProcurementCategories())",
    ) {
    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        dataClassBuilder.addProperty(
            identifier,
            TypeReference(
                "Map", isNullable,
                listOf(
                    TypeReference(
                        "org.dataland.datalandbackend.frameworks.lksg.custom.ProcurementCategoryType",
                        false,
                    ),
                    TypeReference(
                        "org.dataland.datalandbackend.frameworks.lksg.custom.LksgProcurementCategory",
                        false,
                    ),
                ),
            ),
            listOf(
                Annotation(
                    fullyQualifiedName = "io.swagger.v3.oas.annotations.media.Schema",
                    rawParameterSpec =
                        "example = JsonExampleFormattingConstants.PROCUREMENT_CATEGORIES_DEFAULT_VALUE",
                    applicationTargetPrefix = "field",
                    additionalImports = setOf("org.dataland.datalandbackend.utils.JsonExampleFormattingConstants"),
                ),
            ),
        )
    }
}

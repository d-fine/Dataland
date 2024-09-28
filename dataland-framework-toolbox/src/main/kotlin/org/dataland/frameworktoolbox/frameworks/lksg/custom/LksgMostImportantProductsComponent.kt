package org.dataland.frameworktoolbox.frameworks.lksg.custom

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.requireDocumentSupportIn
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder

/**
 * Represents the most Important Products component for the Lksg Framework
 */
class LksgMostImportantProductsComponent(
    identifier: String,
    parent: FieldNodeParent,
) : LksgSimpleCustomComponentBase(
        identifier = identifier,
        parent = parent,
        viewFormattingFunctionName = "formatLksgMostImportantProductsForDisplay",
        uploadComponentName = "MostImportantProductsFormField",
        guaranteedFixtureExpression = "dataGenerator.guaranteedArray(() => dataGenerator.generateLksgProduct(), 0, 10)",
        randomFixtureExpression = "dataGenerator.randomArray(() => dataGenerator.generateLksgProduct(), 0, 10)",
    ) {
    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        dataClassBuilder.addProperty(
            identifier,
            TypeReference(
                "kotlin.collections.List", isNullable,
                listOf(
                    TypeReference(
                        "org.dataland.datalandbackend.frameworks.lksg.custom.LksgProduct",
                        false,
                    ),
                ),
            ),
        )
    }
}

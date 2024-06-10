package org.dataland.frameworktoolbox.frameworks.sme.custom

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.requireDocumentSupportIn
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder

/**
 * Represents the sitesandareas component for vsme
 */
class SmeSiteAndAreasComponent(
    identifier: String,
    parent: FieldNodeParent,
) : SmeSimpleCustomComponentBase(
    identifier = identifier,
    parent = parent,
    viewFormattingFunctionName = "formatSmeSiteAndAreaForDisplay",
    uploadComponentName = "SiteAndAreaFormField",
    guaranteedFixtureExpression = "dataGenerator.randomArray(() => dataGenerator.generateSmeSiteAndArea(), 0, 5)",
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
                        "org.dataland.datalandbackend.frameworks.sme.custom.SmeSiteAndAreas",
                        true,
                    ),

                ),
            ),

        )
    }
}

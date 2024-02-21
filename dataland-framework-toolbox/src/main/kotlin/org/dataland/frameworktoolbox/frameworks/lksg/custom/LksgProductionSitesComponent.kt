package org.dataland.frameworktoolbox.frameworks.lksg.custom

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.components.addStandardUploadConfigCell
import org.dataland.frameworktoolbox.intermediate.components.requireDocumentSupportIn
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport
/**
 * Represents the production site component for lksg
 */
class LksgProductionSitesComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {
    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        dataClassBuilder.addProperty(
            identifier,
            TypeReference(
                "List", isNullable,
                listOf(
                    TypeReference(
                        "org.dataland.datalandbackend.frameworks.lksg.custom.LksgProductionSite",
                        true,
                    ),

                ),
            ),

        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "formatLksgProductionSitesForDisplay(${getTypescriptFieldAccessor(true)}, \"${
                    StringEscapeUtils.escapeEcmaScript(
                        label,
                    )
                }\")",
                setOf(
                    TypeScriptImport(
                        "formatLksgProductionSitesForDisplay",
                        "@/components/resources/dataTable/conversion/lksg/LksgValueGetterFactories",
                    ),
                ),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = "ProductionSitesFormField",
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        sectionBuilder.addAtomicExpression(
            identifier,
            "dataGenerator.randomArray(() => dataGenerator.generateLksgProductionSite(), 0, 5)",
        )
    }
}

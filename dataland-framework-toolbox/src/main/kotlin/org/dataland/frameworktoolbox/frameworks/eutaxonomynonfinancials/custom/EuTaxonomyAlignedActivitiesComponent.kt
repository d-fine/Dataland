package org.dataland.frameworktoolbox.frameworks.eutaxonomynonfinancials.custom

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.components.requireDocumentSupportIn
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * TODO add documentation
 */
class EuTaxonomyAlignedActivitiesComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(
    identifier, parent,
) {
    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "formatEuTaxonomyNonFinancialsAlignedActivitiesDataForTable(" +
                    "${getTypescriptFieldAccessor(true)}, \"${
                        StringEscapeUtils.escapeEcmaScript(
                            label,
                        )
                    }\")",
                setOf(
                    TypeScriptImport(
                        "formatEuTaxonomyNonFinancialsAlignedActivitiesDataForTable",
                        "@/components/resources/dataTable/conversion/" +
                            "EuTaxonomyNonFinancialsAlignedActivitiesDataGetterFactory.ts",
                    ),
                ),
            ),
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        sectionBuilder.addAtomicExpression(
            identifier,
            "dataGenerator.generateAlignedActivity()",

        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        return // is built by hand
    }
}

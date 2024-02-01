package org.dataland.frameworktoolbox.frameworks.eutaxonomynonfinancials.custom

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
 * Represents the EuTaxonomy-Specific "AmountWithCurrency" component
 */
class EuTaxonomyAmountWithCurrencyComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(
    identifier, parent,
    "org.dataland.datalandbackend.model.eutaxonomy.nonfinancials.AmountWithCurrency",
) {

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "formatStringForDatatable(\n" +
                    "formatAmountWithCurrency(${getTypescriptFieldAccessor()})\n" +
                    ")",
                setOf(
                    TypeScriptImport(
                        "formatStringForDatatable",
                        "@/components/resources/dataTable/conversion/PlainStringValueGetterFactory",
                    ),
                    TypeScriptImport(
                        "formatAmountWithCurrency",
                        "@/utils/Formatter",
                    ),
                ),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        // do nothing because UploadConfig-generation is deactivated anyway for eu-taxo-non-financials
        return
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        sectionBuilder.addAtomicExpression(
            identifier,
            "dataGenerator.generateAmountWithCurrency()",
        )
    }
}

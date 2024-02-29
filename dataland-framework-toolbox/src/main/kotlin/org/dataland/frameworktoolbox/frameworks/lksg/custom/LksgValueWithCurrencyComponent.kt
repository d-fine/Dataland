package org.dataland.frameworktoolbox.frameworks.lksg.custom

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
 * Represents the Lksg-Specific "ValueWithCurrency" component
 */
class LksgValueWithCurrencyComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(
    identifier, parent,
    "org.dataland.datalandbackend.frameworks.lksg.custom" +
        ".ValueWithCurrency",
) {

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "formatStringForDatatable(\n" +
                    "formatValueWithCurrency(${getTypescriptFieldAccessor()})\n" +
                    ")",
                setOf(
                    TypeScriptImport(
                        "formatStringForDatatable",
                        "@/components/resources/dataTable/conversion/PlainStringValueGetterFactory",
                    ),
                    TypeScriptImport(
                        "formatValueWithCurrency",
                        "@/utils/Formatter",
                    ),
                ),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        /* do nothing because:
        1) UploadConfig-generation is deactivated anyway for eu-taxo-non-financials
        2) We don't have a form-field-component for AmountWithCurrency alone
         */
        return
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        val guaranteedValueWithCurrencyGenerator = "dataGenerator.generateValueWithCurrency()"
        val fixtureExpression = if (isNullable) {
            "dataGenerator.valueOrNull($guaranteedValueWithCurrencyGenerator)"
        } else {
            guaranteedValueWithCurrencyGenerator
        }
        sectionBuilder.addAtomicExpression(
            identifier,
            fixtureExpression,
        )
    }
}

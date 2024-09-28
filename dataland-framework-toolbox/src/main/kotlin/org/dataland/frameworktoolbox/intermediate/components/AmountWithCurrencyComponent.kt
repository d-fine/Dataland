package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.basecomponents.SimpleKotlinBackedBaseComponent
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * Represents the "AmountWithCurrency" component
 */
class AmountWithCurrencyComponent(
    identifier: String,
    parent: FieldNodeParent,
) : SimpleKotlinBackedBaseComponent(
        identifier, parent,
        "org.dataland.datalandbackend.model.generics.AmountWithCurrency",
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
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = "AmountWithCurrencyFormField",
        )
        return
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        val guaranteedAmountWithCurrencyGenerator = "dataGenerator.generateAmountWithCurrency()"
        val fixtureExpression =
            if (isNullable) {
                "dataGenerator.valueOrNull($guaranteedAmountWithCurrencyGenerator)"
            } else {
                guaranteedAmountWithCurrencyGenerator
            }
        sectionBuilder.addAtomicExpression(
            identifier,
            fixtureExpression,
        )
    }
}

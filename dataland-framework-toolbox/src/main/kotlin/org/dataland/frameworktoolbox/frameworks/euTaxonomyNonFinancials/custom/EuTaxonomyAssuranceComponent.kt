package org.dataland.frameworktoolbox.frameworks.euTaxonomyNonFinancials.custom

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.components.addStandardUploadConfigCell
import org.dataland.frameworktoolbox.intermediate.components.requireDocumentSupportIn
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * Represents the EuTaxonomy-Specific "Assurance" component
 */
class EuTaxonomyAssuranceComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent, "org.dataland.datalandbackend.model.eutaxonomy.AssuranceDataPoint") {

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "formatAssuranceForDataTable(${getTypescriptFieldAccessor(true)}, \"${
                    StringEscapeUtils.escapeEcmaScript(
                        label,
                    )
                }\")",
                setOf(
                    TypeScriptImport(
                        "formatAssuranceForDataTable",
                        "@/components/resources/dataTable/conversion/EutaxonomyAssuranceValueGetterFactory",
                    ),
                ),
            ),
        )
        sectionConfigBuilder.addCell(
            label = "Assurance Provider",
            explanation = "Provider of the Assurance",
            shouldDisplay = availableIf.toFrameworkBooleanLambda(),
            valueGetter = FrameworkDisplayValueLambda(
                "formatAssuranceProviderForDataTable(${getTypescriptFieldAccessor(true)}, \"${
                    StringEscapeUtils.escapeEcmaScript(
                        label,
                    )
                }\")",
                setOf(
                    TypeScriptImport(
                        "formatAssuranceProviderForDataTable",
                        "@/components/resources/dataTable/conversion/EutaxonomyAssuranceValueGetterFactory",
                    ),
                ),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = "AssuranceFormField",
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        sectionBuilder.addAtomicExpression(
            identifier,
            "dataGenerator.generateAssuranceDatapoint()",
        )
    }
}

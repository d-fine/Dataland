package org.dataland.frameworktoolbox.frameworks.vsme.custom

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
 * Represents the VsmeSimpleCustomComponentBase component
 * @param viewFormattingFunctionName the name of the formatting function for the view page in VsmeDisplayValueGetters.ts
 */
open class VsmeSimpleCustomComponentBase(
    identifier: String,
    parent: FieldNodeParent,
    val viewFormattingFunctionName: String,
    val uploadComponentName: String,
    val guaranteedFixtureExpression: String,
    val randomFixtureExpression: String?,
) : ComponentBase(identifier, parent) {
    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "$viewFormattingFunctionName(${getTypescriptFieldAccessor(true)}, \"${
                    StringEscapeUtils.escapeEcmaScript(
                        label,
                    )
                }\")",
                setOf(
                    TypeScriptImport(
                        viewFormattingFunctionName,
                        "@/components/resources/dataTable/conversion/vsme/VsmeDisplayValueGetters",
                    ),
                ),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = uploadComponentName,
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        val fixtureExpression =
            if (isNullable && randomFixtureExpression != null) {
                randomFixtureExpression
            } else {
                guaranteedFixtureExpression
            }
        sectionBuilder.addAtomicExpression(
            identifier,
            fixtureExpression,
        )
    }
}

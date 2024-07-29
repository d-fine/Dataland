package org.dataland.frameworktoolbox.frameworks.sfdr.custom

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.components.addStandardUploadConfigCell
import org.dataland.frameworktoolbox.intermediate.components.basecomponents.SimpleKotlinBackedBaseComponent
import org.dataland.frameworktoolbox.intermediate.components.requireDocumentSupportIn
import org.dataland.frameworktoolbox.intermediate.datapoints.ExtendedDocumentSupport
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * Class for the yes no NoEvidenceComponent
 */
class SfdrYesNoNoEvidenceFoundComponent(
    identifier: String,
    parent: FieldNodeParent,
) : SimpleKotlinBackedBaseComponent(
    identifier,
    parent,
    "org.dataland.datalandbackend.model.enums.commons.YesNoNoEvidenceFound",
) {

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        requireDocumentSupportIn(setOf(ExtendedDocumentSupport))
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            documentSupport.getFrameworkDisplayValueLambda(
                FrameworkDisplayValueLambda(
                    "formatYesNoNoEvidenceFoundValueForDatatable(${getTypescriptFieldAccessor(true)})",
                    setOf(
                        TypeScriptImport(
                            "formatYesNoNoEvidenceFoundValueForDatatable",
                            "@/components/resources/dataTable/conversion/YesNoNoEvidenceFoundValueGetterFactory",
                        ),
                    ),
                ),
                label, getTypescriptFieldAccessor(),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        val uploadComponentNameToUse = when (documentSupport) {
            is ExtendedDocumentSupport -> "YesNoNoEvidenceFoundExtendedDataPointFormField"
            else -> throw IllegalArgumentException(
                "YesNoNoEvidenceFoundComponent does not support document support '$documentSupport",
            )
        }
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = uploadComponentNameToUse,
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        sectionBuilder.addAtomicExpression(
            identifier,
            documentSupport.getFixtureExpression(
                fixtureExpression = "dataGenerator.guaranteedYesNoNoEvidenceFound()",
                nullableFixtureExpression = "dataGenerator.randomYesNoNoEvidenceFound()",
                nullable = isNullable,
            ),
        )
    }
}

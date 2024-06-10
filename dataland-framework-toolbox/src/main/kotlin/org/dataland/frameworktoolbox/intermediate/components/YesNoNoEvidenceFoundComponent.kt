package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder

/**
 * Class for the yes no NoEvidenceComponent
 */
class YesNoNoEvidenceFoundComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent, "org.dataland.datalandbackend.model.enums.commons.YesNoNoEvidenceFound") {

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        /*sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            documentSupport.getFrameworkDisplayValueLambda(
                FrameworkDisplayValueLambda(
                    "formatYesNoNoEvidenceFound(${getTypescriptFieldAccessor(true)})",
                    setOf(
                        TypeScriptImport(
                            "formatYesNoNoEvidenceFound",
                            "@/components/resources/dataTable/conversion/YesNoNoEvidenceFoundGetterFactory",
                        ),
                    ),
                ),
                label, getTypescriptFieldAccessor(),
            ),
         )

         */
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        // val uploadComponentNameToUse = when (documentSupport) {
        //    is NoDocumentSupport -> "YesNoFormField"
        //    is SimpleDocumentSupport -> "YesNoBaseDataPointFormField"
        //    is ExtendedDocumentSupport -> "YesNoExtendedDataPointFormField"
        //    else -> throw IllegalArgumentException(
        //    "YesNoComponent does not support document support '$documentSupport")
        // }
        // uploadCategoryBuilder.addStandardUploadConfigCell(
        //    component = this,
        //    uploadComponentName = uploadComponentNameToUse,
        // )
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

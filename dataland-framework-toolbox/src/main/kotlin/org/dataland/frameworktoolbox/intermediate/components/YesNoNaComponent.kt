package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.SimpleDocumentSupport
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * A YesNoComponent is either Yes or No or N/A.
 */
class YesNoNaComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent, "org.dataland.datalandbackend.model.enums.commons.YesNoNa") {

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            documentSupport.getFrameworkDisplayValueLambda(
                FrameworkDisplayValueLambda(
                    "formatYesNoValueForDatatable(${getTypescriptFieldAccessor(true)})",
                    setOf(
                        TypeScriptImport(
                            "formatYesNoValueForDatatable",
                            "@/components/resources/dataTable/conversion/YesNoValueGetterFactory",
                        ),
                    ),
                ),
                label, getTypescriptFieldAccessor(),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport, SimpleDocumentSupport))
        val uploadComponentNameToUse = when (documentSupport) {
            is NoDocumentSupport -> "YesNoNaFormField"
            is SimpleDocumentSupport -> "YesNoNaBaseDataPointFormField"
            else -> throw IllegalArgumentException("YesNoComponent does not support document support '$documentSupport")
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
                fixtureExpression = "dataGenerator.guaranteedYesNoNa()",
                nullableFixtureExpression = "dataGenerator.randomYesNoNa()",
                nullable = isNullable,
            ),
        )
    }
}

package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.SectionUploadConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.getTypescriptFieldAccessor
as getTypescriptFieldAccessorUpload
import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkDisplayValueLambda
as FrameworkDisplayValueLambdaUpload

/**
 * A YesNoComponent is either Yes or No.
 */
class YesNoComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addProperty(
            this.identifier,
            documentSupport.getJvmTypeReference(
                TypeReference("org.dataland.datalandbackend.model.enums.commons.YesNo", isNullable),
                isNullable,
            ),
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            documentSupport.getFrameworkDisplayValueLambda(
                FrameworkDisplayValueLambda(
                    "formatYesNoValueForDatatable(${getTypescriptFieldAccessor(true)})",
                    setOf(
                        "import { formatYesNoValueForDatatable } from " +
                            "\"@/components/resources/dataTable/conversion/YesNoValueGetterFactory\";",
                    ),
                ),
                label, getTypescriptFieldAccessor(),
            ),
        )
    }

    override fun generateDefaultUploadConfig(sectionUploadConfigBuilder: SectionUploadConfigBuilder) {
        sectionUploadConfigBuilder.addStandardCellWithValueGetterFactory(
            uploadComponentName = "YesNoFormField",
            options = null,
            component = this,
            valueGetter = documentSupport.getFrameworkDisplayValueLambdaUpload(
                FrameworkDisplayValueLambdaUpload(
                    "formatYesNoValueForDatatable(${getTypescriptFieldAccessorUpload(true)})",
                    setOf(
                        "import { formatYesNoValueForDatatable } from " +
                            "\"@/components/resources/dataTable/conversion/YesNoValueGetterFactory\";",
                    ),
                ),
                label, getTypescriptFieldAccessorUpload(),
            ),
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        sectionBuilder.addAtomicExpression(
            identifier,
            documentSupport.getFixtureExpression(
                fixtureExpression = "dataGenerator.guaranteedYesNo()",
                nullableFixtureExpression = "dataGenerator.randomYesNo()",
                nullable = isNullable,
            ),
        )
    }
}

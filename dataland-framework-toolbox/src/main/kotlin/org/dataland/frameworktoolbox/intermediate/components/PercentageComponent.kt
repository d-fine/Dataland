package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.basecomponents.NumberBaseComponent
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda

/**
 * A PercentageComponent represents a decimal percentage between 0 % and 100 %.
 */
class PercentageComponent(
    identifier: String,
    parent: FieldNodeParent,
) : NumberBaseComponent(identifier, parent) {

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            documentSupport.getFrameworkDisplayValueLambda(
                FrameworkDisplayValueLambda(
                    "formatPercentageForDatatable(${getTypescriptFieldAccessor(true)})",
                    setOf(
                        "import { formatPercentageForDatatable } from" +
                            " \"@/components/resources/dataTable/conversion/PercentageValueGetterFactory\";",
                    ),
                ),
                label, getTypescriptFieldAccessor(),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = "PercentageFormField",
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        sectionBuilder.addAtomicExpression(
            identifier,
            documentSupport.getFixtureExpression(
                fixtureExpression = "dataGenerator.guaranteedPercentageValue()",
                nullableFixtureExpression = "dataGenerator.randomPercentageValue()",
                nullable = isNullable,
            ),
        )
    }
}

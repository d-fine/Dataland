package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.capitalizeEn

/**
 * A SingleSelectComponent represents a choice between pre-defined values
 */
open class SingleSelectComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {

    var options: MutableSet<SelectionOption> = mutableSetOf()

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        val enum = dataClassBuilder.parentPackage.addEnum(
            name = "${this.identifier.capitalizeEn()}Options",
            options = this.options,
            comment = "Enum class for the field ${this.identifier}",
        )
        dataClassBuilder.addProperty(
            this.identifier,
            documentSupport.getJvmTypeReference(
                enum.getTypeReference(isNullable),
                isNullable,
            ),
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            documentSupport.getFrameworkDisplayValueLambda(
                FrameworkDisplayValueLambda(
                    "formatStringForDatatable(${getTypescriptFieldAccessor(true)})",
                    setOf(
                        "import { formatStringForDatatable } from " +
                            "\"@/components/resources/dataTable/conversion/PlainStringValueGetterFactory\";",
                    ),
                ),
                label, getTypescriptFieldAccessor(),
            ),
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        val enumName = "${this.identifier.capitalizeEn()}Options"
        sectionBuilder.addAtomicExpression(
            identifier,
            documentSupport.getFixtureExpression(
                fixtureExpression = "pickOneElement(Object.values($enumName))",
                nullableFixtureExpression = "dataGenerator.valueOrNull(pickOneElement(Object.values($enumName)))",
                nullable = isNullable,
            ),
            imports = setOf(
                "import { pickOneElement } from \"@e2e/fixtures/FixtureUtils\";",
                "import { $enumName } from \"@clients/backend\";",
            ),
        )
    }
}

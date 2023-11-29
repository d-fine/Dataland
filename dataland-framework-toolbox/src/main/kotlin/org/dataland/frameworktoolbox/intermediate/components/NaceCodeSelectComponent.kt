package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda

/**
 * A NaceCodeSelectComponent represents a selection of valid NACE codes
 */
open class NaceCodeSelectComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addProperty(
            this.identifier,
            documentSupport.getJvmTypeReference(
                TypeReference("List", isNullable, listOf(TypeReference("String", false))),
                isNullable,
            ),
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "formatStringForDatatable(${getTypescriptFieldAccessor()})", // TODO modal for list displayment?
                setOf(
                    "import { formatStringForDatatable } from " +
                        "\"@/components/resources/dataTable/conversion/PlainStringValueGetterFactory\";",
                ),
            ),
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        sectionBuilder.addAtomicExpression(
            identifier,
            documentSupport.getFixtureExpression(
                fixtureExpression = "generateNaceCodes(3)", // TODO random number ?
                nullableFixtureExpression = "dataGenerator.valueOrNull(generateNaceCodes(3))", // TODO random number ?
                nullable = isNullable,
            ),
            imports = setOf(
                "import { generateNaceCodes } from \"@e2e/fixtures/common/NaceCodeFixtures\";",
            ),
        )
    }
}

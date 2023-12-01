package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda

/**
 * A MultiSelectComponent represents a selection of valid NACE codes
 */
open class MultiSelectComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {

    var options: MutableSet<SelectionOption> = mutableSetOf()

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
                "formatListOfStringsForDatatable(${getTypescriptFieldAccessor()}, '$label')",
                setOf(
                    "import { formatListOfStringsForDatatable } from " +
                        "\"@/components/resources/dataTable/conversion/MultiSelectValueGetterFactory\";",
                ),
            ),
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        if (options.size == 1 && options.single().label.contains("\$File")) {
            val expressionToParse = options.single().label
            val fileName = expressionToParse.substringAfter("\$File ").substringBefore(",").trim()
            val importStatement = expressionToParse.substringAfter(", ")
            sectionBuilder.addAtomicExpression(
                identifier,
                documentSupport.getFixtureExpression(
                    fixtureExpression = "pickSubsetOfElements(Object.values($fileName))",
                    nullableFixtureExpression = "dataGenerator.valueOrNull(pickSubsetOfElements(Object.values($fileName)))",
                    nullable = isNullable,
                ),
                imports = setOf(
                    "$importStatement;",
                ),
            )
        } else {
            val formattedString = "[" + options.joinToString { "\"${it.label}\"" } + "]"
            sectionBuilder.addAtomicExpression(
                identifier,
                documentSupport.getFixtureExpression(
                    fixtureExpression = "pickSubsetOfElements($formattedString)",
                    nullableFixtureExpression = "dataGenerator.valueOrNull(pickSubsetOfElements($formattedString))",
                    nullable = isNullable,
                ),
                imports = setOf(
                    "import { pickSubsetOfElements } from \"@e2e/fixtures/FixtureUtils\";",
                ),
            )
        }
    }
}

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
    val enumName = "${identifier.capitalizeEn()}Options"

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        val enum = dataClassBuilder.parentPackage.addEnum(
            name = this.enumName,
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
                    "{\n" +
                        generateMappingObject() +
                        generateMapperFunction() +
                        generateReturnStatement() +
                        "}",
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
        sectionBuilder.addAtomicExpression(
            identifier,
            documentSupport.getFixtureExpression(
                fixtureExpression = "pickOneElement(Object.values(${this.enumName}))",
                nullableFixtureExpression = "dataGenerator.valueOrNull(pickOneElement(Object.values(${this.enumName})))",
                nullable = isNullable,
            ),
            imports = setOf(
                "import { pickOneElement } from \"@e2e/fixtures/FixtureUtils\";",
                "import { ${this.enumName} } from \"@clients/backend\";",
            ),
        )
    }

    private fun generateMappingObject(): String {
        val codeBuilder = StringBuilder()
        codeBuilder.append("const mappings = {\n")

        for (option in this.options) {
            val escapedLabel = option.label.replace("\"", "\\\"") // TODO use ecma?
            codeBuilder.append("    ${option.identifier}: \"$escapedLabel\",\n")
        }

        codeBuilder.append("}\n")

        return codeBuilder.toString()
    }

    private fun generateMapperFunction(): String {
        val jsDoc =
            "/**\n" +
                "* Maps the technical name of a select option to the respective original name\n" + // TODO
                "* @param technicalName of a select option \n" +
                "* @param mappingObject that contains the mappings\n" +
                "* @returns original name that matches the technical name\n" +
                "*/\n"
        val functionBody =
            "function getOriginalNameFromTechnicalName<T extends string>(technicalName: T, mappingObject: {[key in T]:string}): string{\n" +
                "   return mappingObject[technicalName]\n" +
                "}\n"
        return jsDoc + functionBody
    }

    private fun generateReturnStatement(): String {
        return "return formatStringForDatatable(\n" +
            "${getTypescriptFieldAccessor()} ? " +
            "getOriginalNameFromTechnicalName(${getTypescriptFieldAccessor()}, mappings) : \"\"\n" +
            ")\n"
    }
}

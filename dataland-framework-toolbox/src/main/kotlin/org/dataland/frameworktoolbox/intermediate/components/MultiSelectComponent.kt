package org.dataland.frameworktoolbox.intermediate.components

import org.apache.commons.text.StringEscapeUtils.escapeEcmaScript
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
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
 * A MultiSelectComponent represents a selection of valid NACE codes
 */
open class MultiSelectComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {

    var options: MutableSet<SelectionOption> = mutableSetOf()

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addProperty(
            identifier,
            documentSupport.getJvmTypeReference(
                TypeReference("List", isNullable, listOf(TypeReference("String", false))),
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
                        "import { formatListOfStringsForDatatable } from " +
                            "\"@/components/resources/dataTable/conversion/MultiSelectValueGetterFactory\";",
                    ),
                ),
                label, getTypescriptFieldAccessor(),
            ),
        )
    }

    override fun generateDefaultUploadConfig(sectionUploadConfigBuilder: SectionUploadConfigBuilder) {
        sectionUploadConfigBuilder.addStandardCellWithValueGetterFactory(
            uploadComponentName = "MultiSelectFormField",
            options = this.options,
            component = this,
            FrameworkDisplayValueLambdaUpload(
                "formatListOfStringsForDatatable(${getTypescriptFieldAccessorUpload()}, '${escapeEcmaScript(label)}')",
                setOf(
                    "import { formatListOfStringsForDatatable } from " +
                        "\"@/components/resources/dataTable/conversion/MultiSelectValueGetterFactory\";",
                ),
            ),
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        val formattedString = "[" + options.joinToString { "\"${escapeEcmaScript(it.label)}\"" } + "]"
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

    private fun generateMappingObject(): String { // TODO Emanuel: this is a duplicate to SingleSelectComponent; centralize where?
        val codeBuilder = StringBuilder()
        codeBuilder.append("const mappings = {\n")

        for (option in options) {
            val escapedLabel = option.label.replace("\"", "\\\"")
            codeBuilder.append("    ${option.identifier}: \"$escapedLabel\",\n")
        }

        codeBuilder.append("}\n")

        return codeBuilder.toString()
    }

    private fun generateMapperFunction(): String { // TODO Emanuel: this is a duplicate to SingleSelectComponent; centralize where?
        val jsDoc =
            "/**\n" +
                "* Maps the technical name of a select option to the respective original name\n" +
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
        return "return formatListOfStringsForDatatable(" +
            "${getTypescriptFieldAccessor()}?.map(it => \n" +
            "   getOriginalNameFromTechnicalName(it, mappings)), " +
            "'${escapeEcmaScript(label)}'" +
            ")"
    }
}

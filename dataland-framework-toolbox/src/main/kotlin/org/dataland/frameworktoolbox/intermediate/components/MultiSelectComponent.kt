package org.dataland.frameworktoolbox.intermediate.components

import org.apache.commons.text.StringEscapeUtils.escapeEcmaScript
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkUploadOptions
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.generateTsCodeForOptions
import org.dataland.frameworktoolbox.utils.typescript.generateTsCodeForSelectOptionsMappingObject

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
                        generateTsCodeForSelectOptionsMappingObject(options) +
                        generateReturnStatement() +
                        "}",
                    setOf(
                        "import { formatListOfStringsForDatatable } from " +
                            "\"@/components/resources/dataTable/conversion/MultiSelectValueGetterFactory\";",
                        "import { getOriginalNameFromTechnicalName } from " +
                            "\"@/components/resources/dataTable/conversion/Utils\";",
                    ),
                ),
                label, getTypescriptFieldAccessor(),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        uploadCategoryBuilder.addStandardUploadConfigCell(
            frameworkUploadOptions = FrameworkUploadOptions(
                body = generateTsCodeForOptions(this.options),
                imports = null,
            ),
            component = this,
            uploadComponentName = "MultiSelectFormField",
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        val formattedString = "[" + options.joinToString { "\"${escapeEcmaScript(it.identifier)}\"" } + "]"
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

    private fun generateReturnStatement(): String {
        return "return formatListOfStringsForDatatable(" +
            "${getTypescriptFieldAccessor()}?.map(it => \n" +
            "   getOriginalNameFromTechnicalName(it, mappings)), " +
            "'${escapeEcmaScript(label)}'" +
            ")"
    }
}

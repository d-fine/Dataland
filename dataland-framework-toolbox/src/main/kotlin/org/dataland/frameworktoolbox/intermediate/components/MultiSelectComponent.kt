package org.dataland.frameworktoolbox.intermediate.components

import org.apache.commons.text.StringEscapeUtils.escapeEcmaScript
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.addPropertyWithDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkUploadOptions
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.capitalizeEn
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport
import org.dataland.frameworktoolbox.utils.typescript.generateTsCodeForOptionsOfSelectionFormFields
import org.dataland.frameworktoolbox.utils.typescript.generateTsCodeForSelectOptionsMappingObject

/**
 * A MultiSelectComponent represents a selection of string-options. Multiple of those string-options
 * can be selected.
 */
open class MultiSelectComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {
    var options: Set<SelectionOption> = mutableSetOf()
    val enumName = "${identifier.capitalizeEn()}Options"

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        val enum =
            dataClassBuilder.parentPackage.addEnum(
                name = enumName,
                options = options,
                comment = "Enum class for the multi-select-field $identifier",
            )
        dataClassBuilder.addPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            TypeReference(
                "java.util.EnumSet",
                isNullable, listOf(enum.getTypeReference(false)),
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
                        TypeScriptImport(
                            "formatListOfStringsForDatatable",
                            "@/components/resources/dataTable/conversion/MultiSelectValueGetterFactory",
                        ),
                        TypeScriptImport(
                            "getOriginalNameFromTechnicalName",
                            "@/components/resources/dataTable/conversion/Utils",
                        ),
                    ),
                ),
                label, getTypescriptFieldAccessor(),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        uploadCategoryBuilder.addStandardUploadConfigCell(
            frameworkUploadOptions =
                FrameworkUploadOptions(
                    body = generateTsCodeForOptionsOfSelectionFormFields(this.options),
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
            imports =
                setOf(
                    TypeScriptImport(
                        "pickSubsetOfElements",
                        "@e2e/fixtures/FixtureUtils",
                    ),
                ),
        )
    }

    private fun generateReturnStatement(): String =
        "return formatListOfStringsForDatatable(" +
            "${getTypescriptFieldAccessor()}?.map(it => \n" +
            "   getOriginalNameFromTechnicalName(it, mappings)), " +
            "'${escapeEcmaScript(label)}'" +
            ")"
}

package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.intermediate.datapoints.ExtendedDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.addPropertyWithDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.qamodel.addQaPropertyWithDocumentSupport
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkUploadOptions
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport
import org.dataland.frameworktoolbox.utils.typescript.generateTsCodeForOptionsOfSelectionFormFields
import org.dataland.frameworktoolbox.utils.typescript.generateTsCodeForSelectOptionsMappingObject

/**
 * A SingleSelectComponent represents a choice between pre-defined values
 */
open class SingleSelectComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {
    /**
     * The UploadMode of a SingleSelectComponent determines the form element styling for the upload
     * page (i.e., dropdown or radio buttons?)
     */
    enum class UploadMode(val component: String) {
        Dropdown("SingleSelectFormField"),
        RadioButtons("RadioButtonsFormField"),
    }

    var options: Set<SelectionOption> = mutableSetOf()
    private var enumName = "${camelCaseComponentIdentifier}Options"
    var uploadMode: UploadMode = UploadMode.Dropdown

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        val enum = dataClassBuilder.parentPackage.addEnum(
            name = enumName,
            options = options,
            comment = "Enum class for the single-select-field $identifier",
        )
        dataClassBuilder.addPropertyWithDocumentSupport(
            documentSupport = documentSupport,
            name = identifier,
            type = enum.getTypeReference(isNullable),
        )
    }

    override fun generateDefaultQaModel(dataClassBuilder: DataClassBuilder) {
        val enumTypeReference =
            TypeReference(dataClassBuilder.parentPackage.fullyQualifiedName + "." + enumName, isNullable)
        dataClassBuilder.addQaPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            enumTypeReference,
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            documentSupport.getFrameworkDisplayValueLambda(
                FrameworkDisplayValueLambda(
                    "((): AvailableMLDTDisplayObjectTypes =>{\n" +
                        generateTsCodeForSelectOptionsMappingObject(options) +
                        generateReturnStatement() +
                        "})()",
                    setOf(
                        TypeScriptImport(
                            "formatStringForDatatable",
                            "@/components/resources/dataTable/conversion/PlainStringValueGetterFactory",
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
        uploadCategoryBuilder.addStandardUploadConfigCell(
            frameworkUploadOptions = FrameworkUploadOptions(
                body = generateTsCodeForOptionsOfSelectionFormFields(this.options),
                imports = null,
            ),
            component = this,
            uploadComponentName = uploadMode.component,
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        sectionBuilder.addAtomicExpression(
            identifier,
            documentSupport.getFixtureExpression(
                fixtureExpression = "pickOneElement(Object.values($enumName))",
                nullableFixtureExpression = "dataGenerator.valueOrNull(pickOneElement(Object.values($enumName)))",
                nullable = isNullable,
            ),
            imports = setOf(
                TypeScriptImport("pickOneElement", "@e2e/fixtures/FixtureUtils"),
                TypeScriptImport(enumName, "@clients/backend"),
            ),
        )
    }

    private fun generateReturnStatement(): String {
        val fieldAccessor = getTypescriptFieldAccessor()
        val dataPointValueAccessor =
            if (documentSupport == ExtendedDocumentSupport) "$fieldAccessor?.value" else fieldAccessor
        return "return formatStringForDatatable(\n" +
            "$dataPointValueAccessor ? " +
            "getOriginalNameFromTechnicalName($dataPointValueAccessor, mappings) : \"\"\n" +
            ")\n"
    }
}

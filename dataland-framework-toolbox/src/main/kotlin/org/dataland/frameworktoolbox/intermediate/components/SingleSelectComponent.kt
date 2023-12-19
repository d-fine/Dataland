package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkUploadOptions
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.capitalizeEn
import org.dataland.frameworktoolbox.utils.typescript.generateTsCodeForOptions
import org.dataland.frameworktoolbox.utils.typescript.generateTsCodeForSelectOptionsMappingObject

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
            name = enumName,
            options = options,
            comment = "Enum class for the field $identifier",
        )
        dataClassBuilder.addProperty(
            identifier,
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
                        generateTsCodeForSelectOptionsMappingObject(options) +
                        generateReturnStatement() +
                        "}",
                    setOf(
                        "import { formatStringForDatatable } from " +
                            "\"@/components/resources/dataTable/conversion/PlainStringValueGetterFactory\";",
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
            uploadComponentName = "SingleSelectFormField",
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
                "import { pickOneElement } from \"@e2e/fixtures/FixtureUtils\";",
                "import { $enumName } from \"@clients/backend\";",
            ),
        )
    }

    private fun generateReturnStatement(): String {
        return "return formatStringForDatatable(\n" +
            "${getTypescriptFieldAccessor()} ? " +
            "getOriginalNameFromTechnicalName(${getTypescriptFieldAccessor()}, mappings) : \"\"\n" +
            ")\n"
    }
}

// TODO Emanuel: Check if you have used EcmaString everywhere where you put a val into the generated JS Code

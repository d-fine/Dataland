package org.dataland.frameworktoolbox.intermediate.components

import org.apache.commons.text.StringEscapeUtils.escapeEcmaScript
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkUploadOptions
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * An ISO2 Country Code represents a selection of string-options generated from country Codes. Multiple entries can be
 * selected.
 */
open class Iso2CountryCodesMultiSelectComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {

    var options: Set<SelectionOption> = mutableSetOf()
    var filePathOfPremadeDropdownDatasets: String = "@/utils/PremadeDropdownDatasets"

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        dataClassBuilder.addProperty(
            this.identifier,
            TypeReference(
                "List",
                isNullable,
                listOf(TypeReference("String", false)),
            ),
        )
    }
    private val mappings = "const mappings = getDatasetAsMap(DropdownDatasetIdentifier.CountryCodesIso2);"
    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            documentSupport.getFrameworkDisplayValueLambda(
                FrameworkDisplayValueLambda(
                    "{\n" +
                        mappings +
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
                        TypeScriptImport("DropdownDatasetIdentifier", filePathOfPremadeDropdownDatasets),
                        TypeScriptImport("getDatasetAsMap", filePathOfPremadeDropdownDatasets),
                    ),
                ),
                label, getTypescriptFieldAccessor(),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        uploadCategoryBuilder.addStandardUploadConfigCell(
            frameworkUploadOptions = FrameworkUploadOptions(
                body = "getDataset(DropdownDatasetIdentifier.CountryCodesIso2)",
                imports =
                setOf(
                    TypeScriptImport("DropdownDatasetIdentifier", filePathOfPremadeDropdownDatasets),
                    TypeScriptImport("getDataset", filePathOfPremadeDropdownDatasets),
                ),
            ),
            component = this,
            uploadComponentName = "MultiSelectFormField",
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        val formattedString = "[ \"DE\", \"AL\", \"AZ\", \"GB\", \"US\", \"DK\"]"
        sectionBuilder.addAtomicExpression(
            identifier,
            documentSupport.getFixtureExpression(
                fixtureExpression = "pickSubsetOfElements($formattedString)",
                nullableFixtureExpression = "dataGenerator.valueOrNull(pickSubsetOfElements($formattedString))",
                nullable = isNullable,
            ),
            imports = setOf(
                TypeScriptImport(
                    "pickSubsetOfElements",
                    "@e2e/fixtures/FixtureUtils",
                ),
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

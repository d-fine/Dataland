package org.dataland.frameworktoolbox.intermediate.components

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.datapoints.SimpleDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * In-memory representation of a field that contains a list of base-data-points.
 * The base-data-points have strings as their values.
 */
class ListOfStringBaseDataPointComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {
    var descriptionColumnHeader: String = "Description"
    var documentColumnHeader: String = "Document"

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addProperty(
            this.identifier,
            TypeReference(
                "List",
                isNullable,
                listOf(
                    SimpleDocumentSupport.getJvmTypeReference(
                        TypeReference("String", false),
                        false,
                    ),
                ),
            ),
            SimpleDocumentSupport.getJvmAnnotations(),
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "{\n" +
                    "return formatListOfBaseDataPoint(\n" +
                    "'${StringEscapeUtils.escapeEcmaScript(label)}',\n" +
                    "${getTypescriptFieldAccessor()},\n" +
                    "\"$descriptionColumnHeader\",\n" +
                    "\"$documentColumnHeader\",\n" +
                    ")\n" +
                    "}",
                setOf(
                    TypeScriptImport(
                        "formatListOfBaseDataPoint",
                        "@/components/resources/dataTable/conversion/ListOfBaseDataPointGetterFactory",
                    ),
                ),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = "ListOfBaseDataPointsFormField",
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        sectionBuilder.addAtomicExpression(
            identifier,
            "dataGenerator.valueOrNull(" +
                "generateArray(" +
                "() => dataGenerator.guaranteedBaseDataPoint(dataGenerator.guaranteedShortString()), 1, 5, 0" +
                "))",
            setOf(
                TypeScriptImport("generateArray", "@e2e/fixtures/FixtureUtils"),
            ),
        )
    }
}
